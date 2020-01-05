package com.swissas.toolwindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.JTabbedPane;
import javax.swing.tree.TreeSelectionModel;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.swissas.action.CriticalActionToggle;
import com.swissas.beans.AttributeChildrenBean;
import com.swissas.beans.File;
import com.swissas.beans.Message;
import com.swissas.beans.Module;
import com.swissas.beans.Type;
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
	private static final String CODE_CHECK            = "code check";
	private static final String ROOT                  = "Root";
	
	private static final String COMPILER = "compiler";
	private static final String SONAR    = "sonar";
	private static final String MOVE_TO_SERVER = "Move to Server";
	private static final String MOVE_TO_SERVER_TEAM = "Move to Server (team)";
	private static final String MOVE_TO_SERVER_ATTRIBUTE = "\"Move this code to the server\"";
	
	
	private static final String MESSAGE_URL = ResourceBundle.getBundle("urls").getString("url.warnings");
	public static final  String ID          = "SAS Warnings";
	
	private final Map<String, Type> types = new TreeMap<>();
	
	private final CriticalActionToggle criticalActionToggle;
	private       SwissAsStorage       swissAsStorage;
	private Project                    project;
	private List<Element>              moveToServerModules;
	private List<Element>              moveToServerModulesMeOnly;
	
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
			
			((ToolWindowEx) toolWindow).setTitleActions(this.criticalActionToggle);
		}else {
			this.project = null;
		}
	}
	
	public void refresh() {
			if (!"".equals(this.swissAsStorage.getFourLetterCode())) {
				int selectedTab = this.getSelectedIndex() == -1 ? 0 : this.getSelectedIndex();
				if(readURL()) {
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
	
	private boolean readURL() {
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
			this.moveToServerModules = new ArrayList<>();
			this.moveToServerModulesMeOnly = new ArrayList<>();
			for (String member : this.swissAsStorage.getMyTeamMembers(true)) {
				Elements sonarMoveToServer = readUrlAndUseCssSelector(MESSAGE_URL + member
						, "module:has(file > message[description="
						  + MOVE_TO_SERVER_ATTRIBUTE  + "])", false);
				if(sonarMoveToServer != null) {
					this.moveToServerModules.addAll(sonarMoveToServer);
					if(member.equals(this.swissAsStorage.getFourLetterCode())) {
						this.moveToServerModulesMeOnly.addAll(sonarMoveToServer);
					}
				}
			}
		}
		return true;
	}
	
	private void generateTypeFromElementType(Element elementType) {
		Type tmpType = new Type(elementType);
		String typeName = tmpType.getMainAttribute();
		Type type = this.types.getOrDefault(typeName, tmpType);
		for (Node moduleNode : elementType.childNodes()) {
			type.addChildren(generateModuleFromModuleNodeAndType(moduleNode, type));
		}
		this.types.putIfAbsent(typeName, type);
	}
	private void generateMoveToServerModuleFromElement(Element elementNode, Map<String, Module> modulesByName){
		Module tmpModule =  new Module(elementNode);
		Module module = modulesByName.getOrDefault(tmpModule.getMainAttribute(), tmpModule);
		for(Element fileElement : elementNode.select("file:has(message[description="
		                                             + MOVE_TO_SERVER_ATTRIBUTE + "])")){
			File file = new File(fileElement);
			for(Element messageElement : fileElement.select("message[description="
			                                                + MOVE_TO_SERVER_ATTRIBUTE + "]")){
				Message message = new Message(messageElement);
				file.addChildren(message);
			}
			module.addChildren(file);
		}
		modulesByName.put(module.getMainAttribute(), module);
	}
	
	private Module generateModuleFromModuleNodeAndType(Node moduleNode, Type type) {
		Module module = new Module(moduleNode);
		for (Node fileNode : moduleNode.childNodes()) {
			module.addChildren(generateFileFromFileNodeAndType(fileNode, type));
		}
		return module;
	}
	
	private File generateFileFromFileNodeAndType(Node fileNode, Type type) {
		File file = new File(fileNode);
		for (Node messageNode : fileNode.childNodes()) {
			Message currentMessage = new Message(messageNode);
			if (!this.criticalActionToggle.isCriticalOnly() ||
			    type.getMainAttribute().equalsIgnoreCase(COMPILER) ||
			    type.getMainAttribute().equalsIgnoreCase(SONAR) && currentMessage
					    .isCritical()) {
				file.addChildren(currentMessage);
			}
		}
		return file;
	}
	
	private void fillView() {
		removeAll();
		if (!this.types.isEmpty()) {
			for (Type type : this.types.values()) {
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
		WarningContentTreeNode root = new WarningContentTreeNode(ROOT);
		fillMoveToServerRoot(root, true);
		addMoveToServerTeamTree(root);
		
		root = new WarningContentTreeNode(ROOT);
		fillMoveToServerRoot(root, false);
		addMoveToServerTree(root);
	}
	
	private void fillMoveToServerRoot(WarningContentTreeNode root, boolean entireTeam) {
		List<Element> elements = entireTeam ? this.moveToServerModules : this.moveToServerModulesMeOnly;
		Map<String,Module> modulesByName = new HashMap<>();
		for (Element elementNode : elements) {
			generateMoveToServerModuleFromElement(elementNode, modulesByName);
		}
		modulesByName.values().forEach(module ->
				                               addModuleNodeToRootIfHasChildren(root, module));
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
	
	private void addMoveToServerTeamTree(WarningContentTreeNode root){
		addTreeWithTitleAndRoot(MOVE_TO_SERVER_TEAM, root);
	}
	
	private void addMoveToServerTree(WarningContentTreeNode root){
		addTreeWithTitleAndRoot(MOVE_TO_SERVER, root);
	}
	
	private void createAndAddTreeForTypeAndRootNode(Type type, WarningContentTreeNode root) {
		addTreeWithTitleAndRoot(type.getMainAttribute(), root);
	}
	
	private void addTreeWithTitleAndRoot(String title, WarningContentTreeNode root) {
		Tree tree = new Tree(root);
		tree.setCellRenderer(new WarningContentTreeCellRender());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setToolTipText(ResourceBundle.getBundle("texts").getString("mark.unmark.description"));
		tree.setRootVisible(false);
		tree.addMouseListener(new WarningContentMouseAdapter(this.project, tree));
		tree.addKeyListener(new WarningContentKeyAdapter(tree));
		add(title, new JBScrollPane(tree));
	}
}
