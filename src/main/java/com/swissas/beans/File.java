package com.swissas.beans;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Node;

/**
 * A simple Java Bean for the file structure
 * 
 * @author Tavan Alain
 */

public class File extends AttributeChildrenBean {
    String path;
    
    public File(String fileName, Node fileElement){
        super(fileName);
        setPath(fileElement.attr("path"));
        for (Node messageNode : fileElement.childNodes()) {
            addChildren(new Message(messageNode));
        }
    }
    
    public Set<Message> getNonCriticalMessages() {
        return getChildren().stream().map(Message.class::cast).filter(Message::isCritical).collect(
                Collectors.toSet());
    }
    
   
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public int compareTo(@NotNull AttributeChildrenBean o) {
        if(o instanceof Directory) {
            return 1;
        }
        return super.compareTo(o);
    }
}
