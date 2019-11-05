package com.swissas.actions_on_save;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiFile;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Set;


import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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
		this.processors = processors;
		this.project = project;
		this.psiFiles = psiFiles;
	}
	
	void processPsiFilesIfNecessary() {
		LOGGER.info("Processing " + this.project + " files " + this.psiFiles);
		Set<PsiFile> psiFilesEligible = this.psiFiles.stream()
				.filter(psiFile -> isPsiFileEligible(this.project, psiFile))
				.collect(toSet());
		LOGGER.info("Valid files " + psiFilesEligible);
		processPsiFiles(this.project, psiFilesEligible);
	}
	
	private void processPsiFiles(Project project, Set<PsiFile> psiFiles) {
		if (psiFiles.isEmpty()) {
			return;
		}
		LOGGER.info("Start processors (" + this.processors.size() + ")");
		List<SaveCommand> processorsEligible = this.processors.stream()
				.map(processor -> processor.getSaveCommand(project, psiFiles))
				.filter(command -> command.getInspectionAction().isEnabled())
				.collect(toList());
		LOGGER.info("Filtered processors " + processorsEligible);
		List<SimpleEntry<InspectionAction, Result<ResultCode>>> results = processorsEligible.stream()
				.peek(command -> LOGGER.info("Execute command " + command + " on " + psiFiles.size() + " files"))
				.map(command -> new SimpleEntry<>(command.getInspectionAction(), command.execute()))
				.collect(toList());
		LOGGER.info("Exit engine with results "
				+ results.stream()
				.map(entry -> entry.getKey() + ":" + entry.getValue())
				.collect(toList()));
	}
	
	private boolean isPsiFileEligible(Project project, PsiFile psiFile) {
		return psiFile != null
				&& isProjectValid(project)
				&& isPsiFileInProject(project, psiFile)
				&& isPsiFileFresh(psiFile)
				&& isPsiFileValid(psiFile);
	}
	
	private boolean isProjectValid(Project project) {
		return project.isInitialized()
				&& !project.isDisposed();
	}
	
	private boolean isPsiFileInProject(Project project, PsiFile psiFile) {
		boolean inProject = ProjectRootManager.getInstance(project)
				.getFileIndex().isInContent(psiFile.getVirtualFile());
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