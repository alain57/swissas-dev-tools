package com.swissas.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.branch.BranchData;
import com.intellij.vcs.branch.BranchStateProvider;
import com.intellij.vcsUtil.VcsUtil;

import java.util.Objects;
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

    public String getBranchOfFile(Project project, VirtualFile file) {
        String branchName = null;
        if(file != null) {
            FilePath filePath = VcsUtil.getFilePath(file.getPath());
            AbstractVcs vcsFor = VcsUtil.getVcsFor(project, filePath);
            if(vcsFor != null) {
                branchName = BranchStateProvider.EP_NAME.getExtensionList(project)
                        .stream().filter(Objects::nonNull)
                        .filter(p -> p.getClass().getName().contains(vcsFor.getName()))
                        .map(p -> p.getCurrentBranch(filePath)).filter(Objects::nonNull)
                        .map(BranchData::getBranchName).filter(Objects::nonNull)
                        .map(String::toLowerCase).findFirst().orElse(null);
                if ("trunk".equals(branchName)) {
                    branchName = "preview";
                }
            }
        }
        return branchName;
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
