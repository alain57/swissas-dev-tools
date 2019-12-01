package com.swissas.provider;

import java.util.regex.Pattern;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.vcs.annotate.AnnotationGutterActionProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.swissas.action.OpenCaseOrReviewAction;
import org.jetbrains.annotations.NotNull;

/**
 * The annotation provider that allows to have the "Open Review" menu in the subversion annotate gutter
 *
 * @author Tavan Alain
 */

public class OpenReviewAnnotationProvider implements AnnotationGutterActionProvider {
	private static final Pattern REVIEW_FINDER = Pattern
			.compile("review( (#|id|no))?[: ]?(\\d+)");
	
	@NotNull
	@Override
	public AnAction createAction(@NotNull FileAnnotation annotation) {
		return new OpenCaseOrReviewAction(annotation,
		                                  "Open Review",
		                                  "Display the review contained in the annotation",
		                                  "https://sas-srv-release.swiss-as.com/review_board/r/",
		                                  REVIEW_FINDER);
	}
}
