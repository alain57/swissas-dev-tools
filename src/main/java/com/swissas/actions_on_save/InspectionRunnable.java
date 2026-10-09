package com.swissas.actions_on_save;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.InspectionEngine;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.QuickFix;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;

import static com.swissas.actions_on_save.SaveActionManager.LOGGER;

/** Runs one inspection for the files being saved. */

public class InspectionRunnable {
	
	private final Project project;
	private final Set<PsiFile> psiFiles;
	private final Supplier<LocalInspectionTool> inspectionToolSupplier;
	
	InspectionRunnable(Project project, Set<PsiFile> psiFiles, Supplier<LocalInspectionTool> inspectionToolSupplier) {
		this.project = project;
		this.psiFiles = new HashSet<>(psiFiles);
		this.inspectionToolSupplier = inspectionToolSupplier;
	}
	
	void run(Executor executor, Runnable onComplete) {
		AtomicBoolean completed = new AtomicBoolean();
		Runnable completeOnce = () -> {
			if (completed.compareAndSet(false, true)) {
				onComplete.run();
			}
		};
		ReadAction.nonBlocking(this::collectProblemDescriptors)
				.expireWith(this.project)
				.finishOnUiThread(ModalityState.nonModal(), problemDescriptors -> {
					try {
						if (!problemDescriptors.isEmpty()) {
							applyFixes(problemDescriptors);
						}
					} finally {
						completeOnce.run();
					}
				})
				.submit(executor)
				.onError(exception -> {
					if (exception instanceof ProcessCanceledException) {
						LOGGER.info("Save action inspection was canceled");
					} else {
						LOGGER.error("Unable to inspect files for save actions", exception);
					}
					completeOnce.run();
				});
	}

	private List<ProblemDescriptor> collectProblemDescriptors() {
		InspectionManager inspectionManager = InspectionManager.getInstance(this.project);
		GlobalInspectionContext context = inspectionManager.createNewGlobalContext();
		LocalInspectionToolWrapper toolWrapper = new LocalInspectionToolWrapper(this.inspectionToolSupplier.get());
		List<ProblemDescriptor> allProblemDescriptors = new ArrayList<>();
		for (PsiFile psiFile : this.psiFiles) {
			ProgressManager.checkCanceled();
			if (!psiFile.isValid()) {
				continue;
			}
			try {
				allProblemDescriptors.addAll(InspectionEngine.runInspectionOnFile(psiFile, toolWrapper, context));
			} catch (IndexNotReadyException exception) {
				LOGGER.info("Cannot inspect files: index not ready (" + exception.getMessage() + ")");
			}
		}
		return allProblemDescriptors;
	}

	private void applyFixes(List<ProblemDescriptor> problemDescriptors) {
		WriteCommandAction.writeCommandAction(this.project, this.psiFiles.toArray(new PsiFile[0]))
				.run(() -> problemDescriptors.forEach(this::writeQuickFixes));
	}

	@SuppressWarnings("unchecked")
	private void writeQuickFixes(ProblemDescriptor problemDescriptor) {
		QuickFix<ProblemDescriptor>[] fixes = problemDescriptor.getFixes();
		if (fixes == null) {
			return;
		}
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
