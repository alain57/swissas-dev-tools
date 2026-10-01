package com.swissas.actions_on_save;


import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Executor;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;


/**
 * SaveActionManager based on the code from the save action plugin
 * @author Tavan Alain
 * @see Engine
 */
@Service(Service.Level.APP)
public final class SaveActionManager
		implements FileDocumentManagerListener, Disposable {
	
	public static final Logger LOGGER = Logger.getInstance(SaveActionManager.class);
	
	private final List<Processor> processors;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final Executor executor;


	public static SaveActionManager getInstance() {
		return ApplicationManager.getApplication().getService(SaveActionManager.class);
	}

	public SaveActionManager() {
		this(ApplicationManager.getApplication()::executeOnPooledThread, true);
	}

	SaveActionManager(Executor executor, boolean subscribe) {
		this.processors = Processor.stream().toList();
		this.executor = executor;

		if (subscribe) {
			ApplicationManager.getApplication()
					.getMessageBus()
					.connect(this)
					.subscribe(
							FileDocumentManagerListener.TOPIC,
							this
					);
		}
	}

	@Override
	public void dispose() {
		// nothing to do: connect(this) takes care of the cleanup
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
						.ifPresent(psiFile -> projectPsiFiles
								.computeIfAbsent(project, p -> new HashSet<>())
								.add(psiFile))));
		projectPsiFiles.forEach(this::guardedProcessPsiFiles);
	}
	
	public void guardedProcessPsiFiles(Project project, Set<PsiFile> psiFiles) {
		if (ApplicationManager.getApplication().isDisposed()) {
			LOGGER.info("Application is closing, stopping invocation");
			return;
		}
		//only the call that started the processing may release the lock, otherwise a nested save would unlock it too early
		if (!this.running.compareAndSet(false, true)) {
			LOGGER.info("Plugin already running, stopping invocation");
			return;
		}
		try {
			this.executor.execute(() -> processPsiFiles(project, psiFiles));
		} catch (RuntimeException exception) {
			this.running.set(false);
			throw exception;
		}
	}

	private void processPsiFiles(Project project, Set<PsiFile> psiFiles) {
		try {
			Engine engine = new Engine(this.processors, project, psiFiles);
			engine.processPsiFilesIfNecessary();
		} finally {
			this.running.set(false);
		}
	}
	
}
