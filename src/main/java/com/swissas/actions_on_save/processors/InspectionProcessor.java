package com.swissas.actions_on_save.processors;


import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.GlobalInspectionToolWrapper;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import java.util.List;

import static com.swissas.widget.SwissAsWidget.LOGGER;

/**
 * The inspection processor that execute the given inspection tool to apply the first possible quickfix
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */

public class InspectionProcessor {

    private final Project project;
    private final PsiFile psiFile;
    private final InspectionProfileEntry inspectionTool;

    InspectionProcessor(Project project, PsiFile psiFile, LocalInspectionTool inspectionTool) {
        this.project = project;
        this.psiFile = psiFile;
        this.inspectionTool = inspectionTool;
    }
    
    InspectionProcessor(Project project, PsiFile psiFile, GlobalInspectionTool inspectionTool){
        this.project = project;
        this.psiFile = psiFile;
        this.inspectionTool = inspectionTool;
    }

    public void run() {
        ApplicationManager.getApplication()
                .invokeLater(() -> new InspectionWriteQuickFixesAction(this.project).execute());
    }

    @Override
    public String toString() {
        return this.inspectionTool.getShortName();
    }

    private class InspectionWriteQuickFixesAction extends WriteCommandAction.Simple {

        private InspectionWriteQuickFixesAction(Project project, PsiFile... files) {
            super(project, files);
        }

        @Override
        protected void run() {
            InspectionManager inspectionManager = InspectionManager.getInstance(InspectionProcessor.this.project);
            GlobalInspectionContext context = inspectionManager.createNewGlobalContext(false);
            InspectionToolWrapper toolWrapper;
            
            if(InspectionProcessor.this.inspectionTool instanceof LocalInspectionTool) {
                toolWrapper = new LocalInspectionToolWrapper((LocalInspectionTool) InspectionProcessor.this.inspectionTool);
            }else if(InspectionProcessor.this.inspectionTool instanceof  GlobalInspectionTool){
                toolWrapper = new GlobalInspectionToolWrapper((GlobalInspectionTool) InspectionProcessor.this.inspectionTool);
            }else {
                throw new IllegalArgumentException("Either local or global tool is required, this should not happen !");
            }
            List<ProblemDescriptor> problemDescriptors;
            try {
                problemDescriptors = InspectionEngine.runInspectionOnFile(InspectionProcessor.this.psiFile, toolWrapper, context);
            } catch (IndexNotReadyException exception) {
                return;
            }
            for (ProblemDescriptor problemDescriptor : problemDescriptors) {
                QuickFix[] fixes = problemDescriptor.getFixes();
                if (fixes != null) {
                    writeQuickFixes(problemDescriptor, fixes);
                }
            }
        }

            private void writeQuickFixes(ProblemDescriptor problemDescriptor, QuickFix[] fixes) {
                for (QuickFix fix : fixes) {
                    if (fix != null) {
                        try {
                            fix.applyFix(InspectionProcessor.this.project, problemDescriptor);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }

    }
}


