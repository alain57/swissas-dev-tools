package com.swissas.inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.NotNull;

/**
 * The provider for replacing the author tag with the team name
 * @author Tavan Alain
 */
class ReplaceWithTeamAuthorInspectionProvider implements InspectionToolProvider {
	
	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	public Class<? extends LocalInspectionTool> @NotNull [] getInspectionClasses() {
		return new Class[]{ ReplaceWithTeamAuthorInspection.class };
	}
}
