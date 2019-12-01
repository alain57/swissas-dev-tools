package com.swissas.toolwindow;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreeSelectionModel;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.swissas.action.criticalActionToggle;
import com.swissas.beans.AttributeChildrenBean;
import com.swissas.beans.File;
import com.swissas.beans.Message;
import com.swissas.beans.Module;
import com.swissas.beans.Type;
import com.swissas.util.ProjectUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


/**
 * The warning content panel where all the different code warning coming from the company will be displayed
 *
 * @author Tavan Alain
 */

public class WarningContent extends JTabbedPane implements ToolWindowFactory {
	
	private static final String WARNING_CONTENT_TIMER = "WarningContentTimer";
	private static final String CODE_CHECK            = "code check";
	private static final String ROOT                  = "Root";
	
	private static final String COMPILER = "compiler";
	private static final String SONAR    = "sonar";
	
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
	private static final ResourceBundle URL_BUNDLE      = ResourceBundle.getBundle("urls");
	
	private static final String MESSAGE_URL = URL_BUNDLE.getString("url.warnings");
	public static final  String ID          = "SAS Warnings";
	
	private final Set<Type> types = new TreeSet<>();
	
	private final criticalActionToggle criticalActionToggle;
	private       SwissAsStorage       swissAsStorage;
	
	public WarningContent() {
		this.criticalActionToggle = new criticalActionToggle();
		this.criticalActionToggle.setWarningContent(this);
	}
	
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		if (ProjectUtil.getInstance().isAmosProject(project)) {
			this.swissAsStorage = SwissAsStorage.getInstance();
			ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
			Content content = contentFactory.createContent(this, "", false);
			toolWindow.getContentManager().addContent(content);
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					WarningContent.this.refresh();
				}
			};
			Timer timer = new Timer(WARNING_CONTENT_TIMER);
			timer.schedule(timerTask, 30, 24 * 60 * 60_000);
			
			((ToolWindowEx) toolWindow).setTitleActions(this.criticalActionToggle);
		}
	}
	
	
	public void refresh() {
		try {
			if (!"".equals(this.swissAsStorage.getFourLetterCode())) {
				int selectedTab = this.getSelectedIndex() == -1 ? 0 : this.getSelectedIndex();
				readURL();
				fillView();
				if (this.getTabCount() > selectedTab) {
					this.setSelectedIndex(selectedTab);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readURL() throws IOException {
		if (!this.swissAsStorage.getFourLetterCode().isEmpty()) {
			this.types.clear();
			Elements typesDifferentThanCodeCheck = Jsoup
					.connect(MESSAGE_URL + this.swissAsStorage.getFourLetterCode()).get()
					.select("type[ident~=[^CODE_CHECK]]");
			for (Element type : typesDifferentThanCodeCheck) {
				Type currentType = new Type(type);
				for (Node module : type.childNodes()) {
					Module currentModule = new Module(module);
					for (Node file : module.childNodes()) {
						File currentFile = new File(file);
						for (Node message : file.childNodes()) {
							Message currentMessage = new Message(message);
							if (!this.criticalActionToggle.isCriticalOnly() ||
							    currentType.getMainAttribute().equalsIgnoreCase(COMPILER) ||
							    currentType.getMainAttribute().equalsIgnoreCase(SONAR) && currentMessage
									    .isCritical()) {
								currentFile.addChildren(currentMessage);
							}
						}
						currentModule.addChildren(currentFile);
					}
					currentType.addChildren(currentModule);
				}
				this.types.add(currentType);
			}
		}
	}
	
	private void fillView() {
		removeAll();
		if (!this.types.isEmpty()) {
			for (Type type : this.types) {
				if (!type.getMainAttribute().equalsIgnoreCase(
						CODE_CHECK)) { //code check has no use in the eclipse plugin, therefore get rid of useless stuff
					WarningContentTreeNode root = new WarningContentTreeNode(ROOT);
					for (AttributeChildrenBean childrenBean : type.getChildren()) {
						addModuleNodeToRootIfHasChildren(root, (Module)childrenBean);
					}
					createAndAddTreeForTypeAndRootNode(type, root);
				}
			}
		}
	}
	
	private void addModuleNodeToRootIfHasChildren(WarningContentTreeNode root, Module module) {
		int messageCount = 0;
		WarningContentTreeNode moduleNode = new WarningContentTreeNode("");
		for (AttributeChildrenBean attributeChildrenBean : module.getChildren()) {
			File file = (File) attributeChildrenBean;
			WarningContentTreeNode fileNode = new WarningContentTreeNode("");
			addMessageNodesToFileNode(file, fileNode);
			messageCount += addFileNodeToModuleNodeAndRetunAmountOfChildren(moduleNode, file,
			                                                                fileNode);
		}
		if (moduleNode.getChildCount() > 0) {
			moduleNode.setUserObject(module.getMainAttribute() + " (" + messageCount + ")");
			root.add(moduleNode);
		}
	}
	
	private int addFileNodeToModuleNodeAndRetunAmountOfChildren(WarningContentTreeNode moduleNode,
	                                                            File file,
	                                                            WarningContentTreeNode fileNode) {
		int children = 0;
		if (fileNode.getChildCount() > 0) {
			fileNode.setUserObject(file.getMainAttribute() + " (" + fileNode.getChildCount() + ")");
			moduleNode.add(fileNode);
			children = fileNode.getChildCount();
		}
		return children;
	}
	
	private void addMessageNodesToFileNode(File file, WarningContentTreeNode fileNode) {
		for (AttributeChildrenBean childrenBean : file.getChildren()) {
			Message message = (Message)childrenBean; 
			WarningContentTreeNode messageNode = new WarningContentTreeNode(message.getLine(),
			                                                                message.getDescription());
			messageNode.setCritical(message.isCritical());
			fileNode.add(messageNode);
		}
	}
	
	private void createAndAddTreeForTypeAndRootNode(Type type, WarningContentTreeNode root) {
		Tree tree = new Tree(root);
		tree.setCellRenderer(new WarningContentTreeCellRender());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setToolTipText(RESOURCE_BUNDLE.getString("mark.unmark.description"));
		tree.setRootVisible(false);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				WarningContentTreeNode selectedNode = (WarningContentTreeNode) tree
						.getLastSelectedPathComponent();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2
				    && tree.getSelectionPath() != null
				    && tree.getSelectionPath().getPath().length > 3) {
					Project p = DataManager.getInstance().getDataContext(WarningContent.this)
					                       .getData(PlatformDataKeys.PROJECT);
					if (p != null) {
						Object[] path = tree.getSelectionPath().getPath();
						int line = 0;
						if (path.length > 3) {
							String lineAndDesc = path[3].toString();
							line = Integer.parseInt(
									lineAndDesc.substring(5, lineAndDesc.indexOf(" :"))) - 1;
						}
						String pathAndAmountOfErrors = path[2].toString();
						String filepath = p.getBasePath() + "/" + pathAndAmountOfErrors
								.substring(0, pathAndAmountOfErrors.indexOf(" ("));
						VirtualFile file = VfsUtil
								.findFileByIoFile(new java.io.File(filepath), true);
						if (file == null) {
							JOptionPane.showMessageDialog(null, RESOURCE_BUNDLE.getString(
									"opening.following.file.is.not.working") + filepath,
							                              RESOURCE_BUNDLE.getString("error"),
							                              JOptionPane.ERROR_MESSAGE);
						} else {
							new OpenFileDescriptor(p, file, line, 0).navigate(true);
						}
					}
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					if (selectedNode != null) {
						displayMenuForSelectedNodeOnPosition(selectedNode, e.getX(), e.getY());
					}
				} else if (e.getButton() == MouseEvent.BUTTON2) {
					selectedNode.switchMark();
				}
			}
			
			private void displayMenuForSelectedNodeOnPosition(WarningContentTreeNode selectedNode,
			                                                  int x, int y) {
				JPopupMenu popupMenu = new JPopupMenu();
				JMenuItem markDone = new JMenuItem(
						selectedNode.isMarked() ? "reset mark as done" : "mark as done");
				markDone.addActionListener(event -> selectedNode.switchMark());
				popupMenu.add(markDone);
				popupMenu.show(tree, x, y);
			}
		});
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				WarningContentTreeNode selectedNode = (WarningContentTreeNode) tree
						.getLastSelectedPathComponent();
				if (selectedNode != null && (e.getKeyCode() == KeyEvent.VK_DELETE
				                             || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
					selectedNode.switchMark();
				}
			}
		});
		add(type.getMainAttribute(), new JBScrollPane(tree));
	}
	
}
