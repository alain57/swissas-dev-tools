package com.swissas.beans;

import org.jsoup.nodes.Node;

/**
 * The Bean structure for the Type
 * @author Tavan Alain
 */
public class Type extends AttributeChildrenBean{
    
    public Type(Node type) {
        super(type, "name");
    }
}
