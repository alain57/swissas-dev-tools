package com.swissas.widget;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.swissas.config.SwissAsConfig;
import com.swissas.util.NetworkUtil;
import com.swissas.util.ProjectUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jsoup.select.Elements;

import static com.swissas.util.Constants.BLINKING;
import static com.swissas.util.Constants.GREEN;
import static com.swissas.util.Constants.OFF;
import static com.swissas.util.Constants.ON;
import static com.swissas.util.Constants.RED;
import static com.swissas.util.Constants.YELLOW;

/**
 * Traffic light panel 
 * it will tun on or blink the different lights depending on the read value
 *
 * @author Tavan Alain
 */

public class TrafficLightPanel extends JPanel implements CustomStatusBarWidget {

    enum State{
        ON,
        OFF,
        BLINK

    }

    @NonNls
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
    @NonNls
    private static final ResourceBundle URL_BUNDLE = ResourceBundle.getBundle("urls");

    private static final int RADIUS_VERTICAL = 4;
    private static final int RADIUS_HORIZONTAL = 6;
    private static final int BORDER_VERTICAL = 1;
    private static final int BORDER_HORIZONTAL = 2;
    @NonNls
    public static final String WIDGET_ID = "com.swissas.widget.TrafficLightPanel";

    private boolean informWhenReady;
    private CheckinProjectPanel checkinProjectPanel;
    private Project project;
    private final Bulb green;
    private final Bulb yellow;
    private final Bulb red;
    private final JPanel lamps;
    private boolean isRedOrYellowOn = false;
    private String trafficDetails;

    private final Map<String, String> status = new HashMap<>();
    private final SwissAsStorage swissAsStorage;
    private final String clickUrl;



    TrafficLightPanel(Project project) {
        this.project = project;
        this.swissAsStorage = SwissAsStorage.getInstance();
        this.clickUrl = URL_BUNDLE.getString("url.trafficlight.click");
        this.lamps = new JPanel();
        this.green =  new Bulb(JBColor.GREEN);
        this.yellow = new Bulb(JBColor.YELLOW);
        this.red = new Bulb(JBColor.RED);
        this.lamps.add(this.red);
        this.lamps.add(this.yellow);
        this.lamps.add(this.green);
        add(this.lamps);
        setOrientation();

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                showTrafficDetailsNotificationBubble();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if(TrafficLightPanel.this.swissAsStorage.getFourLetterCode().isEmpty()){
                    ShowSettingsUtil.getInstance().showSettingsDialog(TrafficLightPanel.this.project, SwissAsConfig.class);
                }
            }
        });
    }

    public void changeProject(Project project) {
        if(this.project == null && project != null || this.project.equals(project)) {
            this.project = project;
            refreshContent();
        }
    }

    private void openTrafficDetailLink(HyperlinkEvent event){
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
            URL url = event.getURL();
            if (url != null) {
                BrowserUtil.browse(url);
            } else if(!this.swissAsStorage.getFourLetterCode().isEmpty()){
                BrowserUtil.browse(this.clickUrl + this.swissAsStorage.getFourLetterCode());
            }
        }
    }

    private void showTrafficDetailsNotificationBubble() {
        if(this.trafficDetails != null) {
            Balloon balloon = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(this.trafficDetails, MessageType.INFO, this::openTrafficDetailLink)
                    .setCloseButtonEnabled(true)
                    .setDisposable(this.project)
                    .setHideOnAction(true)
                    .setHideOnClickOutside(true)
                    .setHideOnLinkClick(true)
                    .setHideOnKeyOutside(true)
                    .setFadeoutTime(3_000)
                    .createBalloon();
            balloon.show(RelativePoint.getCenterOf(this.getComponent()), Balloon.Position.above);
        }
    }

    public void setOrientation() {
        int radius =  this.swissAsStorage.isHorizontalOrientation() ? RADIUS_HORIZONTAL : RADIUS_VERTICAL;
        int border = this.swissAsStorage.isHorizontalOrientation() ? BORDER_HORIZONTAL : BORDER_VERTICAL;
        this.green.setRadiusAndBorder(radius, border);
        this.yellow.setRadiusAndBorder(radius, border);
        this.red.setRadiusAndBorder(radius, border);
        this.lamps.setLayout(this.swissAsStorage.isHorizontalOrientation() ? new GridLayout(1,3) : new GridLayout(3,1));
    }

    void refreshContent(){
        if(this.swissAsStorage.getFourLetterCode().isEmpty()){
            
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(RESOURCE_BUNDLE.getString("4lc.not.configured"), 
                    MessageType.ERROR, null).createBalloon().
                    show(RelativePoint.getCenterOf(this.getComponent()), Balloon.Position.above);
        }else {
            readTrafficValues();
            this.green.changeState(getStateForColor(GREEN));
            this.red.changeState(getStateForColor(RED));
            this.yellow.changeState(getStateForColor(YELLOW));
        }
        this.isRedOrYellowOn = !State.OFF.equals(getStateForColor(RED)) || !State.OFF.equals(getStateForColor(YELLOW));
        if(this.informWhenReady) {
            displayCommitDialogWhenReady();
        }
    }
    


    private void displayCommitDialogWhenReady(){
        if(!this.isRedOrYellowOn){
            this.informWhenReady = false;
            Runnable showCommit = () -> AbstractVcsHelper.getInstance(this.project).commitChanges(this.checkinProjectPanel.getSelectedChanges(), LocalChangeList.createEmptyChangeList(this.project, "")
                    , this.checkinProjectPanel.getCommitMessage(), null);
            //as we are in a background task, we need to run the commitChange window in the main thread therefore invokeLater
            ApplicationManager.getApplication().invokeAndWait(showCommit, ModalityState.NON_MODAL);
        }
    }

    private State getStateForColor(String color) {
        if(this.status.isEmpty()){
            return State.OFF;
        }
        switch (this.status.get(color)) {
            case ON:
                return State.ON;
            case BLINKING:
                return State.BLINK;
            default:
            case OFF:
                return State.OFF;
        }
    }
    
    private void readTrafficValues() {
        Elements tickerDetail = NetworkUtil.getInstance().getTrafficLightContent();
        this.status.clear();
        this.status.putAll(getSmartTrafficLightColor(tickerDetail));
        if(!State.OFF.equals(getStateForColor(RED)) || !State.OFF.equals(getStateForColor(YELLOW))) {
            this.trafficDetails = tickerDetail.html();
        }else {
            this.trafficDetails = null;
        }
    }

    public Map<String, String> getSmartTrafficLightColor(Elements tickerDetail) {
        Map<String, String> lampColors = new HashMap<>();
        Elements trs = tickerDetail.select("tr");
        String branchOfSelectedFile = this.project == null ? null : ProjectUtil.getInstance().getBranchOfSelectedEditor(this.project);
        if(trs.size() == 1 && trs.html().contains("happy.jpg")){
            //no issues
            lampColors.put("green", "on");
            lampColors.put("yellow", "off");
            lampColors.put("red", "off");
        } else if(branchOfSelectedFile != null){
            lampColors.put("green", "off");
            boolean issueOnSameBranch = trs.html().toLowerCase().contains(branchOfSelectedFile);
            lampColors.put("yellow", issueOnSameBranch ? "off" : "on");
            lampColors.put("red", issueOnSameBranch ? "on" : "off");
        }else {
            //not ready or no file opened
            lampColors.put("green", "off");
            lampColors.put("yellow", "off");
            lampColors.put("red", "off");
        }

        return lampColors;
    }

    
    public boolean isRedOrYellowOn() {
        return this.isRedOrYellowOn;
    }
    
    public void setInformWhenReady(CheckinProjectPanel checkinProjectPanel) {
        this.informWhenReady = true;
        this.checkinProjectPanel = checkinProjectPanel;
    }
    
    public String getTrafficDetails() {
        return this.trafficDetails;
    }
    
    @Override
    public JComponent getComponent() {
        return this;
    }

    @NotNull
    @Override
    public String ID() {
        return WIDGET_ID;
    }


    @Override
    public void install(@NotNull StatusBar statusBar) {

    }

    @Override
    public void dispose() {

    }
}
