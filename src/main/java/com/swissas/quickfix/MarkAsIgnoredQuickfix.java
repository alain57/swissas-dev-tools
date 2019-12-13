package com.swissas.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * Quickfix class to mark a string as ignored (for translation or SQL check)
 * @author Tavan Alain
 */
public class MarkAsIgnoredQuickfix implements LocalQuickFix {

    private final String javaComment;
    
    public MarkAsIgnoredQuickfix(String javaComment) {
        this.javaComment = javaComment;
    }
    
    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return ResourceBundle.getBundle("texts").getString("swiss.as");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return ResourceBundle.getBundle("texts").getString("mark.as.no.ext");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement startElement = descriptor.getPsiElement();
        PsiComment noExt = JavaPsiFacade.getInstance(project).getElementFactory().createCommentFromText(this.javaComment, null);
        startElement.getParent().addAfter(noExt, startElement);
    }
}
