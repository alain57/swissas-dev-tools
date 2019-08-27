package com.swissas.inspection;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.ex.LineStatusTracker;
import com.intellij.openapi.vcs.ex.Range;
import com.intellij.openapi.vcs.impl.LineStatusTrackerManager;
import com.intellij.openapi.vfs.VirtualFile;
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
		LocalQuickFix[] fixes = SwissAsStorage.getInstance().isNewTranslation() ?new LocalQuickFix[]{ignoreFix, translateFix, translateTooltipFix} : new LocalQuickFix[]{ignoreFix};

		if(holder.getFile().getName().endsWith("Test.java")){
			return super.buildVisitor(holder, isOnTheFly);
		}
				
		return new MyJavaElementVisitor(holder, fixes);
	}

	static class MyJavaElementVisitor extends JavaElementVisitor {
		
		private final List<Integer> linesToCheck;
		private final ProblemsHolder holder;
		private final LocalQuickFix[] fixes;

		private final Pattern sqlOrFilename;
		private final Pattern isInMethods;
		private final boolean noSvn;

		MyJavaElementVisitor(@NotNull ProblemsHolder holder, @NotNull LocalQuickFix[] fixes) {
			this.sqlOrFilename = Pattern.compile(".*(DELETE|INSERT|UPDATE|SELECT).*|^[^.]+\\.\\w{3}$");
			this.isInMethods = Pattern.compile(".*(Exception|firePropertyChange|assertEquals|MultiLang(Text|ToolTip)|getLogger\\(\\).*|WithHistory)$");

			this.holder = holder;
			this.fixes = fixes;
			VirtualFile virtualFile = holder.getFile().getVirtualFile();
			Project project = holder.getProject();
			LineStatusTracker lineStatusTracker = LineStatusTrackerManager.getInstance(project).getLineStatusTracker(virtualFile);
			List<Range> ranges = lineStatusTracker == null ? Collections.emptyList() : lineStatusTracker.getRanges();
			this.noSvn = lineStatusTracker == null || ranges == null;
			this.linesToCheck = ranges == null ? Collections.emptyList() : ranges.stream().filter(e -> e.getType() != Range.DELETED).map(Range::getVcsLine1).collect(Collectors.toList());
		}

		@Override
		public void visitLiteralExpression(PsiLiteralExpression expression) {
			super.visitLiteralExpression(expression);
			int minSize = Integer.parseInt(SwissAsStorage.getInstance().getMinWarningSize()) + 2; //psiStringElements are withing double quotes
			int textOffset = expression.getTextOffset();
			int lineNumber = StringUtil.offsetToLineNumber(expression.getContainingFile().getText(), textOffset);
			boolean shouldCheckFile = this.noSvn || !SwissAsStorage.getInstance().isTranslationOnlyCheckChangedLine() || this.linesToCheck.contains(lineNumber);
			if (shouldCheckFile && expression.getTextLength() > minSize &&
						(expression.getValue() instanceof String) &&
						!this.sqlOrFilename.matcher((String) expression.getValue()).matches() &&
						hasNoNoExtAsNextSibling(expression)
				) {
					PsiElement parent = expression.getParent();
					if(hasNoNoExtAsNextSibling(parent)) {
						if (parent instanceof PsiExpressionList) {
							PsiElement parentPrevSibling = getPrevNotEmptySpaces(parent);
							if (parentPrevSibling != null && !this.isInMethods.matcher(parentPrevSibling.getText()).matches()) {
								this.holder.registerProblem(expression, RESOURCE_BUNDLE.getString("missing.translation"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, this.fixes);
							}
						} else if (parent instanceof PsiPolyadicExpression) {
							PsiElement grandParent = parent.getParent();
							if (grandParent instanceof PsiAssignmentExpression || grandParent instanceof PsiLocalVariable) {
								this.holder.registerProblem(parent, RESOURCE_BUNDLE.getString("missing.translation"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, this.fixes);
							} else {
								PsiElement beforeGrandParent = getPrevNotEmptySpaces(parent.getParent());
								if (beforeGrandParent != null && !this.isInMethods.matcher(beforeGrandParent.getText()).matches()) {
									this.holder.registerProblem(parent, RESOURCE_BUNDLE.getString("missing.translation"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, this.fixes);
								}
							}
						}else { //don't care about the parent special cases, the issue is on the expression itself
							this.holder.registerProblem(expression, RESOURCE_BUNDLE.getString("missing.translation"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, this.fixes);
						}
					}
				}
		}
		
		private PsiElement getPrevNotEmptySpaces(PsiElement element){
			PsiElement psiElement = element.getPrevSibling();
			if(psiElement instanceof PsiWhiteSpace){
				psiElement = psiElement.getPrevSibling();
			}
			return psiElement;
		}

		@NotNull
		private Boolean hasNoNoExtAsNextSibling(PsiElement expression) {
			return Optional.ofNullable(PsiTreeUtil.getNextSiblingOfType(expression, PsiComment.class)).
					map(doc -> !doc.getText().contains("NO_EXT")).orElse(true);
		}
	}
	
}
