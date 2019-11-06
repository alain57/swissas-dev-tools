package com.swissas.checkin;


import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
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

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * The pre commit checking that will prevent committing with empty message, incorrect traffic light indicator
 * or if the definition of done is not respected.
 * 
 * @author Tavan Alain
 */
class PreCommitCheckingHandler extends CheckinHandler {

    public static final String FIX_RELEASE_BLOCKER = "fix release blocker";
    private static final Pattern START_WITH_SUPPORT_STRING = Pattern.compile("^(sc|story|case|sup)\\s\\d+", CASE_INSENSITIVE);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
    @NonNls
    private static final String TITLE = RESOURCE_BUNDLE.getString("commit.title");
    @NonNls
    private static final String EMPTY_COMMIT_MSG = RESOURCE_BUNDLE.getString("commit.without.message");
    
    private final Project project;
    private final CheckinProjectPanel checkinProjectPanel;
    
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
        boolean informOther = SwissAsStorage.getInstance().isPreCommitInformOther();
        boolean reviewNeeded = SwissAsStorage.getInstance().isPreCommitCodeReview();
        if(informOther || reviewNeeded){
            ImportantPreCommitsDone dialog = new ImportantPreCommitsDone(this.checkinProjectPanel);
            dialog.show();
            int exitCode = dialog.getExitCode();
            if((exitCode == DialogWrapper.NEXT_USER_EXIT_CODE && !sendMail()) || 
                    exitCode == DialogWrapper.CANCEL_EXIT_CODE){
                result = false;
            }
        }
        
        return result;
    }
    
    private boolean sendMail(){
        String message = this.checkinProjectPanel.getCommitMessage();
        Matcher matcher = START_WITH_SUPPORT_STRING.matcher(message);
        if(!matcher.find()) {
            Messages.showMessageDialog("Your commit message needs to start with one of following options: SC/CASE/STORY/SUP followed by case number", "Commit message invalid", Messages.getErrorIcon());
            return false;
        }
        Properties properties = System.getProperties();
        SwissAsStorage storage = SwissAsStorage.getInstance();
        properties.setProperty("mail.smtp.host", "sas-mail.swiss-as.com");
        Session session = Session.getDefaultInstance(properties, null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(storage.getMyMail()));
            InternetAddress[] internetAddresses = Stream.of(storage.getQaMail(), storage.getDocuMail(), storage.getSupportMail())
                    .map(this::generateAddress).filter(Objects::nonNull).toArray(InternetAddress[]::new);
            
            msg.addRecipients(RecipientType.TO, internetAddresses);
            msg.setSubject("Automatic User Interface Change");
            msg.setText(message);
            Transport.send(msg);
        }catch (Exception e){
            Messages.showMessageDialog(e.getMessage(), "Mail Could not Be Sent", Messages.getErrorIcon());
            e.printStackTrace();
            return false;
        }
        return true;
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
    
    /**
     * dummy method needed in order to generate an InternetAddress with a stream 
     * @param address
     * @return
     */
    private InternetAddress generateAddress(String address) {
        InternetAddress result = null;
        if(address != null) {
            try {
                result = new InternetAddress(address);
            } catch (AddressException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    
}
