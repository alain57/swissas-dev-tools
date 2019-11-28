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
 * The annotation provider that allows to have the "Open Support Case" menu in the subversion annotate gutter
 *
 * @author Tavan Alain
 */

public class OpenCaseAnnotationProvider implements AnnotationGutterActionProvider {
	
	private static final Pattern SUPPORT_FINDER          = Pattern
			.compile("(#|sc|case|sup|support|story|request) ?(id|no)?[: ]?(\\d+[`']?\\d+)");
	private static final int     SUPPORT_NUMBER_POSITION = 3;
	
	@NotNull
	@Override
	public AnAction createAction(@NotNull FileAnnotation annotation) {
		return new OpenCaseAction(annotation);
	}
	
	
	private static class OpenCaseAction extends AnAction implements UpToDateLineNumberListener {
		private final FileAnnotation annotation;
		private       int            lineNumber;
		private       int            previousLineNumber;
		private       String         link         = null;
		
		public OpenCaseAction(@NotNull FileAnnotation annotation) {
			super("Open Support Case", "Open the support written in the annotation",
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
					Matcher matcher = SUPPORT_FINDER.matcher(message);
					if (matcher.find() && matcher.groupCount() == SUPPORT_NUMBER_POSITION) {
						this.link = "amos://SUP." + matcher.group(SUPPORT_NUMBER_POSITION);
					}
				}
				this.previousLineNumber = this.lineNumber;
			}
		}
		
		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			if(this.link != null){
				BrowserUtil.browse(this.link);
			}
		}
		
		@Override
		public void consume(Integer integer) {
			this.lineNumber = integer;
		}
	}
}
