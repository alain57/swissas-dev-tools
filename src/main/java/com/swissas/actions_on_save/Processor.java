package com.swissas.actions_on_save;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.siyeh.ig.inheritance.MissingOverrideAnnotationInspection;
import com.siyeh.ig.maturity.SuppressionAnnotationInspection;
import com.siyeh.ig.style.UnqualifiedFieldAccessInspection;
import com.swissas.inspection.MissingAuthorInspection;

/**
 * Available processors for java.
 *
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */

public enum Processor {
	
	unqualifiedFieldAccess(InspectionAction.unqualifiedFieldAccess,
			UnqualifiedFieldAccessInspection::new),
	
	missingOverrideAnnotation(InspectionAction.missingOverrideAnnotation,
			() -> {
				MissingOverrideAnnotationInspection inspection = new MissingOverrideAnnotationInspection();
				inspection.ignoreObjectMethods = false;
				return inspection;
			}),
	suppressAnnotation(InspectionAction.suppressAnnotation,
			SuppressionAnnotationInspection::new),
	missingAuthor(InspectionAction.missingAuthor, MissingAuthorInspection::new);
	
	private final InspectionAction inspectionAction;
	private final LocalInspectionTool inspection;
	
	Processor(InspectionAction inspectionAction, Supplier<LocalInspectionTool> inspection) {
		this.inspectionAction = inspectionAction;
		this.inspection = inspection.get();
	}
	
	public InspectionAction getInspectionAction() {
		return this.inspectionAction;
	}
	
	
	public SaveWriteCommand getSaveCommand(Project project, Set<PsiFile> psiFiles) {
		BiFunction<Project, PsiFile[], Runnable> command =
				(p, f) -> new InspectionRunnable(project, psiFiles, getInspection());
		return new SaveWriteCommand(project, psiFiles, getInspectionAction(), command);
	}
	
	public LocalInspectionTool getInspection() {
		return this.inspection;
	}
	
	public static Stream<Processor> stream() {
		return Stream.of(values());
	}
}
