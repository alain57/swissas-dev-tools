package com.swissas.toolwindow;

import java.awt.Color;
import java.awt.Component;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.intellij.icons.AllIcons;
import icons.SwissAsIcons;

/**
 * The tree cell renderer for the elements displayed on the content warning content panel
 * 
 * @author Tavan Alain
 */
class WarningContentTreeCellRender extends DefaultTreeCellRenderer {
    
    
    public WarningContentTreeCellRender(){
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        WarningContentTreeNode node = (WarningContentTreeNode) value;
        Map<TextAttribute, Object>  attributes = new HashMap<>();
        attributes.put(TextAttribute.STRIKETHROUGH, node.isMarked());
        attributes.put(TextAttribute.FOREGROUND, node.isMine() ? Color.RED : Color.LIGHT_GRAY);
        attributes.put(TextAttribute.WEIGHT, node.isMine() ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
        setFont(getFont().deriveFont(attributes));
        switch (node.getTreeType()){
            case File:
                setIcon(AllIcons.FileTypes.Java);
                break;
            case Directory:
                setIcon(AllIcons.Nodes.Folder);
                break;
            default:
                setIcon(node.isCritical() ? SwissAsIcons.CRITICAL : SwissAsIcons.WARNING);
        }
        return this;
    }
}
