package com.swissas.inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import org.jetbrains.annotations.NotNull;

/**
 * The provider for the missing author inspection
 * @author Tavan Alain
 */
class MissingAuthorInspectionProvider implements InspectionToolProvider {

    @NotNull
    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{MissingAuthorInspection.class};
    }
}
