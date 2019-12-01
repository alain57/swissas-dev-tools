package com.swissas.beans;

import org.jsoup.nodes.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The Bean structure for the Module
 * @author Tavan Alain
 */
public class Module extends AttributeChildrenBean{
    
    public Module(Node module) {
        super(module, "name");
    }

    public Set<File> getFiles() {
        return getChildren().stream().map(File.class::cast).collect(Collectors.toSet());
    }

    public void addFile(File file) {
        addChildren(file);
    }
}
