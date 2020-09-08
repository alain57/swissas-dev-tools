package com.swissas.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The pre-commit checking factory class
 * @author Tavan Alain
 */
class PreCommitCheckingFactory extends CheckinHandlerFactory {
    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        VirtualFile file = panel.getVirtualFiles().stream().findFirst().orElse(null);
        boolean isGit = false;
        if(file != null) {
            FilePath filePath = VcsUtil.getFilePath(file.getPath());
            Project project = panel.getProject();
            String vcsName = Optional.ofNullable(VcsUtil.getVcsFor(project, filePath)).map(AbstractVcs::getName).orElse("");
            isGit = "Git".equalsIgnoreCase(vcsName);
        }
        return new PreCommitCheckingHandler(panel, isGit);
    }
}
