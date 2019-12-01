package com.swissas.beans;

import org.jsoup.nodes.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A simple Java Bean for the file structure
 * 
 * @author Tavan Alain
 */

public class File extends AttributeChildrenBean {
    
    public File(Node fileElement){
        super(fileElement, "path");
    }

    public Set<Message> getMessages() {
        return getChildren().stream().map(Message.class::cast).collect(Collectors.toSet());
    }

    public void addMessage(Message message) {
        addChildren(message);
    }

    
}
