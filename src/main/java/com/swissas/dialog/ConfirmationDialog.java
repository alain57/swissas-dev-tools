package com.swissas.dialog;

import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.HintHint;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.Html;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * The Confirmation Dialog for the pre commit check.
 * This dialog will display the warning message followed by the traffic light content.
 *
 * @author Tavan Alain
 */

public class ConfirmationDialog extends DialogWrapper {
    public static final int FIX_RELEASE_EXIT_CODE = 3;
    
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");

    private final Action whenReadyAction;
    private final Action fixReleaseAction;
    private final String trafficLightMessage;

    public ConfirmationDialog(String trafficLightMessage) {
        super(true);
        this.trafficLightMessage = HintUtil.prepareHintText(new Html(trafficLightMessage), new HintHint().setAwtTooltip(true));
        this.whenReadyAction = new DialogWrapperExitAction(RESOURCE_BUNDLE.getString("when.ready"), DialogWrapper.NEXT_USER_EXIT_CODE);
        this.fixReleaseAction = new DialogWrapperExitAction(RESOURCE_BUNDLE.getString("fix.release"), FIX_RELEASE_EXIT_CODE);
        setTitle(RESOURCE_BUNDLE.getString("commit.title"));
        setOKButtonText(RESOURCE_BUNDLE.getString("yes"));
        setCancelButtonText(RESOURCE_BUNDLE.getString("no"));
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel content = new JPanel(new BorderLayout());
        JLabel commitMessage = new JLabel(RESOURCE_BUNDLE.getString("commit.with.trafficlight"));
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
        return new Action[]{getOKAction(), getFixReleaseAction(), getCancelAction(), getWhenReadyAction()};
    }

    private Action getWhenReadyAction() {
        return this.whenReadyAction;
    }

    private Action getFixReleaseAction(){
        return this.fixReleaseAction;
    }
}