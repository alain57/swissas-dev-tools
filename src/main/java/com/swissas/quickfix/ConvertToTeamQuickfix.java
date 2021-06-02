package com.swissas.quickfix;

import java.util.ResourceBundle;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.javadoc.PsiDocTag;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ConvertToTeamQuickfix implements LocalQuickFix {
	
	
	@Nls(capitalization = Nls.Capitalization.Sentence)
	@NotNull
	@Override
	public String getFamilyName() {
		return ResourceBundle.getBundle("texts").getString("ConfigPanel.convertToTeamCheckbox.text");
	}
	
	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
		PsiDocTag tag = (PsiDocTag)descriptor.getPsiElement();
		if(tag != null) {
		PsiDocTag newAuthorTag = JavaPsiFacade.getElementFactory(project).createDocTagFromText(
				"@author " + SwissAsStorage.getInstance().getMyTeam());
			tag.getParent().addBefore(newAuthorTag, tag);
			tag.delete();
		}
	}
}
