package com.swissas.beans;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;


/**
 * The bean structure for the message
 * @author Tavan Alain
 */
public class Message implements Comparable<Message> {
    private static final String CRITICAL = "critical";
    private Integer line;
    private String description;
    private String severity;

    public Integer getLine() {
        return this.line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCritical() {
        return this.severity != null && CRITICAL.equals(this.severity.toLowerCase());
    }
    
    private String getSeverity() {
        return this.severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public int compareTo(@NotNull Message other) {
        return new CompareToBuilder()
                .append(this.severity, other.getSeverity())
                .append(this.line, other.getLine())
                .append(this.description, other.getDescription())
                .toComparison();
    }
}
