package com.swissas.toolwindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JTabbedPane;
import javax.swing.tree.TreeSelectionModel;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.swissas.action.CriticalActionToggle;
import com.swissas.beans.AttributeChildrenBean;
import com.swissas.beans.Directory;
import com.swissas.beans.File;
import com.swissas.beans.Message;
import com.swissas.beans.Type;
import com.swissas.toolwindow.WarningContentTreeNode.TreeType;
import com.swissas.toolwindow.adapters.WarningContentKeyAdapter;
import com.swissas.toolwindow.adapters.WarningContentMouseAdapter;
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
	private static final String ROOT                  = "Root";
	private static final String MOVE_TO_SERVER_ATTRIBUTE = "\"Move this code to the server\"";
	
	private static final String MESSAGE_URL = ResourceBundle.getBundle("urls").getString("url.warnings");
	public static final  String ID          = "SAS Warnings";
	
	private final Set<Type>              types       = new TreeSet<>();
	private final Map<String, Directory> directories = new TreeMap<>();
	
	private final CriticalActionToggle criticalActionToggle;
	private       SwissAsStorage       swissAsStorage;
	private Project                    project;
	private static final String        MOVE_TO_SERVER_TEAM = "move to server (Team)";
	
	public WarningContent() {
		this.criticalActionToggle = new CriticalActionToggle();
		this.criticalActionToggle.setWarningContent(this);
	}
	
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		if (ProjectUtil.getInstance().isAmosProject(project)) {
			this.project = project;
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
			timer.schedule(timerTask, 30, 24 * 60 * 60_000L);
			toolWindow.setTitleActions(List.of(this.criticalActionToggle));
		}else {
			this.project = null;
		}
	}
	
	public void refresh() {
			if (!"".equals(this.swissAsStorage.getFourLetterCode())) {
				int selectedTab = this.getSelectedIndex() == -1 ? 0 : this.getSelectedIndex();
				if(readWarningsAndFindings()) {
					fillView();
					if (this.getTabCount() > selectedTab) {
						this.setSelectedIndex(selectedTab);
					}
				}
			}
	}
	
	private Elements readUrlAndUseCssSelector(String url, String cssSelector, boolean logToError){
		Elements result = null;
		try{
			result = Jsoup.connect(url).get().select(cssSelector);
		}catch (IOException e){
			if(logToError){
				Logger.getInstance("Swiss-as").error(e);
			}else {
				Logger.getInstance("Swiss-as").info(e);
			}
		}
		
		return result;
	}
	
	private boolean readWarningsAndFindings() {
		if (!this.swissAsStorage.getFourLetterCode().isEmpty()) {
			this.types.clear();
			Elements typesDifferentThanCodeCheck = readUrlAndUseCssSelector(MESSAGE_URL + this.swissAsStorage.getFourLetterCode()
			                                                                ,"type[ident~=[^CODE_CHECK]]", true);
			if(typesDifferentThanCodeCheck == null) {
				return false;	
			}
			Elements typesDifferentThanCodeCheckForTeam = readUrlAndUseCssSelector(MESSAGE_URL + this.swissAsStorage.getMyTeam()
			                                                                       ,"type[ident~=[^CODE_CHECK]]", false);
			if(typesDifferentThanCodeCheckForTeam != null) {
				typesDifferentThanCodeCheck.addAll(typesDifferentThanCodeCheckForTeam);
			}
			for (Element type : typesDifferentThanCodeCheck) {
				generateTypeFromElementType(type);
			}
			List<Element> moveToServerFiles = new ArrayList<>();
			List<Element> moveToServerFilesMeOnly = new ArrayList<>();
			for (String member : this.swissAsStorage.getMyTeamMembers(true)) {
				Elements sonarMoveToServer = readUrlAndUseCssSelector(MESSAGE_URL + member
						, "file:has(message[description="
						  + MOVE_TO_SERVER_ATTRIBUTE  + "])", false);
				if(sonarMoveToServer != null) {
					moveToServerFiles.addAll(sonarMoveToServer);
					if(member.equals(this.swissAsStorage.getFourLetterCode())) {
						moveToServerFilesMeOnly.addAll(sonarMoveToServer);
					}
				}
			}
			generateMoveToServer("move to server", moveToServerFilesMeOnly);
			generateMoveToServer(MOVE_TO_SERVER_TEAM, moveToServerFiles);
		}
		return true;
	}
	
	private void generateMoveToServer(String title, List<Element> elements) {
		Type type = new Type(title);
		elements.forEach( fileNode -> {
			String fullPath = fileNode.attr("path");
			fullPath = fullPath.substring(0, fullPath.lastIndexOf("/"));
			type.addChildren(generateDirectory(fullPath, "", fileNode, title));
		});
		this.types.add(type);
	}
	
	
	private void generateTypeFromElementType(Element elementType) {
		String typeName = elementType.attr("name");
		Type type = new Type(elementType);
		elementType.childNodes().stream().flatMap(e -> e.childNodes().stream()).forEach(fileNode -> {
			String fullPath = fileNode.attr("path");
			fullPath = fullPath.substring(0, fullPath.lastIndexOf("/"));
			type.addChildren(generateDirectory(fullPath, "", fileNode, typeName));
		});
		this.types.add(type);
	}
	
	private Directory generateDirectory(String folderName, String parentPath,Node fileNode, String typeName) {
		int indexOf = folderName.indexOf("/");
		Directory dir;
		if(indexOf > -1) {
			String topFolder = folderName.substring(0, indexOf);
			dir = this.directories.computeIfAbsent(typeName + "-> " + parentPath + topFolder,  k-> new Directory(topFolder, parentPath));
			dir.addChildren(generateDirectory(folderName.substring(indexOf+1), parentPath + topFolder + "/", fileNode, typeName));
		}else {
			dir = this.directories.computeIfAbsent(typeName + "-> " + parentPath + folderName,  k-> new Directory(folderName, parentPath));
			String path = fileNode.attr("path");
			String fileName = path.substring(path.lastIndexOf("/")+1);
			dir.addChildren(new File(fileName, fileNode));
		}
		return dir;
	}
	
	private void fillView() {
		removeAll();
		if (!this.types.isEmpty()) {
			for(Type type : this.types) {
				String typeName = type.getMainAttribute();
				WarningContentTreeNode root = new WarningContentTreeNode(ROOT);
				for(AttributeChildrenBean child : type.getChildren()) {
					fillTreeWithChildren(root, child, typeName.contains("move to server"), 
					                     "sonar".equalsIgnoreCase(typeName) && this.criticalActionToggle.isCriticalOnly());
				}
				addTreeWithTitleAndRoot(typeName, root, MOVE_TO_SERVER_TEAM.equals(typeName));
			}
		}
	}
	
	
	private void fillTreeWithChildren(WarningContentTreeNode node, AttributeChildrenBean element, boolean ignoreWarnings, boolean onlyCritical) {
		WarningContentTreeNode currentElement = new WarningContentTreeNode("");
		if(element instanceof File) {
			currentElement.setCurrentType(TreeType.File);
			currentElement.setMine(((File)element).isMine());
		}else if(element instanceof Message) {
			currentElement.setCurrentType(TreeType.Message);
			if(ignoreWarnings && ((Message)element).isWarning()) {
				return;
			}
			if(onlyCritical && !((Message)element).isCritical()) {
				return;
			}
		}
		
		Set<AttributeChildrenBean> children = element.getChildren();
		
		if(element instanceof Directory && children.size() == 1) {
			StringBuilder sb = new StringBuilder();
			Directory dir = getSingleDirectoryPath(sb, (Directory) element);			
			currentElement.setUserObject(sb.toString());
			children = dir.getChildren();
		}
		children.forEach(child -> fillTreeWithChildren(currentElement, child, ignoreWarnings, onlyCritical));
		
		
		if(currentElement.isLeaf()) {
			currentElement.setUserObject(element.getText());
		}else {
			String text = currentElement.getUserObject().toString().isBlank() ? element.getText() 
			                                                                  : currentElement.getUserObject().toString(); 
			currentElement
						.setUserObject(text);
			
		}
		node.add(currentElement);
	}
	
	
	private Directory getSingleDirectoryPath(StringBuilder sb, Directory directory) {
		Directory dir;
		sb.append(directory.getMainAttribute());
		if(directory.getChildren().stream().filter(Directory.class::isInstance).count() == 1L) {
			Directory childDirectory = directory.getChildren().stream()
			                                    .filter(Directory.class::isInstance)
			                                    .map(Directory.class::cast).findFirst().get();
			sb.append("/");
			dir =  getSingleDirectoryPath(sb, childDirectory);
		}else {
			dir = directory;
		}
		return dir;
	}
	
	private void addTreeWithTitleAndRoot(String title, WarningContentTreeNode root, boolean myTasksInBold) {
		Tree tree = new Tree(root);
		tree.setCellRenderer(new WarningContentTreeCellRender(myTasksInBold));
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setToolTipText(ResourceBundle.getBundle("texts").getString("mark.unmark.description"));
		tree.setRootVisible(false);
		tree.addMouseListener(new WarningContentMouseAdapter(this.project, tree));
		tree.addKeyListener(new WarningContentKeyAdapter(tree));
		add(title, new JBScrollPane(tree));
	}
}
