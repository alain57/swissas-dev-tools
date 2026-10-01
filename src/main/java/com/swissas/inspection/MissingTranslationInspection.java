package com.swissas.inspection;

import java.util.*;
import java.util.regex.Pattern;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ex.LineStatusTracker;
import com.intellij.openapi.vcs.ex.LineStatusTrackerI;
import com.intellij.openapi.vcs.ex.Range;
import com.intellij.openapi.vcs.impl.LineStatusTrackerManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.swissas.quickfix.MarkAsIgnoredQuickfix;
import com.swissas.quickfix.TranslateQuickFix;
import com.swissas.quickfix.TranslateTooltipQuickFix;
import com.swissas.util.ProjectUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The inspection for missing translation
 *
 * @author Tavan Alain
 */

class MissingTranslationInspection extends LocalInspectionTool {
	
	@Override
	public boolean isEnabledByDefault() {
		return true;
	}
	
	@Override
	@NotNull
	public String getDisplayName() {
		return ResourceBundle.getBundle("texts").getString("missing.translation");
	}
	
	@Override
	@NotNull
	public String getGroupDisplayName() {
		return ResourceBundle.getBundle("texts").getString("swiss.as");
	}
	
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		LocalQuickFix ignoreFix = new MarkAsIgnoredQuickfix("/*NO_EXT*/");
		LocalQuickFix translateFix = new TranslateQuickFix(holder.getFile());
		LocalQuickFix translateTooltipFix = new TranslateTooltipQuickFix(holder.getFile());
		LocalQuickFix noSolFix = new MarkAsIgnoredQuickfix("/*NOSQL*/");
		LocalQuickFix[] fixes = SwissAsStorage.getInstance().isNewTranslation() ? new LocalQuickFix[]{ignoreFix, translateFix, translateTooltipFix} : new LocalQuickFix[]{ignoreFix};
		
		if (holder.getFile().getName().endsWith("Test.java") || !ProjectUtil.getInstance(holder.getProject()).isAmosProject()) {
			return super.buildVisitor(holder, isOnTheFly);
		}
		
		return new MyJavaElementVisitor(holder, fixes, noSolFix);
	}
	
	
	static class MyJavaElementVisitor extends JavaElementVisitor {
		
		private static final String MISSING_NOSQL_MESSAGE       = ResourceBundle.getBundle("texts").getString("missing.nosql");
		private static final String MISSING_TRANSLATION_MESSAGE = ResourceBundle.getBundle("texts").getString("missing.translation");
		private static final Pattern SQL_PATTERN = Pattern.compile(".*(DELETE|INSERT|UPDATE|SELECT).*", Pattern.CASE_INSENSITIVE);
		private static final Pattern FILENAME_PATTERN = Pattern.compile("^[^.]+\\.\\w{3}$");
		private static final Pattern IS_IN_METHODS = Pattern.compile(".*(Exception|firePropertyChange|fireIndexedPropertyChange|assertEquals|MultiLang(Text|ToolTip)|getLogger\\(\\).*|WithHistory)$");
		
		private final ProblemsHolder  holder;
		private final LocalQuickFix[] fixes;
		private final LocalQuickFix   noSqlFix;
		private final boolean         noSvn;
		private final List<Range>     rangesToCheck;
		private final Document        document;
		private final int             minSize;
		private final boolean         onlyCheckChangedLines;
		
		MyJavaElementVisitor(@NotNull ProblemsHolder holder, @NotNull LocalQuickFix[] fixes, @NotNull LocalQuickFix noSqlFix) {
			this.noSqlFix = noSqlFix;
			this.holder = holder;
			this.fixes = fixes.clone();
			VirtualFile virtualFile = holder.getFile().getVirtualFile();
			Project project = holder.getProject();
			LineStatusTracker<?> lineStatusTracker = virtualFile == null ? null
			                                                                    : LineStatusTrackerManager.getInstance(project).getLineStatusTracker(virtualFile);
			this.rangesToCheck = Optional.ofNullable(lineStatusTracker).map(LineStatusTrackerI::getRanges)
										 .map(ArrayList<Range>::new)
										 .orElse(new ArrayList<>());
			this.rangesToCheck.removeIf(e -> e.getType() == Range.DELETED);
			this.noSvn = this.rangesToCheck.isEmpty();
			this.document = holder.getFile().getViewProvider().getDocument();
			this.minSize = readMinSize();
			this.onlyCheckChangedLines = SwissAsStorage.getInstance().isTranslationOnlyCheckChangedLine();
		}
		
		private static int readMinSize() {
			try {
				return Integer.parseInt(SwissAsStorage.getInstance().getMinWarningSize());
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		
		@Override
		public void visitLiteralExpression(@NotNull PsiLiteralExpression expression) {
			super.visitLiteralExpression(expression);
			if (!(expression.getValue() instanceof String expressionValue)
			    || expressionValue.length() < this.minSize
			    || FILENAME_PATTERN.matcher(expressionValue).matches()) {
				return;
			}
			if (!shouldCheckLineOf(expression)) {
				return;
			}
			if (SQL_PATTERN.matcher(expressionValue).matches()) {
				checkHierarchyAndRegisterMissingNoSOLProblemIfNeeded(expression);
			}
			checkHierrarchyAndRegisterMissingTranslationProblemIfNeeded(expression);
		}
		
		/**
		 * Uses the document to get the line number instead of rebuilding and scanning the whole
		 * file text for every single literal (which was quadratic on big files).
		 */
		private boolean shouldCheckLineOf(@NotNull PsiLiteralExpression expression) {
			if (this.noSvn || !this.onlyCheckChangedLines) {
				return true;
			}
			if (this.document == null) {
				return true;
			}
			int textOffset = expression.getTextOffset();
			if (textOffset < 0 || textOffset > this.document.getTextLength()) {
				return true;
			}
			int lineNumber = this.document.getLineNumber(textOffset);
			return this.rangesToCheck.stream()
			                         .anyMatch(r -> lineNumber >= r.getLine1() && lineNumber <= r.getLine2());
		}
		
		private void checkHierarchyAndRegisterMissingNoSOLProblemIfNeeded(PsiLiteralExpression expression) {
			if(hasNoNoSqlAsNextSibling(expression)) {
				PsiElement parent = expression.getParent();
				if (hasNoNoSqlAsNextSibling(parent)) {
					boolean wasRegisteredOnParent = registerNoSOLProblemOnParentIfRequired(parent, expression);
					boolean wasRegisterOnGrandParent = registerNoSOLProblemOnGrandParentIfRequired(parent, wasRegisteredOnParent);
					if(!wasRegisteredOnParent && !wasRegisterOnGrandParent) { //don't care about the parent special cases, the issue is on the expression itself
						this.holder.registerProblem(expression, MISSING_NOSQL_MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, this.noSqlFix);
					}
				}
			}
		}
		
		private boolean registerNoSOLProblemOnParentIfRequired(PsiElement parent, PsiLiteralExpression expression) {
			boolean wasRegistered = false;
			if (parent instanceof PsiExpressionList) {
				wasRegistered = registerNoSolOnElementIfParentIsNotNull(expression, getPrevNotEmptySpaces(parent));
			}
			return wasRegistered;
		}
		
		private boolean registerNoSolOnElementIfParentIsNotNull(PsiElement elementToHighlight, PsiElement elementToNullCheck) {
			boolean result = elementToNullCheck != null;
			if (result) {
				this.holder
						.registerProblem(elementToHighlight, MISSING_NOSQL_MESSAGE,
						                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
						                 this.noSqlFix);
			}
			return result;
		}
		
		private boolean registerNoSOLProblemOnGrandParentIfRequired(PsiElement parent, boolean wasRegisteredOnParent) {
			boolean wasRegistered = false;
			if (!wasRegisteredOnParent && parent instanceof PsiPolyadicExpression) {
				PsiElement grandParent = parent.getParent();
				if (grandParent instanceof PsiAssignmentExpression
				    || grandParent instanceof PsiLocalVariable) {
					this.holder.registerProblem(parent, MISSING_NOSQL_MESSAGE,
					                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
					                            this.noSqlFix);
					wasRegistered = true;
				} else {
					wasRegistered = registerNoSolOnElementIfParentIsNotNull(parent, getPrevNotEmptySpaces(parent.getParent()));
				}
			}
			return wasRegistered;
		} 
		
		private void checkHierrarchyAndRegisterMissingTranslationProblemIfNeeded(@NotNull PsiLiteralExpression expression) {
			PsiElement parent = expression.getParent();
			PsiElement grandParent = parent == null ? null : parent.getParent();
			if(grandParent instanceof PsiAnnotationParameterList){
				return;
			}
			if(hasNoNoExtAsNextSibling(expression) && hasNoNoExtAsNextSibling(parent)) {
				if (parent instanceof PsiExpressionList) {
					PsiElement parentPrevSibling = getPrevNotEmptySpaces(parent);
					registerProblemIfParentPreviousSiblingNotInMethods(expression, parentPrevSibling);
				} else if (parent instanceof PsiPolyadicExpression) {
					if (grandParent instanceof PsiAssignmentExpression || grandParent instanceof PsiLocalVariable) {
						this.holder.registerProblem(parent, MISSING_TRANSLATION_MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, this.fixes);
					} else {
						PsiElement beforeGrandParent = getPrevNotEmptySpaces(parent.getParent());
						registerProblemIfParentPreviousSiblingNotInMethods(parent, beforeGrandParent);
					}
				} else { //don't care about the parent special cases, the issue is on the expression itself
					this.holder.registerProblem(expression, MISSING_TRANSLATION_MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, this.fixes);
				}
			}
		}

		private void registerProblemIfParentPreviousSiblingNotInMethods(@NotNull PsiElement currentElement, PsiElement parentPrevSibling) {
			if (parentPrevSibling != null && !IS_IN_METHODS.matcher(parentPrevSibling.getText()).matches()) {
				this.holder.registerProblem(currentElement, MISSING_TRANSLATION_MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, this.fixes);
			}
		}

		private PsiElement getPrevNotEmptySpaces(PsiElement element) {
			PsiElement psiElement = element.getPrevSibling();
			if (psiElement instanceof PsiWhiteSpace) {
				psiElement = psiElement.getPrevSibling();
			}
			return psiElement;
		}
		
		private boolean hasNoNoExtAsNextSibling(PsiElement expression) {
			return hasNoSiblingContainingText(expression, "NO_EXT");
		}
		
		private boolean hasNoNoSqlAsNextSibling(PsiElement expression) {
			return hasNoSiblingContainingText(expression, "NOSQL");
		}
		
		private static boolean hasNoSiblingContainingText(@Nullable PsiElement sibling, @NotNull String text) {
			boolean result = true;
			if (sibling != null) {
				for (PsiElement nextSibling = sibling.getNextSibling(); nextSibling != null; nextSibling = nextSibling.getNextSibling()) {
					if (nextSibling instanceof PsiComment && nextSibling.getText().contains(text)) {
						result = false;
						break;
					}
				}
			}
			return result;
		}
		
	}
	
}
