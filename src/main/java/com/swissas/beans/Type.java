package com.swissas.beans;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The Bean structure for the Type
 * @author Tavan Alain
 */
public class Type implements Comparable<Type>{
    private String name;
    private final Set<Module> modules;
    
    public Type(){
        this.modules = new TreeSet<>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
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
