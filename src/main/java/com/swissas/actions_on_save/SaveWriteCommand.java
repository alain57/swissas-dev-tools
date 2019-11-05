package com.swissas.actions_on_save;

import java.util.Set;
import java.util.function.BiFunction;

import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import static com.swissas.actions_on_save.Result.ResultCode;
import static com.swissas.actions_on_save.Result.ResultCode.OK;

/**
 * Implements write action that encapsulates {@link com.intellij.openapi.command.WriteCommandAction} that returns
 *
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
		RunResult<ResultCode> runResult = new WriteCommandAction<ResultCode>(getProject(), getPsiFilesAsArray()) {
			@Override
			protected void run(@NotNull com.intellij.openapi.application.Result<ResultCode> result) {
				getCommand().apply(getProject(), getPsiFilesAsArray()).run();
				result.setResult(OK);
			}
		}.execute();
		return new Result<>(runResult);
	}
	
}