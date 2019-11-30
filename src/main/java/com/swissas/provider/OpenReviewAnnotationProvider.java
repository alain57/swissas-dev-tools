package com.swissas.provider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vcs.annotate.AnnotationGutterActionProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.UpToDateLineNumberListener;
import icons.SwissAsIcons;
import org.jetbrains.annotations.NotNull;

/**
 * The annotation provider that allows to have the "Open Review" menu in the subversion annotate gutter
 *
 * @author Tavan Alain
 */

public class OpenReviewAnnotationProvider implements AnnotationGutterActionProvider {
	private static final Pattern REVIEW_FINDER          = Pattern
			.compile("review( (#|id|no))?[: ]?(\\d+)");
	private static final int     REVIEW_NUMBER_POSITION = 3;
	
	@NotNull
	@Override
	public AnAction createAction(@NotNull FileAnnotation annotation) {
		return new OpenReviewClickAction(annotation);
	}
	
	
	private static class OpenReviewClickAction extends AnAction implements UpToDateLineNumberListener {
		private final FileAnnotation annotation;
		private       int            lineNumber;
		private       int            previousLineNumber;
		private       String         link      = null;
		
		public OpenReviewClickAction(@NotNull FileAnnotation annotation) {
			super("Open Review", "Display the review contained in the annotation",
			      SwissAsIcons.AMOS);
			this.annotation = annotation;
			
		}
		
		@Override
		public void update(@NotNull AnActionEvent e) {
			refreshDataAndFields();
			Presentation presentation = e.getPresentation();
			presentation.setVisible(this.link != null);
			super.update(e);
		}
		
		private void refreshDataAndFields() {
			if (this.previousLineNumber != this.lineNumber) {
				String message = this.annotation.getHtmlToolTip(this.lineNumber);
				this.link = null;
				if (message != null) {
					message = message.toLowerCase();
					Matcher matcher = REVIEW_FINDER.matcher(message);
					if (matcher.find() && matcher.groupCount() == REVIEW_NUMBER_POSITION) {
						this.link = "https://sas-srv-release.swiss-as.com/review_board/r/" +
						            matcher.group(REVIEW_NUMBER_POSITION);
					}
				}
				this.previousLineNumber = this.lineNumber;
			}
		}
		
		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			if (this.link != null) {
				BrowserUtil.browse(this.link);
			}
		}
		
		@Override
		public void consume(Integer integer) {
			this.lineNumber = integer;
		}
	}
}
