package com.swissas.actions_on_save;

import java.util.Set;
import java.util.function.BiFunction;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.ThrowableRunnable;

import static com.swissas.actions_on_save.Result.ResultCode;
import static com.swissas.actions_on_save.Result.ResultCode.OK;

/**
 * Implements write action that encapsulates {@link WriteCommandAction} that returns
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
		try {
			WriteCommandAction.writeCommandAction(getProject(), getPsiFilesAsArray())
			                  .run((ThrowableRunnable<Throwable>) () -> getCommand().apply(getProject(), getPsiFilesAsArray()).run());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return new Result<>(OK);
	}
}