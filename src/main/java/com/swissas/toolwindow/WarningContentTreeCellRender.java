package com.swissas.toolwindow;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * The tree cell renderer for the elements displayed on the content warning content panel
 * 
 * @author Tavan Alain
 */
class WarningContentTreeCellRender extends DefaultTreeCellRenderer {
    
    private ImageIcon warning;
    private ImageIcon critical;
    
    
    
    public WarningContentTreeCellRender(){
        super();
        try {
            this.warning = new ImageIcon(ImageIO.read(WarningContentTreeCellRender.class.getResourceAsStream("/warning.png"/*NON-NLS*/)).getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            this.critical = new ImageIcon(ImageIO.read(WarningContentTreeCellRender.class.getResourceAsStream("/critical.png"/*NON-NLS*/)).getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
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
            setIcon(node.isCritical() ? this.critical : this.warning);
        }
        return this;
    }
}
