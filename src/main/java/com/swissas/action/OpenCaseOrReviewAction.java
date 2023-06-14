package com.swissas.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vcs.actions.ShowAnnotateOperationsPopup;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.UpToDateLineNumberListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Action class which is opening a support case, or the review.
 * @author Tavan Alain
 */
public class OpenCaseOrReviewAction extends AnAction {
	private static final int            PATTERN_POSITION = 3;
	private final        FileAnnotation annotation;
	private              int            previousLineNumber;
	private              String         link             = null;
	private final        Pattern        searchPattern;
	private final        String         urlPrefix;
	
	public OpenCaseOrReviewAction(@NotNull FileAnnotation annotation, String text,
	                              String description, String urlPrefix, Pattern searchPattern, @Nullable Icon icon) {
		super(text, description,
		      icon);
		this.searchPattern = searchPattern;
		this.annotation = annotation;
		this.urlPrefix = urlPrefix;
	}
	
	
	@Override
	public void update(@NotNull AnActionEvent e) {
		refreshDataAndFields(ShowAnnotateOperationsPopup.getAnnotationLineNumber(e.getDataContext()));
		Presentation presentation = e.getPresentation();
		presentation.setVisible(this.link != null);
		super.update(e);
	}
	
	private void refreshDataAndFields(int lineNumber) {
		if (this.previousLineNumber != lineNumber) {
			String message = this.annotation.getHtmlToolTip(lineNumber);
			this.link = null;
			if (message != null) {
				Matcher matcher = this.searchPattern.matcher(message);
				if (matcher.find() && matcher.groupCount() == PATTERN_POSITION) {
					this.link = this.urlPrefix + matcher.group(PATTERN_POSITION);
				}
			}
			this.previousLineNumber = lineNumber;
		}
	}
	
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		if (this.link != null) {
			BrowserUtil.browse(this.link);
		}
	}
}
