package com.swissas.provider;

import java.util.regex.Pattern;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.vcs.annotate.AnnotationGutterActionProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.swissas.action.OpenCaseOrReviewAction;
import icons.SwissAsIcons;
import org.jetbrains.annotations.NotNull;

/**
 * The annotation provider that allows to have the "Open Support Case" menu in the subversion annotate gutter
 *
 * @author Tavan Alain
 */

public class OpenCaseAnnotationProvider implements AnnotationGutterActionProvider {
	
	private static final Pattern SUPPORT_FINDER = Pattern
			.compile("(#|sc|case|sup|support|story|request) ?(id|no|.)?.(\\d+[`']?\\d+)", Pattern.CASE_INSENSITIVE);
	
	
	@NotNull
	@Override
	public AnAction createAction(@NotNull FileAnnotation annotation) {
		return new OpenCaseOrReviewAction(annotation, "Show Support Case",
		                                  "Show the support written in the annotation in AMOS",
		                                  "amos://SUP.",
		                                  SUPPORT_FINDER, SwissAsIcons.SUPPORT);
	}
	
	
}
