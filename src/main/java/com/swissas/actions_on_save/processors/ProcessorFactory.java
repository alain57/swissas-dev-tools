package com.swissas.actions_on_save.processors;

import java.util.ArrayList;
import java.util.List;

import com.intellij.codeInspection.RedundantSuppressInspection;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.siyeh.ig.style.UnqualifiedFieldAccessInspection;
import com.swissas.inspection.MissingAuthorInspection;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;

/**
 * The factory emum to know what process to call depending on the settings the user defined.
 * @author Tavan Alain
 */

public enum ProcessorFactory {
    INSTANCE;
    public List<InspectionProcessor> getSaveActionsProcessors(Project project, PsiFile psiFile, SwissAsStorage swissAsStorage) {
        List<InspectionProcessor> processors = new ArrayList<>();
        if(swissAsStorage.isFixMissingThis()) {
            processors.add(getUnqualifiedFieldAccessProcessor(project, psiFile));
        }
        if(swissAsStorage.isFixUnusedSuppressWarning()) {
            processors.add(getSuppressionAnnotationProcessor(project, psiFile));
        }
        if(swissAsStorage.isFixMissingAuthor()){
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
    private InspectionProcessor getSuppressionAnnotationProcessor(
            Project project, PsiFile psiFile) {
        return new InspectionProcessor(project, psiFile, new RedundantSuppressInspection());
    }
    
    @NotNull
    private InspectionProcessor getMissingAuthorAnnotationProcess(Project project, PsiFile psiFile) {
        return new InspectionProcessor(project, psiFile, new MissingAuthorInspection());
    }
    
    
}