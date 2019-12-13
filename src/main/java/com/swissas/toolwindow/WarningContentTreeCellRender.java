package com.swissas.toolwindow;

import java.awt.Component;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

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
        setFont(getFont().deriveFont(attributes));
        if(node.getChildCount() ==0){
            setIcon(node.isCritical() ? SwissAsIcons.CRITICAL : SwissAsIcons.WARNING);
        }
        return this;
    }
}
