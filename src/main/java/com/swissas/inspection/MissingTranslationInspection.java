package com.swissas.inspection;

import java.util.*;
import java.util.stream.Collectors;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.quickfix.TranslateMakAsIgnoreQuickFix;
import com.swissas.quickfix.TranslateQuickFix;
import com.swissas.quickfix.TranslateTooltipQuickFix;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * The inspesction and quickfixes for missing translation
 *
 * @author Tavan Alain
 */

public class MissingTranslationInspection extends LocalInspectionTool {

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
		return new JavaElementVisitor() {

			@Override
			public void visitJavaFile(PsiJavaFile file) {
				super.visitJavaFile(file);
				Collection<PsiElement> children = getElementsToTranslate(file);
				LocalQuickFix ignoreFix = new TranslateMakAsIgnoreQuickFix();
				LocalQuickFix translateFix = new TranslateQuickFix(file);
				LocalQuickFix translateTooltipFix = new TranslateTooltipQuickFix(file);
				
				
				for (PsiElement element : children) {
					holder.registerProblem(element, RESOURCE_BUNDLE.getString("missing.translation"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, ignoreFix, translateFix, translateTooltipFix);
				}
			}
		};
	}

	private List<PsiElement> getElementsToTranslate(PsiJavaFile file){
		List<PsiElement> result = new ArrayList<>();
		Collection<PsiElement> stringExpression = PsiTreeUtil.collectElementsOfType(file, PsiLiteralExpression.class).
				stream().filter(e -> ((PsiJavaToken)e.getFirstChild()).getTokenType().equals(JavaTokenType.STRING_LITERAL))
				.collect(Collectors.toList());
		for (PsiElement currentElement : stringExpression) {
		
			//first check if the string is in the range already handled
			if(result.stream().noneMatch(r -> r.getTextRange().contains(currentElement.getTextRange()))){
				PsiElement potentialParent = PsiTreeUtil.getParentOfType(currentElement, PsiPolyadicExpression.class);
				if(potentialParent != null){
					if(isNotSql(potentialParent) && isNotException(potentialParent) && isNotIgnored(potentialParent)){
						result.add(potentialParent);
					}
				}else {
					if(isNotSql(currentElement) && isNotException(currentElement) && isNotIgnored(currentElement)){
						result.add(currentElement);
					}
				}
			}
		}
		return result;
	}

	private boolean isNotSql(PsiElement element){
		String value = element.getText();
		return !value.contains("DELETE") && !value.contains("INSERT") &&
				!value.contains("UPDATE") && !value.contains("SELECT");
	}
	
	private boolean isNotIgnored(PsiElement element) {
		PsiElement parent = element.getParent();
		List<PsiComment> comments = PsiTreeUtil.getChildrenOfTypeAsList(parent, PsiComment.class);
		return comments.isEmpty() || comments.stream().noneMatch(e -> e.getText().contains("NO_EXT"));
	}
	
	private boolean isNotException(PsiElement element){
		PsiExpressionList expressionList = PsiTreeUtil.getParentOfType(element, PsiExpressionList.class);
		return expressionList == null || !expressionList.getPrevSibling().getText().contains("Exception");
	}
}
