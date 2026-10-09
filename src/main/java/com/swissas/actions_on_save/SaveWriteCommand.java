package com.swissas.actions_on_save;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.intellij.openapi.project.Project;

import static com.swissas.actions_on_save.Result.ResultCode;
import static com.swissas.actions_on_save.Result.ResultCode.OK;

/**
 * Implements the command that executes an action on the files being saved.
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */

public class SaveWriteCommand extends SaveCommand {
	
	public SaveWriteCommand(Project project, InspectionAction inspectionAction, Supplier<InspectionRunnable> command) {
		super(project, inspectionAction, command);
	}
	
	@Override
	public void execute(Executor executor, Consumer<Result<ResultCode>> onComplete) {
		getCommand().get().run(executor, () -> onComplete.accept(new Result<>(OK)));
	}
}