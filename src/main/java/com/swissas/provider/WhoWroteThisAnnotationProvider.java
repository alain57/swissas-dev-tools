package com.swissas.provider;

import java.util.Objects;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vcs.actions.ShowAnnotateOperationsPopup;
import com.intellij.openapi.vcs.annotate.AnnotationGutterActionProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.UpToDateLineNumberListener;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.swissas.util.ShowLetterCodeInformationHelper;
import icons.SwissAsIcons;
import org.jetbrains.annotations.NotNull;

/**
 * The annotation provider that allows to have the "who wrote this" menu in the subversion annotate gutter
 *
 * @author Tavan Alain
 */

public class WhoWroteThisAnnotationProvider implements AnnotationGutterActionProvider {
	
	@NotNull
	@Override
	public AnAction createAction(@NotNull FileAnnotation annotation) {
		return new WhoIsThisRightClickAction(annotation);
	}
	
	
	private static class WhoIsThisRightClickAction extends AnAction {
		private final FileAnnotation annotation;
		
		public WhoIsThisRightClickAction(@NotNull FileAnnotation annotation){
			super("Who Wrote This", "Display information about the letter code", SwissAsIcons.AUTHOR);
			
			this.annotation = annotation;
		}
		
		
		
		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			int lineNumber = ShowAnnotateOperationsPopup.getAnnotationLineNumber(e.getDataContext());
			VcsRevisionNumber lineRevisionNumber = this.annotation.getLineRevisionNumber(lineNumber);
			String lc = Objects.requireNonNull(this.annotation.getAuthorsMappingProvider())
			                   .getAuthors().get(lineRevisionNumber);
			ShowLetterCodeInformationHelper.displayInformation(lc, null);
		}
	}
}
