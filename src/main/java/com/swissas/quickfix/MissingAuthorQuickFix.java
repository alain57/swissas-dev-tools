package com.swissas.quickfix;

import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The quickfix implementation for missing author
 * @author Tavan Alain
 */
public class MissingAuthorQuickFix implements LocalQuickFix {

    private static final String AUTHOR = " * @author ";
    private final SwissAsStorage                  swissAsStorage;
    private final SmartPsiElementPointer<PsiFile> smartPsiElementPointer;

    public MissingAuthorQuickFix(PsiFile file) {
        this.smartPsiElementPointer = SmartPointerManager.getInstance(file.getProject()).createSmartPsiElementPointer(file);
        this.swissAsStorage = SwissAsStorage.getInstance();
    }


    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return InspectionsBundle.message("inspection.javadoc.problem.add.tag.family");
    }

    /**
     * Adds the author line just before the end of the doc comment, a one line doc comment (/** text *&#47;) is
     * first converted to a multi lines one.
     */
    static String addAuthorToDocComment(String docComment, String author) {
        List<String> lines = Stream.of(docComment.split("\n")).collect(Collectors.toList());
        if (lines.size() == 1) {
            String description = docComment.trim().replaceFirst("^/\\*+", "").replaceFirst("\\*+/$", "").trim();
            lines = new ArrayList<>();
            lines.add("/**");
            if (!description.isEmpty()) {
                lines.add(" * " + description);
            }
            lines.add(" */");
        }
        lines.add(lines.size() - 1, AUTHOR + author);
        return StringUtil.join(lines, "\n");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement startElement = descriptor.getStartElement();
        PsiElement addedTag = null;
        if (startElement instanceof PsiClass) {
            PsiDocComment docComment = JavaPsiFacade.getInstance(project).getElementFactory().createDocCommentFromText("/**\n" +
                                                                                                                       " * TODO: write you class description here\n" +
                                                                                                                       " *\n" +
                                                                                                                       AUTHOR + this.swissAsStorage.getFourLetterCode() + "\n" +
                                                                                                                       " */");
            addedTag = Optional.ofNullable(this.smartPsiElementPointer.getElement())
                               .map(element -> element.addBefore(docComment, startElement))
                               .orElse(null);
            
        } else if (startElement instanceof PsiDocComment) {
            PsiDocComment docComment = JavaPsiFacade.getInstance(project).getElementFactory().createDocCommentFromText(
                    addAuthorToDocComment(startElement.getText(), this.swissAsStorage.getFourLetterCode()));
            startElement.replace(docComment);
        } else {
            PsiDocComment docComment = PsiTreeUtil.getParentOfType(startElement, PsiDocComment.class);
            if (docComment != null) {
                PsiDocTag tag = JavaPsiFacade.getInstance(project).getElementFactory().createDocTagFromText(AUTHOR + this.swissAsStorage.getFourLetterCode() + "\n");
                addedTag = docComment.addBefore(tag, startElement);
            }
        }
        if (addedTag != null) {
            PsiElement sibling = addedTag.getNextSibling();
            if (sibling instanceof Navigatable navigatable) {
                navigatable.navigate(true);
            }
        }
    }
}

