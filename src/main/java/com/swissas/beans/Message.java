package com.swissas.beans;

import java.util.Objects;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Node;


/**
 * The bean structure for the message
 * @author Tavan Alain
 */
public class Message extends AttributeChildrenBean{
    private static final String CRITICAL = "critical";
    private Integer line;
    private String description;
    
    private boolean isMine;

    public Message(Node message){
        this(message, false);
    }
    
    public Message(Node message, boolean isMine) {
        super(message, "severity");
        setLine(Integer.valueOf(message.attr("line")));
        setDescription(message.attr("description"));
        setMine(isMine);
    }
    
    public boolean isMine() {
        return this.isMine;
    }
    
    public void setMine(boolean mine) {
        this.isMine = mine;
    }
    
    public Integer getLine() {
        return this.line;
    }

    private void setLine(Integer line) {
        this.line = line;
    }

    public String getDescription() {
        return this.description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public boolean isCritical() {
        return getMainAttribute() != null && CRITICAL.equalsIgnoreCase(getMainAttribute());
    }

    @Override
    public int compareTo(@NotNull AttributeChildrenBean other) {
        Message otherMessage = (Message)other;
        return new CompareToBuilder()
                .append(getMainAttribute(), otherMessage.getMainAttribute())
                .append(this.line, otherMessage.getLine())
                .append(this.description, otherMessage.getDescription())
                .toComparison();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Message message = (Message) o;
        return Objects.equals(this.line, message.line) &&
               Objects.equals(this.description, message.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.line, this.description);
    }
}
