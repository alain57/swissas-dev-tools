package com.swissas.beans;

import org.jsoup.nodes.Node;

/**
 * A simple Java Bean for the file structure
 * 
 * @author Tavan Alain
 */

public class File extends AttributeChildrenBean {
    
    public File(Node fileElement){
        super(fileElement, "path");
    }
}
