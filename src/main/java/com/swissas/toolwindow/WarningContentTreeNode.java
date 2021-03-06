package com.swissas.toolwindow;


import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * The cell node for the warning Content 
 *
 * @author Tavan Alain
 */

public class WarningContentTreeNode extends DefaultMutableTreeNode {
    
    public enum TreeType {
        DIRECTORY, FILE, MESSAGE
    }
    
    private boolean isMarked;
    private boolean isCritical;
    private TreeType currentType;
    
    private boolean isMine = false;

    WarningContentTreeNode(String value){
        super(value);
        this.isMarked = false;
        this.currentType = TreeType.DIRECTORY;
    }
    
    public TreeType getTreeType() {
        return this.currentType;
    }
    
    public void setCurrentType(TreeType currentType) {
        this.currentType = currentType;
    }
    
    boolean isCritical() {
        return this.isCritical;
    }

    private void setMarked(boolean value){
        this.isMarked = value;
    }
    
    public void switchMark(){
        markNode(this, !this.isMarked);
    }
    
    private void markNode(WarningContentTreeNode node, boolean marked){
        node.setMarked(marked);
        for(var i = 0; i< node.getChildCount() ; i++){
            markNode((WarningContentTreeNode)node.getChildAt(i), marked);
        }
        
    }
    
    public boolean isMine() {
        int children = getChildCount();
        if (children != 0) {
            for (var i = 0; i < children; i++) {
                WarningContentTreeNode currentChild = (WarningContentTreeNode) getChildAt(i);
                if (currentChild.isMine()) {
                    return true;
                }
            }
        }
        return this.isMine;
    }
    
    public void setMine(boolean mine) {
        this.isMine = mine;
    }
    
    public int getUnmarkedCount() {
        var count = 0;
    
        WarningContentTreeNode node;
        Enumeration<TreeNode> enumeration = breadthFirstEnumeration(); // order matters not
    
        while (enumeration.hasMoreElements()) {
            node = (WarningContentTreeNode)enumeration.nextElement();
            if (node.isLeaf() && !node.isMarked()) {
                count++;
            }
        }
        return count;
    }
    public boolean isMarked() {
        int children = getChildCount();
        if (children != 0) {
        
            var loopBreak = false;
            for (var i = 0; i < children; i++) {
                WarningContentTreeNode currentChild = (WarningContentTreeNode) getChildAt(i);
                if (!currentChild.isMarked()) {
                    loopBreak = true;
                    break;
                }
            }
            this.isMarked = !loopBreak; //this means at least one child isn't marked, otherwise all are marked.
        }
        return this.isMarked;
    }
}
