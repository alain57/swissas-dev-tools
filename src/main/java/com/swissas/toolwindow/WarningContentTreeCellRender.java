package com.swissas.toolwindow;

import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
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
        super();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        WarningContentTreeNode node = (WarningContentTreeNode) value;
        Map attributes = getFont().getAttributes();
        if (node.isMarked()) {
            attributes.put(TextAttribute.STRIKETHROUGH, true);
        } else {
            attributes.put(TextAttribute.STRIKETHROUGH, false);
        }
        setFont(new Font(attributes));
        if(node.getChildCount() ==0){
            setIcon(node.isCritical() ? SwissAsIcons.CRITICAL : SwissAsIcons.WARNING);
        }
        return this;
    }
}
