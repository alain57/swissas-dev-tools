package com.swissas.quickfix;

import java.util.ResourceBundle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Quickfix class to mark a string as not an SQL
 *
 * @author TALA
 */

public class MarkAsNoSQLQuickFix implements LocalQuickFix {
	@NonNls
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
	
	@Nls(capitalization = Nls.Capitalization.Sentence)
	@NotNull
	@Override
	public String getFamilyName() {
		return RESOURCE_BUNDLE.getString("swiss.as");
	}
	
	@Nls(capitalization = Nls.Capitalization.Sentence)
	@NotNull
	@Override
	public String getName() {
		return RESOURCE_BUNDLE.getString("mark.as.no.sql");
	}
	
	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
		PsiElement startElement = descriptor.getPsiElement();
		PsiComment noSql = JavaPsiFacade.getInstance(project).getElementFactory().createCommentFromText("/*NOSQL*/", null);
		startElement.getParent().addAfter(noSql, startElement);
	}
}
