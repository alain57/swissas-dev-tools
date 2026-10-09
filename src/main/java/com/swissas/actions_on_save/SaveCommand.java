package com.swissas.actions_on_save;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.intellij.openapi.project.Project;

/**
 * Abstracts an asynchronous save command.
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */

public abstract class SaveCommand {
	
	private final Project project;
	private final InspectionAction inspectionAction;
	private final Supplier<InspectionRunnable> command;
	
	protected SaveCommand(Project project, InspectionAction inspectionAction, Supplier<InspectionRunnable> command) {
		this.project = project;
		this.inspectionAction = inspectionAction;
		this.command = command;
	}
	
	public Project getProject() {
		return this.project;
	}
	
	public InspectionAction getInspectionAction() {
		return this.inspectionAction;
	}
	
	public Supplier<InspectionRunnable> getCommand() {
		return this.command;
	}
	
	public abstract void execute(Executor executor, Consumer<Result<Result.ResultCode>> onComplete);
	
}