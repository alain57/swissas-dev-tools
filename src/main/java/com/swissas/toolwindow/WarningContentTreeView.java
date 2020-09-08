package com.swissas.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.swissas.action.CriticalActionToggle;
import com.swissas.beans.AttributeChildrenBean;
import com.swissas.beans.Type;
import com.swissas.toolwindow.adapters.WarningContentKeyAdapter;
import com.swissas.toolwindow.adapters.WarningContentMouseAdapter;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Warning Content UI Panel that contains the tree but also a left and top panel to make the tree more
 * user friendly
 *
 *  @author Tavan Alain
 */
public class WarningContentTreeView extends JPanel {
    private static final String ROOT                  = "Root";
    private static final CommonActionsManager ACTIONS_MANAGER = CommonActionsManager.getInstance();
    private final Tree tree;
    private final ComboBox<String> userChoice = new ComboBox<>();
    private final WarningContentTreeCellRender cellRenderer;
    private final CriticalActionToggle criticalActionToggle;
    private final Project project;
    private final Type type;
    private final WarningContentTreeNode rootNode;
    private final WarningContent delegate;
    private String name;
    private final String filterSimilar;

    public WarningContentTreeView(@NotNull Project project, @NotNull Type type, @NotNull WarningContent delegate, @Nullable String filterSimilar) {
        this.project = project;
        this.type = type;
        this.delegate = delegate;
        this.filterSimilar = filterSimilar;
        this.cellRenderer = new WarningContentTreeCellRender(true);
        this.criticalActionToggle = new CriticalActionToggle();
        this.criticalActionToggle.setWarningContentTreeView(this);
        this.rootNode = new WarningContentTreeNode(ROOT);
        this.tree = new Tree(this.rootNode);
        buildUI();
    }

    public void removeSelfFromDelegate() {
        this.delegate.remove(this);
        
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        JComponent topActionBar = createTopActionBar();//needs to be done before the tree !
        buildTree();
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(this.tree, SideBorder.LEFT);
        JComponent leftActionsToolbar = createLeftActionsToolbar();
        JPanel borderLayoutPanel = JBUI.Panels.simplePanel()
                .addToLeft(leftActionsToolbar)
                .addToCenter(scrollPane)
                .addToTop(topActionBar);
        add(borderLayoutPanel, BorderLayout.CENTER);
        new TreeSpeedSearch(this.tree, e -> WarningContentTreeComparator.getDisplayTextToSort(e.getLastPathComponent().toString()), true);
    }

    private void buildTree() {
        this.name = this.type.getMainAttribute();
        refreshTree();
        this.tree.setCellRenderer(this.cellRenderer);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.tree.setToolTipText(ResourceBundle.getBundle("texts").getString("mark.unmark.description"));
        this.tree.setRootVisible(false);
        this.tree.addMouseListener(new WarningContentMouseAdapter(this.project, this.tree, this.delegate));
        this.tree.addKeyListener(new WarningContentKeyAdapter(this.tree));
    }

    public void refreshTree() {
        this.rootNode.removeAllChildren();
        for(AttributeChildrenBean child : this.type.getChildren()) {
            WarningContentHelper.fillTreeWithChildren(this.rootNode, child, (String)this.userChoice.getSelectedItem(),
                    isSonarTab() && this.criticalActionToggle.isCriticalOnly(), this.filterSimilar);
        }
        ((DefaultTreeModel)this.tree.getModel()).reload();

    }

    private boolean isSonarTab() {
        return "sonar".equalsIgnoreCase(this.name);
    }

    private JComponent createLeftActionsToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        final TreeExpander treeExpander = new DefaultTreeExpander(this.tree);
        group.add(ACTIONS_MANAGER.createExpandAllAction(treeExpander, this.tree));
        group.add(ACTIONS_MANAGER.createCollapseAllAction(treeExpander, this.tree));
        if(isSonarTab()){
            group.add(this.criticalActionToggle);
        }
        return createToolbar(group, false);
    }

    private JComponent createTopActionBar() {
        this.userChoice.addItem(SwissAsStorage.getInstance().getMyTeam());
        SwissAsStorage.getInstance().getMyTeamMembers(true, false).forEach(this.userChoice::addItem);
        this.userChoice.addActionListener(e -> {
            this.cellRenderer.setMyTasksInBold(SwissAsStorage.getInstance().getMyTeam().equals(this.userChoice.getSelectedItem()));
            refreshTree();
        });
        this.userChoice.setMaximumSize(this.userChoice.getPreferredSize() );
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        content.add(new JBLabel("Filter Messages for "));
        content.add(this.userChoice);
        if(this.filterSimilar != null) {
            JButton closeButton = getCloseButton();
            content.add(closeButton);
        }
        return content;
    }

    @NotNull
    private JButton getCloseButton() {
        JButton closeButton = new JButton(AllIcons.Actions.Close);
        closeButton.setMaximumSize(new Dimension(32, 32));
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setToolTipText("Click to close the current Tab");
        closeButton.addActionListener(e -> removeSelfFromDelegate());
        return closeButton;
    }

    private JComponent createToolbar(final DefaultActionGroup specialGroup, boolean horizontal) {
        final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(this.name, specialGroup, horizontal);
        return toolbar.getComponent();
    }
}
