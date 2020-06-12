package com.swissas.toolwindow;


import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The cell node for the warning Content 
 *
 * @author Tavan Alain
 */

public class WarningContentTreeNode extends DefaultMutableTreeNode {
    private static final String LINE = "Line ";
    
    public enum TreeType {
        Directory,
        File,
        Message
    }
    
    private boolean isMarked;
    private boolean isCritical;
    private boolean isErrorLine;
    private String description;
    
    private TreeType currentType;
    
    private boolean isMine = false;

    WarningContentTreeNode(String value){
        super(value);
        this.isMarked = false;
        this.isErrorLine = false;
        this.description = null;
        this.currentType = TreeType.Directory;
    }

    WarningContentTreeNode(Integer start, String description, boolean isMine){
        this(LINE + start + " : " + description);
        this.isErrorLine = true;
        this.description = description;
        this.isMine = isMine;
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

    void setCritical(boolean critical) {
        this.isCritical = critical;
    }
    
    boolean isErrorLine() {
        return this.isErrorLine;
    }
    
    String getDescription() {
        return this.description;
    }

    private void setMarked(boolean value){
        this.isMarked = value;
    }
    
    public void switchMark(){
        markNode(this, !this.isMarked);
    }
    
    private void markNode(WarningContentTreeNode node, boolean marked){
        node.setMarked(marked);
        for(int i = 0; i< node.getChildCount() ; i++){
            markNode((WarningContentTreeNode)node.getChildAt(i), marked);
        }
    }
    
    public boolean isMine() {
        int children = getChildCount();
        if (children != 0) {
        
            boolean loopBreak = false;
            for (int i = 0; i < children; i++) {
                WarningContentTreeNode currentChild = (WarningContentTreeNode) getChildAt(i);
                if (!currentChild.isMine()) {
                    loopBreak = true;
                    break;
                }
            }
            this.isMine = !loopBreak; //this means at least one child isn't marked, otherwise all are marked.
        }
        return this.isMine;
    }
    
    public boolean isMarked() {
        int children = getChildCount();
        if (children != 0) {
        
            boolean loopBreak = false;
            for (int i = 0; i < children; i++) {
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
