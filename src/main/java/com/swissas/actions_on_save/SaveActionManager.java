package com.swissas.actions_on_save;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;


/**
 * SaveActionManager based on the code from the save action plugin
 * @author Tavan Alain
 * @see Engine
 */
public class SaveActionManager implements FileDocumentManagerListener {
	
	public static final Logger LOGGER = Logger.getInstance(SaveActionManager.class);
	
	private static SaveActionManager instance;
	private final List<Processor> processors;
	private boolean running;
	
	
	public static  SaveActionManager getInstance(){
		if(instance == null){
			instance = new SaveActionManager();
		}
		return instance;
	}
	
	
	private SaveActionManager() {
		this.processors = new ArrayList<>();
		this.running = false;
	}
	
	public void addProcessors(Stream<Processor> processors) {
		processors.forEach(this.processors::add);
	}
	
	@Override
	public void beforeAllDocumentsSaving() {
		LOGGER.info("[+] Start SaveActionManager#beforeAllDocumentsSaving");
		Document[] unsavedDocuments = FileDocumentManager.getInstance().getUnsavedDocuments();
		beforeDocumentsSaving(asList(unsavedDocuments));
		LOGGER.info("End SaveActionManager#beforeAllDocumentsSaving");
	}
	
	private void beforeDocumentsSaving(List<Document> documents) {
		LOGGER.info("Locating psi files for " + documents.size() + " documents: " + documents);
		
		Map<Project, Set<PsiFile>> projectPsiFiles = new HashMap<>();
		documents.forEach(document -> stream(ProjectManager.getInstance().getOpenProjects())
				.forEach(project -> ofNullable(PsiDocumentManager.getInstance(project).getPsiFile(document))
						.map(psiFile -> {
							Set<PsiFile> psiFiles = projectPsiFiles.getOrDefault(project, new HashSet<>());
							projectPsiFiles.put(project, psiFiles);
							return psiFiles.add(psiFile);
						})));
		projectPsiFiles.forEach(this::guardedProcessPsiFiles);
	}
	
	public void guardedProcessPsiFiles(Project project, Set<PsiFile> psiFiles) {
		if (ApplicationManager.getApplication().isDisposed()) {
			LOGGER.info("Application is closing, stopping invocation");
			return;
		}
		try {
			if (this.running) {
				LOGGER.info("Plugin already running, stopping invocation");
				return;
			}
			this.running = true;
			Engine engine = new Engine(this.processors, project, psiFiles);
			engine.processPsiFilesIfNecessary();
		} finally {
			this.running = false;
		}
	}
	
}
