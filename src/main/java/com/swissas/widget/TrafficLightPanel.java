package com.swissas.widget;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.ex.MultiLineLabel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.labels.BoldLabel;
import com.swissas.beans.BranchFailure;
import com.swissas.beans.Failure;
import com.swissas.beans.User;
import com.swissas.config.SwissAsConfig;
import com.swissas.enumerations.State;
import com.swissas.util.NetworkUtil;
import com.swissas.util.ProjectUtil;
import com.swissas.util.SwissAsStorage;
import icons.SwissAsIcons;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static com.swissas.util.Constants.GREEN;
import static com.swissas.util.Constants.OFF;
import static com.swissas.util.Constants.RED;
import static com.swissas.util.Constants.YELLOW;

/**
 * Traffic light panel 
 * it will tun on or blink the different lights depending on the read value
 *
 * @author Tavan Alain
 */

public class TrafficLightPanel extends JPanel implements CustomStatusBarWidget, UISettingsListener {

    private TimerTask refreshTrafficLightTimerTask;

    private static final int RADIUS_VERTICAL = 4;
    
    private static final int RADIUS_HORIZONTAL = 6;
    private static final int BORDER_VERTICAL = 1;
    private static final int BORDER_HORIZONTAL = 2;
    @NonNls
    public static final String WIDGET_ID = "trafficLightPanel";
    private StatusBar statusBar;
    
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
    private String currentBranch;
    private Timer retrieveTrafficLightTimer;
    
    TrafficLightPanel(Project project) {
        this.project = project;
        this.swissAsStorage = SwissAsStorage.getInstance();
        this.currentBranch = ProjectUtil.getInstance().getBranchOfFile(project, null);
        this.clickUrl = ResourceBundle.getBundle("urls").getString("url.trafficlight.click");
        this.lamps = new JPanel();
        this.green =  new Bulb(JBColor.GREEN);
        this.yellow = new Bulb(JBColor.YELLOW);
        this.red = new Bulb(JBColor.RED);
        this.lamps.add(this.red);
        this.lamps.add(this.yellow);
        this.lamps.add(this.green);
        add(this.lamps);
        setOrientation();
        this.project.getMessageBus().connect().subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                    @Override public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        refreshOnBranchChange(event.getNewFile());
                    }
                });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                showTrafficDetailsNotificationBubble();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showTrafficDetailsNotificationBubble();
                if(TrafficLightPanel.this.swissAsStorage.getFourLetterCode().isEmpty()){
                    ShowSettingsUtil.getInstance().showSettingsDialog(TrafficLightPanel.this.project, SwissAsConfig.class);
                }else {
                    showBreakMessage();
                }
            }
        });

        this.retrieveTrafficLightTimer = new Timer("trafficLightChecker");
        this.refreshTrafficLightTimerTask = new TimerTask() {
            @Override
            public void run() {
                    refreshContent();
            }
        };
        this.retrieveTrafficLightTimer.schedule(this.refreshTrafficLightTimerTask, 500, 30_000);
    }
    
    @Override
    public void uiSettingsChanged(@NotNull UISettings uiSettings) {
        refreshContent();
    }

    private void openTrafficDetailLink(HyperlinkEvent event){
        if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())){
            URL url = event.getURL();
            if (url != null) {
                BrowserUtil.browse(url);
            } else if(!this.swissAsStorage.getFourLetterCode().isEmpty()){
                BrowserUtil.browse(this.clickUrl + this.swissAsStorage.getFourLetterCode());
            }
        }
    }

    private void showBreakMessage() {
        List<BranchFailure> breaker = NetworkUtil.getInstance().getTrafficLightBreaker();
        System.out.println(breaker);

        JPanel contentPanel = new JPanel(new MigLayout(new LC().fill()));
        String fourLetterCode = TrafficLightPanel.this.swissAsStorage.getFourLetterCode();
        for (BranchFailure branchFailure : breaker) {
            // TODO TALA please uncomment it later
//            if (concerns(branchFailure.getCommitters(), fourLetterCode)) {
            JPanel branchePanel = new JPanel(new MigLayout(new LC().fillX()));
            JLabel brancheLabel = new BoldLabel(branchFailure.getJobs());
            branchePanel.add(brancheLabel, new CC().pushX().wrap());
            Color failureBackground = Color.LIGHT_GRAY;
            for (Failure failure : branchFailure.getFailures()) {
                JPanel failurePanel = new JPanel(new MigLayout(new LC().fillX()));
                failurePanel.setBackground(failureBackground);
                
                JPanel detailsPanel = new JPanel(new MigLayout(new LC().insets("0 0 0 0"), new AC().gap("0"), new AC().gap("0")));
                detailsPanel.setBackground(failureBackground);
                JLabel classNameLabel = getFailureClassName(failure);
                detailsPanel.add(classNameLabel, new CC().wrap());
                JLabel nameLabel = new MultiLineLabel(failure.getName());
                detailsPanel.add(nameLabel, new CC().gapBefore("35"));
    
                failurePanel.add(detailsPanel, new CC().alignX("left"));

                JBCheckBox lookCauseCkb = new JBCheckBox("Look for cause");
                lookCauseCkb.setSelected(SwissAsStorage.getInstance().hasFailure(failure));
                lookCauseCkb.setHorizontalTextPosition(SwingConstants.LEFT);
                lookCauseCkb.setBackground(failureBackground);
                lookCauseCkb.addActionListener(e -> {
                    if(lookCauseCkb.isSelected()){
                        SwissAsStorage.getInstance().addFailure(failure);
                    }else {
                        SwissAsStorage.getInstance().removeFailure(failure);
                    }
                    NetworkUtil.getInstance().informInfoTerm(failure.getId(), State.CHECKING);
                    updateLabelIcon(lookCauseCkb, classNameLabel);
                });
    
                failurePanel.add(lookCauseCkb, new CC().alignX("right"));
                
                branchePanel.add(failurePanel, new CC().wrap().growX());
            }

            contentPanel.add(branchePanel, new CC().wrap().growX());
//            }
        }

        JBScrollPane scrollPanel = new JBScrollPane(contentPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ComponentPopupBuilder componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(scrollPanel, null);
        componentPopupBuilder.setCancelOnClickOutside(false)
                             .setTitle("You broke one or multiple branches !")
                             .setRequestFocus(true)
                             .setFocusable(true)
                             .setMovable(false)
                             .setCancelOnOtherWindowOpen(false)
                             .setCancelOnWindowDeactivation(true)
                             .setCancelKeyEnabled(false)
                             .setShowBorder(true)
                             .setResizable(true)
                             .setCancelButton(new IconButton("Not involved", AllIcons.Actions.Close));


        JBPopup popup = componentPopupBuilder.setCancelOnWindowDeactivation(true).createPopup();
        popup.setSize(new Dimension(770, 400));
        Optional<JComponent> sourceComponentOptional = Optional.ofNullable(this.getComponent());
        if (sourceComponentOptional.isPresent()) {
            popup.showCenteredInCurrentWindow(this.project);
        } else {
            popup.showInFocusCenter();
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

    private String formatFailureName(String name) {
        StringBuilder sb = new StringBuilder(name);
        sb.append("<html><p style=\"widht:400px\">");
        sb.append(name);
        sb.append("</p></html>");
        return sb.toString();
    }

    private boolean concerns(String committersArray, String fourLC) {
        List<String> committers = Arrays.asList(committersArray.split(";"));
        Map<String, User> userMap = this.swissAsStorage.getUserMap();
        return SwissAsStorage.getInstance().isUser() && committers.contains(fourLC) || !SwissAsStorage.getInstance().isUser() && userMap.values().stream().anyMatch(u-> u.isInTeam(userMap.get(fourLC).getTeam()));
    }

    private JLabel getFailureClassName(Failure failure) {
        JLabel classNameLabel = new JLabel(failure.getClassName());
        if ("CHECKING".equals(failure.getState())) {
            classNameLabel.setIcon(SwissAsIcons.LOOK_FOR_CAUSE);
        } else if ("ALREADY_FIXED".equals(failure.getState())){
            classNameLabel.setIcon(SwissAsIcons.ISSUE_FIXED);
        } else {
            classNameLabel.setIcon(SwissAsIcons.ISSUE);
        }
        return classNameLabel;
    }

    private void updateLabelIcon(JBCheckBox lookCauseCkb, JLabel classNameLabel) {
        if (lookCauseCkb.isSelected()) {
           classNameLabel.setIcon(SwissAsIcons.LOOK_FOR_CAUSE);
        } else {
            classNameLabel.setIcon(SwissAsIcons.ISSUE);
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
        if(this.currentBranch == null || this.project.isDisposed() || !ProjectUtil.getInstance().isAmosProject(this.project)){
            return;
        }
        if(this.swissAsStorage.getFourLetterCode().isEmpty()){
            
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(ResourceBundle.getBundle("texts").getString("4lc.not.configured"), 
                    MessageType.ERROR, null).createBalloon().
                    show(RelativePoint.getCenterOf(this.getComponent()), Balloon.Position.above);
        }else {
            readTrafficValues();
            this.green.changeState(getStateForColor(GREEN));
            this.red.changeState(getStateForColor(RED));
            this.yellow.changeState(getStateForColor(YELLOW));
        }
        this.isRedOrYellowOn = !OFF.equals(getStateForColor(RED)) || !OFF.equals(getStateForColor(YELLOW));
        if(this.informWhenReady) {
            displayCommitDialogWhenReady();
        }
        this.statusBar.updateWidget(ID());
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

    @NotNull
    private String getStateForColor(String color) {
        if(this.status.isEmpty()){
            return OFF;
        }
        return this.status.getOrDefault(color, OFF);
    }
    
    private void readTrafficValues() {
        Elements tickerDetail = NetworkUtil.getInstance().getTrafficLightContent();
        this.status.clear();
        if(tickerDetail == null) {
            this.trafficDetails = "<b>network issue, check again later</b>";
            return;
        }
        this.status.putAll(getSmartTrafficLightColor(tickerDetail));
        if(!OFF.equals(getStateForColor(RED)) || !OFF.equals(getStateForColor(YELLOW))) {
            this.trafficDetails = tickerDetail.html();
        }else {
            this.trafficDetails = null;
        }
    }

    private void refreshOnBranchChange(VirtualFile file){
        String branch = ProjectUtil.getInstance().getBranchOfFile(this.project, file);
        if(branch != null && !branch.equals(this.currentBranch)){
            this.currentBranch = branch;
            this.retrieveTrafficLightTimer.cancel();
            this.retrieveTrafficLightTimer.purge();
            this.refreshTrafficLightTimerTask.cancel();
            this.refreshTrafficLightTimerTask = new TimerTask() {
                @Override
                public void run() {
                    refreshContent();
                }
            };
            this.retrieveTrafficLightTimer = new Timer("trafficLightChecker");
            this.retrieveTrafficLightTimer.schedule(this.refreshTrafficLightTimerTask, 0, 30_000);
        }
    }


    public Map<String, String> getSmartTrafficLightColor(Elements tickerDetail) {
        Map<String, String> lampColors = new HashMap<>();
        if(tickerDetail != null) {
            Elements trs = tickerDetail.select("tr");
            if (trs.size() == 1 && trs.html().contains("happy.jpg")) {
                //no issues
                lampColors.put("green", "on");
            } else if (this.currentBranch != null) {
                boolean issueOnSameBranch = trs.html().toLowerCase().contains(this.currentBranch);
                lampColors.put("yellow", issueOnSameBranch ? "off" : "on");
                lampColors.put("red", issueOnSameBranch ? "on" : "off");
            }
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
        this.statusBar = statusBar;
        this.project = statusBar.getProject();
        refreshContent();
    }

    @Override
    public void dispose() {
        this.currentBranch = null;
        this.status.clear();
        this.trafficDetails = null;
        
    }
}
