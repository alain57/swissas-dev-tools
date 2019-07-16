package com.swissas.inspection;

import java.util.*;
import java.util.regex.Pattern;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.quickfix.TranslateMakAsIgnoreQuickFix;
import com.swissas.quickfix.TranslateQuickFix;
import com.swissas.quickfix.TranslateTooltipQuickFix;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * The inspection  for missing translation
 *
 * @author Tavan Alain
 */

class MissingTranslationInspection extends LocalInspectionTool {

	@NonNls
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

	@Override
	@NotNull
	public String getDisplayName() {
		return RESOURCE_BUNDLE.getString("missing.translation");
	}

	@Override
	@NotNull
	public String getGroupDisplayName() {
		return RESOURCE_BUNDLE.getString("swiss.as");
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		LocalQuickFix ignoreFix = new TranslateMakAsIgnoreQuickFix();
		LocalQuickFix translateFix = new TranslateQuickFix(holder.getFile());
		LocalQuickFix translateTooltipFix = new TranslateTooltipQuickFix(holder.getFile());
		
		LocalQuickFix[] fixes = SwissAsStorage.getInstance(holder.getProject()).isNewTranslation() ?new LocalQuickFix[]{ignoreFix, translateFix, translateTooltipFix} : new LocalQuickFix[]{ignoreFix};
		int minSize = Integer.valueOf(SwissAsStorage.getInstance(holder.getProject()).getMinWarningSize()) + 2; //psiStringElements are withing double quotes
		Pattern sqlAndTemplate = Pattern.compile(".*(DELETE|INSERT|UPDATE|SELECT).*|\\.tpl$");
		Pattern isInMethods = Pattern.compile(".*(Exception|getLogger\\(\\)|assertEquals).*|WithHistory$");
		if(holder.getFile().getName().endsWith("Test.java")){
			return super.buildVisitor(holder, isOnTheFly);
		}
		
		return new JavaElementVisitor() {

			@Override
			public void visitLiteralExpression(PsiLiteralExpression expression) {
				super.visitLiteralExpression(expression);
				if(expression.getTextLength() > minSize && 
						(expression.getValue() instanceof String) &&
						!sqlAndTemplate.matcher((String) expression.getValue()).matches() &&
						hasNoNoExtAsNextSibling(expression)
				) {
					PsiElement parent = expression.getParent();
					if ((parent instanceof PsiField || parent instanceof PsiLocalVariable || parent instanceof PsiReferenceExpression) && hasNoNoExtAsNextSibling(parent)) {
							holder.registerProblem(expression, RESOURCE_BUNDLE.getString("missing.translation"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixes);
					} else if(parent instanceof PsiExpressionList && hasNoNoExtAsNextSibling(parent)){
						PsiElement parentPrevSibling = parent.getPrevSibling();
						if(!isInMethods.matcher(parentPrevSibling.getText()).matches()){
							holder.registerProblem(expression, RESOURCE_BUNDLE.getString("missing.translation"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixes);
						}
					} else if(parent instanceof PsiPolyadicExpression && hasNoNoExtAsNextSibling(parent)) {
						PsiElement parentPrevSibling = parent.getPrevSibling();
						if(!isInMethods.matcher(parentPrevSibling.getText()).matches()){
							holder.registerProblem(parent, RESOURCE_BUNDLE.getString("missing.translation"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixes);
						}
					}
				}
			}
			
			@NotNull
			private Boolean hasNoNoExtAsNextSibling(PsiElement expression) {
				return Optional.ofNullable(PsiTreeUtil.getNextSiblingOfType(expression, PsiComment.class)).
						map(doc -> !doc.getText().contains("NO_EXT")).orElse(true);
			}
			
		};
	}
	
}
