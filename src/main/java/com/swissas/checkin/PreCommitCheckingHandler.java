package com.swissas.checkin;


import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
import com.swissas.dialog.ConfirmationDialog;
import com.swissas.dialog.ImportantPreCommitsDone;
import com.swissas.util.SwissAsStorage;
import com.swissas.widget.TrafficLightPanel;
import org.jetbrains.annotations.NonNls;

/**
 * The pre commit checking that will make sure that nobody can commit with an empty message or commit with the traffic light indicator 
 * 
 * @author Tavan Alain
 */
class PreCommitCheckingHandler extends CheckinHandler {

    public static final String FIX_RELEASE_BLOCKER = "fix release blocker";
    private final Project project;
    private final CheckinProjectPanel checkinProjectPanel;

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");

    @NonNls
    private static final String TITLE = RESOURCE_BUNDLE.getString("commit.title");
    @NonNls
    private static final String EMPTY_COMMIT_MSG = RESOURCE_BUNDLE.getString("commit.without.message");
    private TrafficLightPanel trafficLightPanel = null;

    
    PreCommitCheckingHandler(CheckinProjectPanel checkinProjectPanel){
        this.project = checkinProjectPanel.getProject();
        this.checkinProjectPanel = checkinProjectPanel;
        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(this.project);
        if (ideFrame != null) {
            this.trafficLightPanel = (TrafficLightPanel) ideFrame.getStatusBar().getWidget(TrafficLightPanel.WIDGET_ID);
        }
        
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
        ReturnResult result;
        if(this.trafficLightPanel != null && this.trafficLightPanel.isRedOrYellowOn()){
            result  = showTrafficLightDialog() ;
        }else {
            result = ReturnResult.COMMIT;
        }
        
        if(result == ReturnResult.COMMIT && !displayPreCommitChecksIfNeeded()) {
            result = ReturnResult.CANCEL;
        }
        
        return result;
    }

    private boolean displayPreCommitChecksIfNeeded(){
        boolean result = true;
        boolean informQA = SwissAsStorage.getInstance().isPreCommitInformQA();
        boolean reviewNeeded = SwissAsStorage.getInstance().isPreCommitCodeReview();
        if(informQA || reviewNeeded){
            ImportantPreCommitsDone dialog = new ImportantPreCommitsDone(this.checkinProjectPanel);
            dialog.show();
            int exitCode = dialog.getExitCode();
            if(exitCode == DialogWrapper.NEXT_USER_EXIT_CODE){
                sendMail();
            }else if(exitCode == DialogWrapper.CANCEL_EXIT_CODE){
                result = false;
            }
        }
        
        return result;
    }
    
    private void sendMail(){
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.server", "sas-mail.swiss-as.com");
        properties.put("mail.smtp.auth", "true");
        Session session = Session.getDefaultInstance(new Properties(), null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SwissAsStorage.getInstance().getFourLetterCode() + "@swiss-as.com"));
            msg.addRecipient(RecipientType.TO, new InternetAddress(SwissAsStorage.getInstance().getFourLetterCode() + "@swiss-as.com"));
            msg.setSubject("QA test");
            msg.setText("my nice text goes here");
            Transport.send(msg);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private ReturnResult showTrafficLightDialog() {
        ConfirmationDialog confirmationDialog = new ConfirmationDialog(this.trafficLightPanel.getTrafficDetails());
        confirmationDialog.show();
        int messageResult = confirmationDialog.getExitCode();
        if(messageResult == DialogWrapper.NEXT_USER_EXIT_CODE){
            this.trafficLightPanel.setInformWhenReady(this.checkinProjectPanel);
        }else if(messageResult == ConfirmationDialog.FIX_RELEASE_EXIT_CODE){
            if (!this.checkinProjectPanel.getCommitMessage().contains(FIX_RELEASE_BLOCKER)) {
                this.checkinProjectPanel.setCommitMessage(FIX_RELEASE_BLOCKER + " \n\n" + this.checkinProjectPanel.getCommitMessage());
            }
            messageResult = DialogWrapper.OK_EXIT_CODE;
        }

        return messageResult == DialogWrapper.OK_EXIT_CODE ? ReturnResult.COMMIT : ReturnResult.CLOSE_WINDOW;
    }
}
