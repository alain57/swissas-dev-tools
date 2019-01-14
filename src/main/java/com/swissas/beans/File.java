package com.swissas.beans;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A simple Java Bean for the file structure
 * 
 * @author Tavan Alain
 */

public class File implements Comparable<File>{
    private String path;
    private final Set<Message> messages;
    
    public File(){
        this.messages = new TreeSet<>();
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<Message> getMessages() {
        return this.messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }


    @Override
    public int compareTo(@NotNull File o) {
        return new CompareToBuilder()
                .append(this.path, o.getPath())
                .toComparison();
    }
}
