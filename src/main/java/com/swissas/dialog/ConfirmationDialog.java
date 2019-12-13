package com.swissas.dialog;

import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.ui.HintHint;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.Html;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * The Confirmation Dialog for the pre commit check.
 * This dialog will display the warning message followed by the traffic light content.
 *
 * @author Tavan Alain
 */

public class ConfirmationDialog extends DialogWrapper {
    private static final String FIX_RELEASE_BLOCKER = "fix release blocker";
    private static final String FIX_RELEASE_BLOCKER_REMOVE_REGEX = "fix release blocker\\s*";
    private final        CheckinProjectPanel checkinProjectPanel;

    private final Action  whenReadyAction;
    private final JButton addRemoveFixRelease;
    private final String  trafficLightMessage;
    private boolean hasFixReleaseText;

    public ConfirmationDialog(String trafficLightMessage, CheckinProjectPanel checkinProjectPanel) {
        super(true);
        this.checkinProjectPanel = checkinProjectPanel;
        this.trafficLightMessage = HintUtil.prepareHintText(new Html(trafficLightMessage), new HintHint().setAwtTooltip(true));
        this.whenReadyAction = new DialogWrapperExitAction(ResourceBundle.getBundle("texts").getString("when.ready"), DialogWrapper.NEXT_USER_EXIT_CODE);
        this.addRemoveFixRelease = new JButton();
        this.addRemoveFixRelease.setAction(new AbstractAction() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                addRemoveFixReleaseBlockerText();
            }
        });
        setTitle(ResourceBundle.getBundle("texts").getString("commit.title"));
        setOKButtonText(ResourceBundle.getBundle("texts").getString("yes"));
        setCancelButtonText(ResourceBundle.getBundle("texts").getString("no"));
        setFixReleaseTitle();
        init();
    }

    private void setFixReleaseTitle(){
        this.hasFixReleaseText = this.checkinProjectPanel.getCommitMessage().contains(FIX_RELEASE_BLOCKER);
        String actionName = this.hasFixReleaseText ? ResourceBundle.getBundle("texts").getString("fix.remove_fix_release") 
                                                   : ResourceBundle.getBundle("texts").getString("fix.add_fix_release");
        this.addRemoveFixRelease.getAction().putValue(Action.NAME, actionName);
        repaint();
    }
    
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel content = new JPanel(new BorderLayout());
        JLabel commitMessage = new JLabel(ResourceBundle.getBundle("texts").getString("commit.with.trafficlight"));
        JEditorPane trafficMessage = new JEditorPane("text/html", this.trafficLightMessage);

        trafficMessage.setPreferredSize(new Dimension(550, 65));
        trafficMessage.setEditable(false);
        trafficMessage.addHyperlinkListener(event -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
                BrowserUtil.browse(event.getURL());
            }
        });
        content.add(commitMessage, BorderLayout.NORTH);
        content.add(new JBScrollPane(trafficMessage), BorderLayout.CENTER);
        return content;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{ getOKAction(), getAddRemoveFixRelease(), getCancelAction(), getWhenReadyAction()};
    }

    private Action getWhenReadyAction() {
        return this.whenReadyAction;
    }

    private Action getAddRemoveFixRelease(){
        return this.addRemoveFixRelease.getAction();
    }
    
    private void addRemoveFixReleaseBlockerText() {
        String message = this.checkinProjectPanel.getCommitMessage();
        if(this.hasFixReleaseText){
            String withoutFixRelease = message.replaceAll(FIX_RELEASE_BLOCKER_REMOVE_REGEX, "");
            this.checkinProjectPanel.setCommitMessage(withoutFixRelease);
            
        }else {
            this.checkinProjectPanel.setCommitMessage(FIX_RELEASE_BLOCKER + " \n\n" +
                                                      this.checkinProjectPanel
                                                              .getCommitMessage());
        }
        setFixReleaseTitle();
    }
}
