package com.swissas.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.UpToDateLineNumberListener;
import icons.SwissAsIcons;
import org.jetbrains.annotations.NotNull;

/**
 * Action class which is opening a support case, or the review.
 * @author Tavan Alain
 */
public class OpenCaseOrReviewAction extends AnAction implements UpToDateLineNumberListener {
	private static final int            PATTERN_POSITION = 3;
	private final        FileAnnotation annotation;
	private              int            lineNumber;
	private              int            previousLineNumber;
	private              String         link             = null;
	private final        Pattern        searchPattern;
	private final        String         urlPrefix;
	
	public OpenCaseOrReviewAction(@NotNull FileAnnotation annotation, String text,
	                              String description, String urlPrefix, Pattern searchPattern) {
		super(text, description,
		      SwissAsIcons.AMOS);
		this.searchPattern = searchPattern;
		this.annotation = annotation;
		this.urlPrefix = urlPrefix;
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
				Matcher matcher = this.searchPattern.matcher(message);
				if (matcher.find() && matcher.groupCount() == PATTERN_POSITION) {
					this.link = this.urlPrefix + matcher.group(PATTERN_POSITION);
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
