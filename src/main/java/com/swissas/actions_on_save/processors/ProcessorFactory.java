package com.swissas.actions_on_save.processors;

import java.util.ArrayList;
import java.util.List;

import com.intellij.codeInspection.RedundantSuppressInspection;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.siyeh.ig.inheritance.MissingOverrideAnnotationInspection;
import com.siyeh.ig.style.UnqualifiedFieldAccessInspection;
import com.swissas.inspection.MissingAuthorInspection;
import com.swissas.util.Storage;
import org.jetbrains.annotations.NotNull;

/**
 * The factory emum to know what process to call depending on the settings the user defined.
 * @author Tavan Alain
 */

public enum ProcessorFactory {
    INSTANCE;
    public List<InspectionProcessor> getSaveActionsProcessors(Project project, PsiFile psiFile, Storage storage) {
        List<InspectionProcessor> processors = new ArrayList<>();
        if(storage.isFixMissingThis()) {
            processors.add(getUnqualifiedFieldAccessProcessor(project, psiFile));
        }
        if(storage.isFixMissingOverride()) {
            processors.add(getMissingOverrideAnnotationProcessor(project, psiFile));
        }
        if(storage.isFixUnusedSuppressWarning()) {
            processors.add(getSuppressionAnnotationProcessor(project, psiFile));
        }
        if(storage.isFixMissingAuthor()){
            processors.add(getMissingAuthorAnnotationProcess(project, psiFile));
        }
        return processors;
    }
    
    @NotNull
    private InspectionProcessor getUnqualifiedFieldAccessProcessor(
            Project project, PsiFile psiFile) {
        return new InspectionProcessor(project, psiFile, new UnqualifiedFieldAccessInspection());
    }

    @NotNull
    private InspectionProcessor getMissingOverrideAnnotationProcessor(
            Project project, PsiFile psiFile) {
        MissingOverrideAnnotationInspection inspection = new MissingOverrideAnnotationInspection();
        inspection.ignoreObjectMethods = false;
        return new InspectionProcessor(project, psiFile, inspection);
    }

    @NotNull
    private InspectionProcessor getSuppressionAnnotationProcessor(
            Project project, PsiFile psiFile) {
        return new InspectionProcessor(project, psiFile, new RedundantSuppressInspection());
    }
    
    @NotNull
    private InspectionProcessor getMissingAuthorAnnotationProcess(Project project, PsiFile psiFile) {
        return new InspectionProcessor(project, psiFile, new MissingAuthorInspection());
    }
    
    
}