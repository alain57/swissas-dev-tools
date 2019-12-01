package com.swissas.beans;

import org.jsoup.nodes.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The Bean structure for the Type
 * @author Tavan Alain
 */
public class Type extends AttributeChildrenBean{
    
    public Type(Node type) {
        super(type, "name");
    }

    public Set<Module> getModules() {
        return getChildren().stream().map(Module.class::cast).collect(Collectors.toSet());
    }

    public void addModule(Module module) {
        addChildren(module);
    }
    
}
