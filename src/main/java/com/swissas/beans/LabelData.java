package com.swissas.beans;

import javax.swing.Icon;

import icons.SwissAsIcons;

/**
 * The bean structure for the labelData
 *
 * @author Tavan Alain
 */

public class LabelData {
    public enum WarningType{
        WARNING,
        SONAR,
        CRITICAL;
        
        public Icon getIcon(){
            switch (this){
                case SONAR:
                    return SwissAsIcons.SONAR;
                case CRITICAL:
                    return SwissAsIcons.CRITICAL;
                default:
                    return SwissAsIcons.WARNING;
                    
            }
        }
    }
    
    private final WarningType warningType;
    private final int newMessages;
    private final String name;
    
    public LabelData(WarningType warningType, int newMessages, String name){
        this.warningType = warningType;
        this.newMessages = newMessages;
        this.name = name;
    }

    public WarningType getWarningType() {
        return this.warningType;
    }

    public int getNewMessages() {
        return this.newMessages;
    }

    public String getName() {
        return this.name;
    }
}
