package com.swissas.inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.NotNull;

/**
 * The provider for the missing author inspection
 * @author Tavan Alain
 */
class MissingAuthorInspectionProvider implements InspectionToolProvider {
    
    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public Class<? extends LocalInspectionTool> @NotNull [] getInspectionClasses() {
        return new Class[]{MissingAuthorInspection.class};
    }
}
