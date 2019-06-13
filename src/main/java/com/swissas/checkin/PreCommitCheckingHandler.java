package com.swissas.checkin;


import java.util.ResourceBundle;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.PairConsumer;
import com.swissas.widget.TrafficLightPanel;
import org.jetbrains.annotations.NonNls;

/**
 * The pre commit checking that will make sure that nobody can commit with an empty message or commit with the traffic light indicator 
 * 
 * @author Tavan Alain
 */
class PreCommitCheckingHandler extends CheckinHandler {
    
    private final Project project;
    private final CheckinProjectPanel checkinProjectPanel;

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");

    @NonNls
    private static final String TITLE = RESOURCE_BUNDLE.getString("commit.title");
    @NonNls
    private static final String WANT_TO_COMMIT = RESOURCE_BUNDLE.getString("commit.with.trafficlight");
    @NonNls
    private static final String EMPTY_COMMIT_MSG = RESOURCE_BUNDLE.getString("commit.without.message");
    private final TrafficLightPanel trafficLightPanel;
    
    
    PreCommitCheckingHandler(final CheckinProjectPanel checkinProjectPanel){
        this.project = checkinProjectPanel.getProject();
        this.checkinProjectPanel = checkinProjectPanel;
        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(this.project);
        this.trafficLightPanel = (TrafficLightPanel)ideFrame.getStatusBar().getWidget(TrafficLightPanel.WIDGET_ID);
        
    }

    @Override
    public ReturnResult beforeCheckin(CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
        if (DumbService.getInstance(this.project).isDumb()) {
            Messages.showErrorDialog(this.project, RESOURCE_BUNDLE.getString("commit.not.possible"),
                    TITLE);
            return ReturnResult.CANCEL;
        }
        if(this.checkinProjectPanel.getCommitMessage().trim().isEmpty()){
            Messages.showDialog(this.project,
                    EMPTY_COMMIT_MSG,
                    TITLE,
                    new String[]{RESOURCE_BUNDLE.getString("ok")},
                    1,
                    null);
            return ReturnResult.CANCEL;
        }
        
        if(this.trafficLightPanel.isRedOrYellowOn()){
            return showTrafficLightDialog();
        }else {
            return ReturnResult.COMMIT;
        }
    }

    private ReturnResult showTrafficLightDialog() {
        ConfirmationDialog confirmationDialog = new ConfirmationDialog(this.trafficLightPanel.getTrafficDetails());
        confirmationDialog.show();
        int messageResult = confirmationDialog.getExitCode();
        if(messageResult == DialogWrapper.NEXT_USER_EXIT_CODE){
            this.trafficLightPanel.setInformWhenReady(this.checkinProjectPanel);
        }

        return messageResult == DialogWrapper.OK_EXIT_CODE ? ReturnResult.COMMIT : ReturnResult.CLOSE_WINDOW;
    }
}
