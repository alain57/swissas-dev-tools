package com.swissas.beans;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Node;

import java.util.*;

/**
 * The Bean structure for the Type
 * @author Tavan Alain
 */
public class Type implements Comparable<Type>{
    private String name;
    private final Set<Module> modules;
    
    public Type(Node type) {
        this.modules = new TreeSet<>();
        setName(type.attr("name"));
    }

    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Set<Module> getModules() {
        return this.modules;
    }

    public void addModule(Module module) {
        this.modules.add(module);
    }

    @Override
    public int compareTo(@NotNull Type o) {
        return new CompareToBuilder()
                .append(this.name, o.getName())
                .toComparison();
    }
}
