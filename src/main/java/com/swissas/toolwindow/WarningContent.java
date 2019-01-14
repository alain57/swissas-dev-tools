package com.swissas.toolwindow;

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
import com.swissas.beans.File;
import com.swissas.beans.Message;
import com.swissas.beans.Module;
import com.swissas.beans.Type;
import com.swissas.util.Storage;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.*;


/**
 * The warning content panel where all the different code warning coming from the company will be displayed
 *
 * @author Tavan Alain
 */

public class WarningContent extends JTabbedPane implements ToolWindowFactory {

    private static final String TYPE = "type";
    private static final String MODULE = "module";
    private static final String FILE = "file";
    private static final String MESSAGE = "message";
    private static final String WARNING_CONTENT_TIMER = "WarningContentTimer";
    private static final String CODE_CHECK = "code check";
    private static final String ROOT = "Root";
    
    private static final String NAME = "name";
    private static final String PATH = "path";
    private static final String COMPILER = "compiler";
    private static final String SONAR = "sonar";
    private static final String LINE_WITHOUT_SPACE = "line";
    private static final String DESCRIPTION = "description";
    private static final String SEVERITY = "severity";
    private static final String CRITICAL = "critical";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
    private static final ResourceBundle URL_BUNDLE = ResourceBundle.getBundle("urls");

    private static final String MESSAGE_URL = URL_BUNDLE.getString("url.warnings");
    public static final String ID = "SAS Warnings";
    
    private InputStream body;
    private List<Type> types = new ArrayList<>();
    
    private final criticalActionToggle criticalActionToggle;
    private Storage storage;
    
    public WarningContent(){
        super();
        this.criticalActionToggle = new criticalActionToggle();
        this.criticalActionToggle.setWarningContent(this);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ((ToolWindowEx) toolWindow).setTitleActions(this.criticalActionToggle);
        this.storage = Storage.getStorageFromProject(project);
        
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
    }
    
    
    public void refresh() {
        try {
            int selectedTab = this.getSelectedIndex() == -1 ? 0 : this.getSelectedIndex() ;
            readURL();
            fillView();
            if(this.getTabCount() > selectedTab) {
                this.setSelectedIndex(selectedTab);
            }
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
    }
    
    private void readURL() throws IOException, XMLStreamException {
        if(!this.storage.getFourLetterCode().isEmpty()){
            HttpClient client = new HttpClient();
            GetMethod get = new GetMethod(MESSAGE_URL + this.storage.getFourLetterCode());
            client.executeMethod(get);
            this.body = get.getResponseBodyAsStream();
            this.types = parseXml();
            get.releaseConnection();
        }
    }

    private void fillView()  {
        removeAll();
        if(!this.types.isEmpty()){
            for(Type type : this.types){
                if(!type.getName().toLowerCase().equals(CODE_CHECK)) { //code check is not used in the eclispe plugin, so don't even show it in IntelliJ !
                    WarningContentTreeNode root = new WarningContentTreeNode(ROOT);
                    for(Module module : type.getModules()){
                        int messageCount = 0;
                        WarningContentTreeNode moduleNode =  new WarningContentTreeNode("");
                        for(File file : module.getFiles()){
                            WarningContentTreeNode fileNode = new WarningContentTreeNode("");
                            for(Message message : file.getMessages()){
                                WarningContentTreeNode messageNode = new WarningContentTreeNode(message.getLine(), message.getDescription());
                                messageNode.setCritical(message.isCritical());
                                fileNode.add(messageNode);
                            }
                            if(fileNode.getChildCount() > 0) {
                                fileNode.setUserObject(file.getPath() + " (" + fileNode.getChildCount() + ")");
                                moduleNode.add(fileNode);
                                messageCount += fileNode.getChildCount();
                            }
                        }
                        if(moduleNode.getChildCount() > 0) {
                            moduleNode.setUserObject(module.getName() + " (" + messageCount + ")");
                            root.add(moduleNode);
                        }
                    }
                    createAndAddTreeForTypeAndRootNode(type, root);
                }
            }
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
                WarningContentTreeNode selectedNode = (WarningContentTreeNode)tree.getLastSelectedPathComponent();
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && tree.getSelectionPath() != null && tree.getSelectionPath().getPath().length > 3){
                    Project p  = DataManager.getInstance().getDataContext(WarningContent.this).getData(PlatformDataKeys.PROJECT);
                    if(p != null) {
                        Object[] path = tree.getSelectionPath().getPath();
                        int line = 0;
                        if (path.length > 3) {
                            String lineAndDesc = path[3].toString();
                            line = Integer.valueOf(lineAndDesc.substring(5, lineAndDesc.indexOf(" :"))) - 1;
                        }
                        String pathAndAmountOfErrors = path[2].toString();
                        String filepath = p.getBasePath() + "/" + pathAndAmountOfErrors.substring(0, pathAndAmountOfErrors.indexOf(" ("));
                        VirtualFile file = VfsUtil.findFileByIoFile(new java.io.File(filepath), true);
                        if(file == null) {
                            JOptionPane.showMessageDialog(null, RESOURCE_BUNDLE.getString("opening.following.file.is.not.working") + filepath, RESOURCE_BUNDLE.getString("error"), JOptionPane.ERROR_MESSAGE);
                        }else{
                            new OpenFileDescriptor(p, file, line, 0).navigate(true);
                        }
                    }
                }else if(e.getButton() == MouseEvent.BUTTON3){
                    if(selectedNode != null) {
                        displayMenuForSelectedNodeOnPosition(selectedNode, e.getX(), e.getY());
                    }
                }else if(e.getButton() == MouseEvent.BUTTON2) {
                    selectedNode.switchMark();
                }
            }

            private void displayMenuForSelectedNodeOnPosition(WarningContentTreeNode selectedNode, int x, int y) {
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem markDone = new JMenuItem(selectedNode.isMarked() ? "reset mark as done" : "mark as done");
                markDone.addActionListener(event -> selectedNode.switchMark());
                popupMenu.add(markDone);
                if(selectedNode.isErrorLine()) {
                    JMenuItem markIgnore = new JMenuItem("ignore all \"" +selectedNode.getDescription() + "\"");
                    markIgnore.addActionListener(event -> markIgnore(selectedNode));
                    popupMenu.add(markIgnore);
                }
                popupMenu.show(tree, x, y);
            }
        });
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                WarningContentTreeNode selectedNode = (WarningContentTreeNode)tree.getLastSelectedPathComponent();
                if(selectedNode != null && e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
                    selectedNode.switchMark();
                }
            }
        });
        add(type.getName(), new JBScrollPane(tree));
    }
    
    private void markIgnore(WarningContentTreeNode node){
        node.getDescription();
    }


    private List<Type> parseXml() throws XMLStreamException {
        File currentFile = null;
        Module currentModule = null;
        Type currentType = null;

        List<Type> result = new ArrayList<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(this.body);
        while (reader.hasNext()) {
            int event = reader.next();
            String elementName;
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                     elementName = reader.getLocalName();
                    if (TYPE.equals(elementName)) {
                        currentType = new Type();
                        currentType.setName(reader.getAttributeValue(null, NAME));
                    } else if (MODULE.equals(elementName)) {
                        currentModule = new Module();
                        currentModule.setName(reader.getAttributeValue(null, NAME));
                    } else if (FILE.equals(elementName)) {
                        currentFile = new File();
                        currentFile.setPath(reader.getAttributeValue(null, PATH));
                    } else if (MESSAGE.equals(elementName)) {
                        if(!this.criticalActionToggle.isCriticalOnly() ||
                                (this.criticalActionToggle.isCriticalOnly() &&
                                        (currentType.getName().toLowerCase().equals(COMPILER) ||
                                                (currentType.getName().toLowerCase().equals(SONAR) && reader.getAttributeValue(null, SEVERITY).toLowerCase().equals(CRITICAL))))){
                            Message currentMessage = new Message();
                            currentMessage.setLine(Integer.valueOf(reader.getAttributeValue(null, LINE_WITHOUT_SPACE)));
                            currentMessage.setDescription(reader.getAttributeValue(null, DESCRIPTION));
                            currentMessage.setSeverity(reader.getAttributeValue(null, SEVERITY));
                            currentFile.addMessage(currentMessage);
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    elementName = reader.getLocalName();
                    if (FILE.equals(elementName)) {
                        currentModule.addFile(currentFile);
                    } else if (MODULE.equals(elementName)) {
                        currentType.addModule(currentModule);
                    } else if (TYPE.equals(elementName)) {
                        result.add(currentType);
                    }
                    break;
            }
        }
        return result;
    }
}
