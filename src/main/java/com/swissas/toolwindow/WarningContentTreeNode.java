package com.swissas.toolwindow;


import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The cell node for the warning Content 
 *
 * @author Tavan Alain
 */

class WarningContentTreeNode extends DefaultMutableTreeNode {
    private static final String LINE = "Line ";
    
    private boolean isMarked;
    private boolean isCritical;
    private boolean isErrorLine;
    private String description;

    WarningContentTreeNode(String value){
        super(value);
        this.isMarked = false;
        this.isErrorLine = false;
        this.description = null;
    }

    WarningContentTreeNode(Integer start, String description){
        this(LINE + start + " : " + description);
        this.isErrorLine = true;
        this.description = description;
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
    
    void switchMark(){
        markNode(this, !this.isMarked);
    }
    
    private void markNode(WarningContentTreeNode node, boolean marked){
        node.setMarked(marked);
        for(int i = 0; i< node.getChildCount() ; i++){
            markNode((WarningContentTreeNode)node.getChildAt(i), marked);
        }
    }
    
    boolean isMarked() {
        int children = getChildCount();
        if(children == 0) {
            return this.isMarked;
        }else {
            
            boolean loopBreaked = false;
            for(int i = 0 ; i< children; i++){
                WarningContentTreeNode currentChild = (WarningContentTreeNode)getChildAt(i);
                if(!currentChild.isMarked()){
                    loopBreaked = true;
                    break;
                }
            }
            this.isMarked = !loopBreaked; //this means at least one child is not marked, otherwise all are marked.
            return this.isMarked;
        }
    }
}
