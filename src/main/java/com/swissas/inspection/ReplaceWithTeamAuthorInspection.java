package com.swissas.inspection;

import java.util.ResourceBundle;
import java.util.stream.Stream;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.quickfix.ConvertToTeamQuickfix;
import com.swissas.util.ProjectUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;

public class ReplaceWithTeamAuthorInspection extends LocalInspectionTool {
	@Override
	public boolean isEnabledByDefault() {
		return false;
	}
	
	
	@Override
	@NotNull
	public String getGroupDisplayName() {
		return ResourceBundle.getBundle("texts").getString("swiss.as");
	}
	
	@Override
	@NotNull
	public String getDisplayName() {
		return ResourceBundle.getBundle("texts").getString("ConfigPanel.convertToTeamCheckbox.text");
	}
	
	
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		
		return new JavaElementVisitor() {
			
			@Override
			public void visitJavaFile(@NotNull PsiJavaFile file) {
				super.visitJavaFile(file);
				if (SwissAsStorage.getInstance().isConvertToTeam() &&
				    ProjectUtil.getInstance().isAmosProject(file.getProject()) &&
				    ProjectUtil.getInstance().isPreviewProject() &&
					file.getClasses().length > 0) {
					PsiDocComment docComment = PsiTreeUtil.getChildOfType(file.getClasses()[0], PsiDocComment.class);
					if (docComment != null) {
						Stream.of(docComment.getTags())
						      .filter(tag -> tag.getName().equals("author")
						              && tag.getValueElement() != null 
						              && SwissAsStorage.getInstance().getMyTeamMembers(true).contains(tag.getValueElement().getText().toUpperCase()))
						      .findFirst().ifPresent(tag -> holder.registerProblem(
									  holder.getManager().createProblemDescriptor(tag,
												  ResourceBundle.getBundle("texts")
														  .getString("ConfigPanel.convertToTeamCheckbox.text"),
												  new ConvertToTeamQuickfix(),
												  ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
												  isOnTheFly)));
					} 
				}
			}
		};
	}
}
