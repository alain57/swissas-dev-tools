package com.swissas.beans;

import org.jsoup.nodes.Node;

/**
 * The Bean structure for the Module
 * @author Tavan Alain
 */
public class Module extends AttributeChildrenBean{
    
    public Module(Node module) {
        super(module, "name");
    }

}
