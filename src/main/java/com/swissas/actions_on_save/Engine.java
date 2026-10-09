package com.swissas.actions_on_save;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.swissas.util.ProjectUtil;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.swissas.actions_on_save.Result.ResultCode;

/**
 * Implementation of the save action engine. It will filter, process and log modifications to the files.
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */
class Engine {
	private static final Logger LOGGER = Logger.getInstance("Swiss-as");
	
	private final List<Processor> processors;
	private final Project project;
	private final Set<PsiFile> psiFiles;
	
	Engine(
		   List<Processor> processors,
		   Project project,
		   Set<PsiFile> psiFiles) {
		this.processors = new ArrayList<>(processors);
		this.project = project;
		this.psiFiles = new HashSet<>(psiFiles);
	}
	
	void processPsiFilesIfNecessary(Executor executor, Runnable onComplete) {
		LOGGER.info("Processing " + this.project + " files (" + this.psiFiles.size() + ")");
		AtomicBoolean completed = new AtomicBoolean();
		Runnable completeOnce = () -> {
			if (completed.compareAndSet(false, true)) {
				onComplete.run();
			}
		};
		try {
			ReadAction.nonBlocking(this::findEligiblePsiFiles)
					.expireWith(this.project)
					.finishOnUiThread(ModalityState.nonModal(), psiFilesEligible -> {
						try {
							LOGGER.info("Valid files (" + psiFilesEligible.size() + ")");
							if (psiFilesEligible.isEmpty()) {
								completeOnce.run();
								return;
							}
							List<SaveCommand> commands = this.processors.stream()
									.<SaveCommand>map(processor -> processor.getSaveCommand(this.project, psiFilesEligible))
									.filter(command -> command.getInspectionAction().isEnabled())
									.toList();
							LOGGER.info("Filtered processors " + commands);
							processCommands(commands, 0, psiFilesEligible, executor, new ArrayList<>(), completeOnce);
						} catch (RuntimeException exception) {
							completeOnce.run();
							throw exception;
						}
					})
					.submit(executor)
					.onError(exception -> {
						if (exception instanceof ProcessCanceledException) {
							LOGGER.info("Save action file filtering was canceled");
						} else {
							LOGGER.error("Unable to filter files for save actions", exception);
						}
						completeOnce.run();
					});
		} catch (RuntimeException exception) {
			completeOnce.run();
			throw exception;
		}
	}

	private Set<PsiFile> findEligiblePsiFiles() {
		Set<PsiFile> psiFilesEligible = new HashSet<>();
		for (PsiFile psiFile : this.psiFiles) {
			ProgressManager.checkCanceled();
			if (isPsiFileEligible(this.project, psiFile)) {
				psiFilesEligible.add(psiFile);
			}
		}
		return psiFilesEligible;
	}

	private void processCommands(List<SaveCommand> commands,
	                            int commandIndex,
	                            Set<PsiFile> psiFiles,
	                            Executor executor,
	                            List<SimpleEntry<InspectionAction, Result<ResultCode>>> results,
	                            Runnable onComplete) {
		if (this.project.isDisposed() || commandIndex >= commands.size()) {
			LOGGER.info("Exit engine with results "
					+ results.stream()
					.map(entry -> entry.getKey() + ":" + entry.getValue())
					.toList());
			onComplete.run();
			return;
		}

		SaveCommand command = commands.get(commandIndex);
		LOGGER.info("Execute command " + command + " on " + psiFiles.size() + " files");
		AtomicBoolean commandCompleted = new AtomicBoolean();
		Consumer<Result<ResultCode>> commandFinished = result -> {
			if (!commandCompleted.compareAndSet(false, true)) {
				return;
			}
			results.add(new SimpleEntry<>(command.getInspectionAction(), result));
			processCommands(commands, commandIndex + 1, psiFiles, executor, results, onComplete);
		};
		try {
			command.execute(executor, commandFinished);
		} catch (RuntimeException exception) {
			if (commandCompleted.compareAndSet(false, true)) {
				onComplete.run();
			}
			throw exception;
		}
	}
	
	private boolean isPsiFileEligible(Project project, PsiFile psiFile) {
		return psiFile != null
				&& isProjectValid(project)
				&& isPsiFileInProject(project, psiFile)
				&& isPsiFileFresh(psiFile)
				&& isPsiFileValid(psiFile);
	}
	
	private boolean isProjectValid(Project project) {
		//the fixes are Swiss-AS specific, they must not modify the files of any other project
		return project.isInitialized()
				&& !project.isDisposed()
				&& ProjectUtil.getInstance(project).isAmosProject();
	}
	
	private boolean isPsiFileInProject(Project project, PsiFile psiFile) {
		VirtualFile virtualFile = psiFile.getVirtualFile();
		boolean inProject = virtualFile != null && ProjectRootManager.getInstance(project)
				.getFileIndex().isInContent(virtualFile);
		if (!inProject) {
			LOGGER.info("File " + psiFile + " not in current project " + project);
		}
		return inProject;
	}
	
	
	
	private boolean isPsiFileFresh(PsiFile psiFile) {
		return psiFile.getModificationStamp() != 0;
	}
	
	private boolean isPsiFileValid(PsiFile psiFile) {
		return psiFile.isValid();
	}
	
}