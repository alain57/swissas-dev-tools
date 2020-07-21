package com.swissas.inspection;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.NotNull;

public class WebWarningInspectionProvider implements InspectionToolProvider {
	
	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	public Class<? extends LocalInspectionTool>[] getInspectionClasses() {
		return new Class[]{ WebWarningInspection.class };
	}
}
