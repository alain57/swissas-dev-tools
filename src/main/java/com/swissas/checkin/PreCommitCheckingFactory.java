package com.swissas.checkin;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import org.jetbrains.annotations.NotNull;

/**
 * The pre-commit checking factory class
 * @author Tavan Alain
 */
class PreCommitCheckingFactory extends CheckinHandlerFactory {
    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        // nothing may be read from the panel here: the platform can still be building it (NPE otherwise)
        return new PreCommitCheckingHandler(panel);
    }
}
