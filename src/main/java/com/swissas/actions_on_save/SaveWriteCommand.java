package com.swissas.actions_on_save;

import java.util.Set;
import java.util.function.BiFunction;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import static com.swissas.actions_on_save.Result.ResultCode;
import static com.swissas.actions_on_save.Result.ResultCode.OK;

/**
 * Implements the command that executes an action on the files being saved.
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */

public class SaveWriteCommand extends SaveCommand {
	
	public SaveWriteCommand(Project project, Set<PsiFile> psiFiles, InspectionAction inspectionAction,
							BiFunction<Project, PsiFile[], Runnable> command) {
		super(project, psiFiles, inspectionAction, command);
	}
	
	@Override
	public Result<ResultCode> execute() {
		getCommand().apply(getProject(), getPsiFilesAsArray()).run();
		return new Result<>(OK);
	}
}