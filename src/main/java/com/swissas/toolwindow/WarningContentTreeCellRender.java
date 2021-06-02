package com.swissas.toolwindow;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import icons.SwissAsIcons;

/**
 * The tree cell renderer for the elements displayed on the content warning content panel
 * 
 * @author Tavan Alain
 */
class WarningContentTreeCellRender extends DefaultTreeCellRenderer {
    
    private boolean myTasksInBold;
    
    public WarningContentTreeCellRender(boolean myTasksInBold){
        this.myTasksInBold = myTasksInBold;
    }

    public void setMyTasksInBold(boolean value) {
        this.myTasksInBold = value;
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
        JPanel p = new JBPanel<>(layout);
        JBLabel text = new JBLabel();
        WarningContentTreeNode node = (WarningContentTreeNode) value;
        Map<TextAttribute, Object>  attributes = new HashMap<>();
        attributes.put(TextAttribute.STRIKETHROUGH, node.isMarked());
        attributes.put(TextAttribute.WEIGHT, this.myTasksInBold && node.isMine() ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
        attributes.put(TextAttribute.FOREGROUND, this.myTasksInBold && node.isMine() ? Color.RED : Color.LIGHT_GRAY);
        text.setFont(text.getFont().deriveFont(attributes));
        text.setText(node.getUserObject().toString());
        p.add(text);
        JBLabel amount;
        switch (node.getTreeType()) {
            case FILE:
                text.setIcon(AllIcons.FileTypes.Java);
                amount = new JBLabel("( " + node.getUnmarkedCount() + ")");
                p.add(amount);
                break;
            case DIRECTORY:
                text.setIcon(AllIcons.Nodes.Folder);
                amount = new JBLabel("( " + node.getUnmarkedCount() + ")");
                p.add(amount);
                break;
            default:
                text.setIcon(node.isCritical() ? SwissAsIcons.CRITICAL : SwissAsIcons.WARNING);
                break;
        }
        return p;
    }
}
