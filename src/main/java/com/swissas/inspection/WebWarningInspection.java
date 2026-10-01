package com.swissas.inspection;

import java.util.Optional;
import java.util.ResourceBundle;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.PsiFile;
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
		VirtualFile virtualFile = holder.getFile().getVirtualFile();
		if(virtualFile != null && Optional.ofNullable(ProjectRootManager.getInstance(holder.getProject()).getFileIndex()
		                     .getModuleForFile(virtualFile)).map(Module::getName)
				.filter(name -> name.contains("amos_web")).isPresent()){
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
		public void visitTypeElement(@NotNull PsiTypeElement expression) {
			super.visitTypeElement(expression);
			PsiElement firstChild = expression.getFirstChild();
			if(firstChild instanceof PsiJavaCodeReferenceElement reference) {
				registerProblem(expression, reference.resolve());
			}
		}
		
		@Override
		public void visitReferenceExpression(@NotNull PsiReferenceExpression expression) {
			super.visitReferenceExpression(expression);
			registerProblem(expression, expression.resolve());
		}
		
		private void registerProblem(PsiElement expression, PsiElement resolvedElement) {
			PsiFile resolvedFile = resolvedElement == null ? null : resolvedElement.getContainingFile();
			VirtualFile resolvedVirtualFile = resolvedFile == null ? null : resolvedFile.getVirtualFile();
			String moduleName = resolvedVirtualFile == null ? "" :
			                    Optional.ofNullable(ProjectRootManager.getInstance(resolvedElement.getProject()))
			        .map(ProjectRootManager::getFileIndex)
			        .map(e -> e.getModuleForFile(resolvedVirtualFile))
			        .map(Module::getName)
			        .orElse("");
			if (moduleName.contains("amos_server")) {
				this.holder.registerProblem(expression, "Discourage access, prefer using amos.api()",
				                            ProblemHighlightType.LIKE_MARKED_FOR_REMOVAL);
			}
		}
	}
}
