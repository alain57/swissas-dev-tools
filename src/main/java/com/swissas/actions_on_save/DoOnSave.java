package com.swissas.actions_on_save;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.swissas.actions_on_save.processors.InspectionProcessor;
import com.swissas.actions_on_save.processors.ProcessorFactory;
import com.swissas.util.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.swissas.util.PsiFiles.isPsiFileInProject;
import static com.swissas.widget.SwissAsWidget.LOGGER;
import static java.util.Collections.synchronizedList;

/**
 * The action that will execute the different inspection on save.
 * (based on the code from the save action plugin)
 *
 * @author Tavan Alain
 */

public class DoOnSave implements FileDocumentManagerListener {

    private static final List<InspectionProcessor> runningProcessors = synchronizedList(new ArrayList<>());

    @Override
    public void beforeAllDocumentsSaving() {
       //on IntelliJ 2017 or older this method is needed even if it does nothing. Keep it to be compatible with older IntelliJ versions
    }

    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        LOGGER.debug("Running SaveActionManager on " + document);
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
                checkAndProcessPsiFile(project, psiFile);
        }
    }

    @Override
    public void beforeFileContentReload(VirtualFile file, @NotNull Document document) {
        //on IntelliJ 2017 or older this method is needed even if it does nothing. Keep it to be compatible with older IntelliJ versions
    }

    @Override
    public void fileWithNoDocumentChanged(@NotNull VirtualFile file) {
        //on IntelliJ 2017 or older this method is needed even if it does nothing. Keep it to be compatible with older IntelliJ versions
    }

    @Override
    public void fileContentReloaded(@NotNull VirtualFile file, @NotNull Document document) {
        //on IntelliJ 2017 or older this method is needed even if it does nothing. Keep it to be compatible with older IntelliJ versions
    }

    @Override
    public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {
        //on IntelliJ 2017 or older this method is needed even if it does nothing. Keep it to be compatible with older IntelliJ versions
    }

    @Override
    public void unsavedDocumentsDropped() {
        //on IntelliJ 2017 or older this method is needed even if it does nothing. Keep it to be compatible with older IntelliJ versions
    }

    private void checkAndProcessPsiFile(Project project, PsiFile psiFile) {
        if (isPsiFileEligible(project, psiFile)) {
            processPsiFile(project, psiFile);
        }
    }

   
    /**
     * The psi files seems to be shared between projects, so we need to check if the file is physically
     * in that project before reformating, or else the file is formatted twice and intellij will ask to
     * confirm unlocking of non-project file in the other project.
     */
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

  

    private boolean isPsiFileFresh(PsiFile psiFile) {
        return psiFile.getModificationStamp() != 0;
    }

    private boolean isPsiFileValid(PsiFile psiFile) {
        return psiFile.isValid();
    }

    private void processPsiFile(Project project, PsiFile psiFile) {
        List<InspectionProcessor> processors = getSaveActionsProcessors(project, psiFile);
        LOGGER.debug("Running processors " + processors + ", file " + psiFile + ", project " + project);
        for (InspectionProcessor processor : processors) {
            runProcessor(processor);
        }
    }


    private List<InspectionProcessor> getSaveActionsProcessors(Project project, PsiFile psiFile) {
        return ProcessorFactory.INSTANCE
                .getSaveActionsProcessors(project, psiFile, Storage.getStorageFromProject(project));
    }

    private void runProcessor(InspectionProcessor processor) {
        if (runningProcessors.contains(processor)) {
            return;
        }
        try {
            runningProcessors.add(processor);
            processor.run();
        } finally {
            runningProcessors.remove(processor);
        }
    }
}
