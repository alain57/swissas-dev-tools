package com.swissas.actions_on_save;

import java.util.Set;
import java.util.function.BiFunction;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * Abstracts a save command with a {@link BiFunction} from pair ({@link Project}, {@link PsiFile}[])
 * to {@link Runnable}. The entry point is {@link #execute()}.
 * 
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */

public abstract class SaveCommand {
	
	private final Project project;
	private final Set<PsiFile> psiFiles;
	private final InspectionAction inspectionAction;
	private final BiFunction<Project, PsiFile[], Runnable> command;
	
	protected SaveCommand(Project project, Set<PsiFile> psiFiles, InspectionAction inspectionAction,
						  BiFunction<Project, PsiFile[], Runnable> command) {
		this.project = project;
		this.psiFiles = psiFiles;
		this.inspectionAction = inspectionAction;
		this.command = command;
	}
	
	public Project getProject() {
		return this.project;
	}
	
	public Set<PsiFile> getPsiFiles() {
		return this.psiFiles;
	}
	
	public PsiFile[] getPsiFilesAsArray() {
		return this.psiFiles.toArray(new PsiFile[0]);
	}
	
	public InspectionAction getInspectionAction() {
		return this.inspectionAction;
	}
	
	public BiFunction<Project, PsiFile[], Runnable> getCommand() {
		return this.command;
	}
	
	
	public abstract Result<Result.ResultCode> execute();
	
}