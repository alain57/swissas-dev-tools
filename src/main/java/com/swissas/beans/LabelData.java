package com.swissas.beans;

import javax.swing.Icon;

import icons.SwissAsIcons;

/**
 * The bean structure for the labelData
 *
 * @author Tavan Alain
 */

public record LabelData(WarningType warningType, int newMessages, String name) {
    public enum WarningType {
        WARNING,
        SONAR,
        CRITICAL;

        public Icon getIcon() {
            return switch (this) {
                case SONAR -> SwissAsIcons.SONAR;
                case CRITICAL -> SwissAsIcons.CRITICAL;
                default -> SwissAsIcons.WARNING;
            };
        }
    }

}
