package com.swissas.inspection;


import java.util.ResourceBundle;
import java.util.stream.Stream;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaFile;
import com.swissas.quickfix.MissingAuthorQuickFix;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


/**
 * An inspection for missing author in the java files.
 * It will automatically add a default javadoc author template if none is present
 * @author Tavan Alain
 */

public class MissingAuthorInspection extends LocalInspectionTool{

    @NonNls
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
    private static final String AUTHOR = " * @author ";

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return RESOURCE_BUNDLE.getString("missing.class.author");
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
            public void visitJavaFile(@NotNull PsiJavaFile file) {
                super.visitJavaFile(file);
                PsiClass[] classes = file.getClasses();
                if(classes.length > 0) {
                    PsiClass firstClass = classes[0];
                    LocalQuickFix MissingAuthorQuickFix = new MissingAuthorQuickFix(file);

                    if (firstClass.getDocComment() == null) {
                        holder.registerProblem(holder.getManager().createProblemDescriptor(firstClass, RESOURCE_BUNDLE.getString("class.has.no.javadoc"), MissingAuthorQuickFix, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
                    } else if (firstClass.getDocComment().getTags().length == 0) {
                        holder.registerProblem(holder.getManager().createProblemDescriptor(firstClass.getDocComment(), RESOURCE_BUNDLE.getString("class.has.no.author"), MissingAuthorQuickFix, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
                    } else if (Stream.of(firstClass.getDocComment().getTags()).noneMatch(tag -> "author".equals(tag.getName()))) {
                        holder.registerProblem(holder.getManager().createProblemDescriptor(firstClass.getDocComment().getTags()[0], RESOURCE_BUNDLE.getString("class.has.no.author"), MissingAuthorQuickFix, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
                    }
                }
            }
        };
    }
            
}