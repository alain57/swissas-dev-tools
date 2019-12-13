package com.swissas.actions_on_save;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.InspectionEngine;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.QuickFix;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import static com.swissas.actions_on_save.SaveActionManager.LOGGER;

/**
 * Implements a runnable for inspections commands.
 *
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */

public class InspectionRunnable implements Runnable {
	
	private final Project project;
	private final Set<PsiFile> psiFiles;
	private final LocalInspectionTool inspectionTool;
	
	InspectionRunnable(Project project, Set<PsiFile> psiFiles, LocalInspectionTool inspectionTool) {
		this.project = project;
		this.psiFiles = psiFiles;
		this.inspectionTool = inspectionTool;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		InspectionManager inspectionManager = InspectionManager.getInstance(this.project);
		GlobalInspectionContext context = inspectionManager.createNewGlobalContext(false);
		LocalInspectionToolWrapper toolWrapper = new LocalInspectionToolWrapper(this.inspectionTool);
		for (PsiFile psiFile : this.psiFiles) {
			List<ProblemDescriptor> problemDescriptors = getProblemDescriptors(context, toolWrapper, psiFile);
			for (ProblemDescriptor problemDescriptor : problemDescriptors) {
				QuickFix<ProblemDescriptor>[] fixes = problemDescriptor.getFixes();
				if (fixes != null) {
					writeQuickFixes(problemDescriptor, fixes);
				}
			}
		}
	}
	
	private List<ProblemDescriptor> getProblemDescriptors(GlobalInspectionContext context,
	                                                      LocalInspectionToolWrapper toolWrapper,
														  PsiFile psiFile) {
		List<ProblemDescriptor> problemDescriptors;
		try {
			problemDescriptors = InspectionEngine.runInspectionOnFile(psiFile, toolWrapper, context);
		} catch (IndexNotReadyException exception) {
			LOGGER.info("Cannot inspect files: index not ready (" + exception.getMessage() + ")");
			return Collections.emptyList();
		}
		return problemDescriptors;
	}
	
	private void writeQuickFixes(ProblemDescriptor problemDescriptor, QuickFix<ProblemDescriptor>[] fixes) {
		for (QuickFix<ProblemDescriptor> fix : fixes) {
			if (fix != null) {
				try {
					fix.applyFix(this.project, problemDescriptor);
				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
		}
	}
}
