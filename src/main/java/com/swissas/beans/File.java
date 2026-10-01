package com.swissas.beans;

import java.util.Set;
import java.util.stream.Collectors;

import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * A simple Java Bean for the file structure
 * 
 * @author Tavan Alain
 */

public class File extends AttributeChildrenBean {
    private String path;
    private String responsible;
    
    public File(String fileName, Node fileElement){
        super(fileName);
        setPath(fileElement.attr("path"));
        setResponsible(fileElement.attr("responsible"));
        for (Node messageNode : fileElement.childNodes()) {
            //ignore the text nodes (white spaces) between the messages
            if (messageNode instanceof Element) {
                addChildren(new Message(messageNode));
            }
        }
    }
    
    public Set<Message> getNonCriticalMessages() {
        return getChildren().stream().map(Message.class::cast).filter(message -> !message.isCritical()).collect(
                Collectors.toSet());
    }
    
    public boolean isMine() {
        return SwissAsStorage.getInstance().getFourLetterCode().equalsIgnoreCase(this.responsible);
    }
    
    public String getResponsible() {
        return this.responsible;
    }
    
    public void setResponsible(String responsible) {
        this.responsible = responsible;
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
