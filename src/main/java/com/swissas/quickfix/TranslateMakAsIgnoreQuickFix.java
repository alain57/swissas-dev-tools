package com.swissas.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * Quickfix class to mark a string as not translatable
 * @author Tavan Alain
 */
public class TranslateMakAsIgnoreQuickFix implements LocalQuickFix {

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
        return RESOURCE_BUNDLE.getString("mark.as.no.ext");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement startElement = descriptor.getPsiElement();
        PsiComment noExt = JavaPsiFacade.getInstance(project).getElementFactory().createCommentFromText("/*NO_EXT*/", null);
        startElement.getParent().addAfter(noExt, startElement);
    }
}
