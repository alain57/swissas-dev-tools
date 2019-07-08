package com.swissas.beans;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Node;

import java.util.*;

/**
 * The Bean structure for the Module
 * @author Tavan Alain
 */
public class Module implements Comparable<Module>{
    private String name;
    private final Set<File> files;
    
    public Module(Node module) {
        this.files = new TreeSet<>();
        setName(module.attr("name"));
    }

    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Set<File> getFiles() {
        return this.files;
    }

    public void addFile(File file) {
        this.files.add(file);
    }


    @Override
    public int compareTo(@NotNull Module o) {
        return new CompareToBuilder()
                .append(this.name, o.getName())
                .toComparison();
    }
}
