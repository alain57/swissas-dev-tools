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
        while (!Character.isLetterOrDigit(s.charAt(0))) {
            s = s.substring(1);
            if (s.isEmpty()) {
                return s;
            }
        }
        return s;
    }
}
