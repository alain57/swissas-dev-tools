package com.swissas.inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Provider of Inspection
 *
 * @author Tavan Alain
 */

public class MissingTranslationInspectionProvider implements InspectionToolProvider {

	@NotNull
	@Override
	public Class[] getInspectionClasses() {
		return new Class[]{MissingTranslationInspection.class};
	}
}
