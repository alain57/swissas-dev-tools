package com.swissas.checkin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Confirmation Dialog for the pre commit check.
 * This dialog will display the warning message followed by the traffic light content.
 *
 * @author Tavan Alain
 */

public class ConfirmationDialog extends DialogWrapper {
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");

    private final Action whenReadyAction;

    private final String trafficLightMessage;

    ConfirmationDialog(String trafficLightMessage) {
        super(true);
        this.trafficLightMessage = trafficLightMessage;
        this.whenReadyAction = new DialogWrapperExitAction(RESOURCE_BUNDLE.getString("when.ready"), DialogWrapper.NEXT_USER_EXIT_CODE);
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
        JEditorPane dummy = new JEditorPane("text/html", this.trafficLightMessage);
        dummy.setSize(500, Short.MAX_VALUE);
        trafficMessage.setEditable(false);
        trafficMessage.setPreferredSize(new Dimension(400, (int) dummy.getPreferredSize().getHeight()));
        trafficMessage.setOpaque(false);
        trafficMessage.addHyperlinkListener(event -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
                BrowserUtil.browse(event.getURL());
            }
        });
        content.add(commitMessage, BorderLayout.NORTH);
        content.add(trafficMessage, BorderLayout.SOUTH);
        return content;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction(), getWhenReadyAction()};
    }

    private Action getWhenReadyAction() {
        return this.whenReadyAction;
    }


}
