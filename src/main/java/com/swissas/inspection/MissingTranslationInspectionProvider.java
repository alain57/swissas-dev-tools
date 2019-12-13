package com.swissas.inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.NotNull;

/**
 * Provider of Inspection
 *
 * @author Tavan Alain
 */

class MissingTranslationInspectionProvider implements InspectionToolProvider {
	
	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	public Class<? extends LocalInspectionTool>[] getInspectionClasses() {
		return new Class[]{MissingTranslationInspection.class};
	}
}
