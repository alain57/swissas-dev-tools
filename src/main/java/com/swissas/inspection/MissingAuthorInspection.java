package com.swissas.inspection;


import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.Nls;
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
        Project project = holder.getProject();
        SwissAsStorage swissAsStorage = SwissAsStorage.getInstance(project);
        
        return new JavaElementVisitor() {

            @Override
            public void visitJavaFile(PsiJavaFile file) {
                super.visitJavaFile(file);
                PsiClass firstClass = file.getClasses()[0];
                LocalQuickFix fix = new LocalQuickFix() {
                    @Nls(capitalization = Nls.Capitalization.Sentence)
                    @NotNull
                    @Override
                    public String getFamilyName() {
                        return InspectionsBundle.message("inspection.javadoc.problem.add.tag.family");
                    }

                    @Override
                    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
                        PsiElement startElement = descriptor.getStartElement();
                        PsiElement addedTag = null;
                        if(startElement instanceof PsiClass){
                            PsiDocComment docComment = JavaPsiFacade.getInstance(project).getElementFactory().createDocCommentFromText("/**\n" +
                                    " * TODO: write you class description here\n" +
                                    " *\n" +
                                    AUTHOR + swissAsStorage.getFourLetterCode() + "\n" +
                                    " */");
                            addedTag = file.addBefore(docComment, startElement);
                        }else if(startElement instanceof PsiDocComment) {
                            List<String> lines = Stream.of(startElement.getText().split("\n")).collect(Collectors.toList());
                            lines.add(lines.size()-1,  AUTHOR + swissAsStorage.getFourLetterCode());
                            PsiDocComment docComment = JavaPsiFacade.getInstance(project).getElementFactory().createDocCommentFromText(StringUtil.join(lines, "\n"));
                            startElement.replace(docComment);
                        }else {
                            PsiDocComment docComment = PsiTreeUtil.getParentOfType(startElement, PsiDocComment.class);
                            if (docComment != null) {
                                PsiDocTag tag = JavaPsiFacade.getInstance(project).getElementFactory().createDocTagFromText(AUTHOR + swissAsStorage.getFourLetterCode() +"\n");
                                addedTag = docComment.addBefore(tag, startElement);
                            }
                        }
                        if(addedTag != null){
                            PsiElement sibling = addedTag.getNextSibling();
                            if (sibling != null) {
                                ((Navigatable) sibling).navigate(true);
                            }
                        }
                    }
                };
                if(firstClass.getDocComment() == null) {
                    holder.registerProblem(holder.getManager().createProblemDescriptor(firstClass, RESOURCE_BUNDLE.getString("class.has.no.javadoc"), fix, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
                }else if(firstClass.getDocComment().getTags().length == 0){
                    holder.registerProblem(holder.getManager().createProblemDescriptor(firstClass.getDocComment(), RESOURCE_BUNDLE.getString("class.has.no.author"), fix, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
                }else if(Stream.of(firstClass.getDocComment().getTags()).noneMatch(tag -> "author".equals(tag.getName()))){
                    holder.registerProblem(holder.getManager().createProblemDescriptor(firstClass.getDocComment().getTags()[0], RESOURCE_BUNDLE.getString("class.has.no.author"), fix, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
                }
                
            }
        };
    }
            
}