package com.swissas.toolwindow;

import com.intellij.openapi.util.text.NaturalComparator;

import java.util.Comparator;

public final class WarningContentTreeComparator {
    public static final Comparator<WarningContentTreeNode> INSTANCE =
            Comparator.comparing(n -> getDisplayTextToSort(n.getUserObject().toString()), NaturalComparator.INSTANCE);

    public static String getDisplayTextToSort(String s) {
        if (s.isEmpty()) {
            return s;
        }
        String workingString = s;
        while (!Character.isLetterOrDigit(workingString.charAt(0))) {
            workingString = workingString.substring(1);
            if (workingString.isEmpty()) {
                return workingString;
            }
        }
        return workingString;
    }
}
