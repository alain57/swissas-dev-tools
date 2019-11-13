package com.swissas.util;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.branch.BranchStateProvider;
import com.intellij.vcsUtil.VcsUtil;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The Project utility class
 *
 * @author Alain Tavan
 */

public class ProjectUtil {
    private static ProjectUtil INSTANCE;

    private Module shared;

    private ProjectUtil() {

    }

    public static ProjectUtil getInstance(){
        if(INSTANCE == null) {
            INSTANCE = new ProjectUtil();
        }
        return INSTANCE;
    }

    public String getBranchOfSelectedEditor(Project project) {
        VirtualFile file = Optional.ofNullable(FileEditorManager.getInstance(project).getSelectedEditor())
                .map(FileEditor::getFile).orElse(null);
        if(file != null) {
            FilePath filePath = VcsUtil.getFilePath(file);
            AbstractVcs vcsFor = VcsUtil.getVcsFor(project, file);
            for (BranchStateProvider provider : BranchStateProvider.EP_NAME.getExtensionList(
                    project)) {
                if (provider.getClass().getName().contains(vcsFor.getName())) {
                    return provider.getCurrentBranch(filePath).getBranchName();
                }
            }
        }
        return null;
    }

    public boolean isAmosProject(Project project) {
        Optional<Module> amos_shared = Stream.of(ModuleManager.getInstance(project).getModules()).filter(e -> e.getName().contains("amos_shared")).findFirst();
        if(amos_shared.isPresent()){
            this.shared = amos_shared.get();
            return true;
        }
        return false;
    }

    public Module getShared() {
        return this.shared;
    }



}
