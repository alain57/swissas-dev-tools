package com.swissas.inspection;

import java.util.*;
import java.util.stream.Collectors;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
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
		int minWarningSize = Integer.valueOf(SwissAsStorage.getInstance(file.getProject()).getMinWarningSize());
		for (PsiElement currentElement : stringExpression) {
			
			//first check if the string is in the range already handled
			if(result.stream().noneMatch(r -> r.getTextRange().contains(currentElement.getTextRange()))){
				int currentSize = (currentElement instanceof PsiLiteralExpressionImpl) && ((PsiLiteralExpressionImpl)currentElement).getInnerText() != null ? ((PsiLiteralExpressionImpl)currentElement).getInnerText().length() : currentElement.getTextLength();
				PsiElement expression = PsiTreeUtil.getParentOfType(currentElement, PsiPolyadicExpression.class);
				if(currentSize >= minWarningSize) {
					if (expression != null) {
						if (isValid(expression)) {
							result.add(expression);
						}
					} else {
						if (isValid(currentElement)) {
							result.add(currentElement);
						}
					}
				}
			}
		}
		return result;
	}

	private boolean isValid(PsiElement currentElement) {
		return isNotTestFile(currentElement)  
				&& isNotSql(currentElement) && isNotTemplateFileName(currentElement) &&
				isNotExceptionOrLogger(currentElement) && isNotIgnored(currentElement);
	}

	private boolean isNotSql(PsiElement element){
		String value = element.getText();
		return !value.contains("DELETE") && !value.contains("INSERT") &&
				!value.contains("UPDATE") && !value.contains("SELECT");
	}
	
	private boolean isNotIgnored(PsiElement element) {//TODO: test with AbstractAmosPaneLineHeader and see why this does not work
		PsiComment comment = PsiTreeUtil.getNextSiblingOfType(element, PsiComment.class);
		return comment == null || !comment.getText().contains("NO_EXT");
	}
	
	private boolean isNotExceptionOrLogger(PsiElement element){
		PsiExpressionList expressionList = PsiTreeUtil.getParentOfType(element, PsiExpressionList.class);
		if(expressionList == null){
			return true;
		}
		String methodCallExpression = expressionList.getPrevSibling().getText();
		return !methodCallExpression.contains("Exception") && !methodCallExpression.contains("getLogger()") &&
				!methodCallExpression.contains("assertEquals") && !methodCallExpression.endsWith("WithHistory");
	}
	
	private boolean isNotTestFile(PsiElement element){
		return !element.getContainingFile().getName().endsWith("Test.java");
	}
	
	private boolean isNotTemplateFileName(PsiElement element){
		return !element.getText().endsWith(".tpl");
	}
}
