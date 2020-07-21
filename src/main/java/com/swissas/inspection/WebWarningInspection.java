package com.swissas.inspection;

import java.util.ResourceBundle;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl;
import org.jetbrains.annotations.NotNull;

public class WebWarningInspection extends LocalInspectionTool {
	
	@Override
	public boolean isEnabledByDefault() {
		return true;
	}
	
	@Override
	@NotNull
	public String getDisplayName() {
		return ResourceBundle.getBundle("texts").getString("discourage.access");
	}
	
	@Override
	@NotNull
	public String getGroupDisplayName() {
		return ResourceBundle.getBundle("texts").getString("swiss.as");
	}
	
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		if(ProjectRootManager.getInstance(holder.getProject()).getFileIndex()
		                     .getModuleForFile(holder.getFile().getVirtualFile()).getName().contains("amos_web")){
			return new WebElementVisitor(holder);
		}
		return super.buildVisitor(holder, isOnTheFly);
	}
	
	static class WebElementVisitor extends JavaElementVisitor {
		private final ProblemsHolder  holder;
		
		public WebElementVisitor(
				ProblemsHolder holder) {
			this.holder = holder;
		}
		
		@Override
		public void visitTypeElement(PsiTypeElement expression) {
			super.visitTypeElement(expression);
			PsiElement firstChild = expression.getFirstChild();
			if(firstChild instanceof PsiJavaCodeReferenceElement) {
				PsiElement element = ((PsiJavaCodeReferenceElementImpl) firstChild).resolve();
				registerProblem(expression, element);
			}
		}
		
		@Override
		public void visitReferenceExpression(PsiReferenceExpression expression) {
			super.visitReferenceExpression(expression);
			registerProblem(expression, expression.resolve());
		}
		
		private void registerProblem(PsiElement expression, PsiElement resolvedElement) {
			Module module = ProjectRootManager.getInstance(resolvedElement.getProject()).getFileIndex()
			                                  .getModuleForFile(
					                                  resolvedElement.getContainingFile()
					                                         .getVirtualFile());
			if (module != null && module.getName().contains("amos_server")) {
				this.holder.registerProblem(expression, "Discourage access, prefer using amos.api()",
				                            ProblemHighlightType.LIKE_MARKED_FOR_REMOVAL);
			}
		}
	}
}
