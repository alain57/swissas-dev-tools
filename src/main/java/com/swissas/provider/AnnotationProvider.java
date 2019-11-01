package com.swissas.provider;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vcs.annotate.AnnotationGutterActionProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.UpToDateLineNumberListener;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.swissas.util.ShowLetterCodeInformation;
import icons.SwissAsIcons;
import org.jetbrains.annotations.NotNull;

/**
 * TODO: write you class description here
 *
 * @author TALA
 */

public class AnnotationProvider implements AnnotationGutterActionProvider {
	
	@NotNull
	@Override
	public AnAction createAction(@NotNull FileAnnotation annotation) {
		return new WhoIsThisRightClickAction(annotation);
	}
	
	
	private static class WhoIsThisRightClickAction extends AnAction implements UpToDateLineNumberListener {
		private final FileAnnotation annotation;
		private int lineNumber;
		
		public WhoIsThisRightClickAction(@NotNull FileAnnotation annotation){
			super("Who is This", "Display information about the letter code", SwissAsIcons.AMOS);
			
			this.annotation = annotation;
		}
		
		
		
		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			VcsRevisionNumber lineRevisionNumber = this.annotation.getLineRevisionNumber(this.lineNumber);
			String lc = this.annotation.getAuthorsMappingProvider().getAuthors().get(lineRevisionNumber);
			ShowLetterCodeInformation.displayInformation(lc, null);
		}
		
		@Override
		public void consume(Integer integer) {
			this.lineNumber = integer;
		}
	}
}
