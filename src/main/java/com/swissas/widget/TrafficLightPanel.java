package com.swissas.widget;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Alarm;
import com.swissas.config.SwissAsConfig;
import com.swissas.util.NetworkUtil;
import com.swissas.util.ProjectUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;
import org.jsoup.select.Elements;

import static com.swissas.util.Constants.GREEN;
import static com.swissas.util.Constants.OFF;
import static com.swissas.util.Constants.ON;
import static com.swissas.util.Constants.RED;
import static com.swissas.util.Constants.YELLOW;

/**
 * Traffic light panel 
 * it will turn on or blink the different lights depending on the read value
 *
 * @author Tavan Alain
 */

public class TrafficLightPanel extends JPanel implements CustomStatusBarWidget, UISettingsListener {

    public static final String WIDGET_ID = "trafficLightPanel";
    public static final String WIDGET_DISPLAY_NAME = "Swiss-As Traffic Light";

    private static final Logger LOGGER = Logger.getInstance("Swiss-as");

    private static final int RADIUS_VERTICAL = 4;
    
    private static final int RADIUS_HORIZONTAL = 6;
    private static final int BORDER_VERTICAL = 1;
    private static final int BORDER_HORIZONTAL = 2;

    private static final long REFRESH_DELAY_MS = 30_000L;
    private static final long USER_REFRESH_DELAY_MS = TimeUnit.DAYS.toMillis(1);

    private static final String SHARED_TRANSLATION_PATH = "amos/share/multiLanguage/Standard.properties";

    private final Project project;
    private StatusBar statusBar;
    
    private final SwissAsStorage swissAsStorage;
    private final Bulb green;
    private final Bulb yellow;
    private final Bulb red;
    private final JPanel lamps;
    private final Map<String, String> status = new ConcurrentHashMap<>();

    private final AtomicReference<String> currentBranch = new AtomicReference<>();

    private final Alarm refreshAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
    private final Alarm backgroundAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);

    private volatile boolean disposed = false;

    private boolean informWhenReady;
    private CheckinProjectPanel checkinProjectPanel;
    private volatile String trafficDetails;
    private final String clickUrl;
    
    private volatile boolean isRedOrYellowOn = false;

    public TrafficLightPanel(Project project) {
        this.project = project;
        this.swissAsStorage = SwissAsStorage.getInstance();
        this.clickUrl = ResourceBundle.getBundle("urls").getString("url.trafficlight.click");
        this.lamps = new JPanel();
        // stay transparent so the status bar background shows through (no gray box / repaint leftovers)
        setOpaque(false);
        this.lamps.setOpaque(false);
        this.green =  new Bulb(JBColor.GREEN);
        this.yellow = new Bulb(JBColor.YELLOW);
        this.red = new Bulb(JBColor.RED);
        this.lamps.add(this.red);
        this.lamps.add(this.yellow);
        this.lamps.add(this.green);
        add(this.lamps);
        setOrientation();
        project.getMessageBus().connect(this).subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        VirtualFile file = event.getNewFile();
                        if (file == null) return;

                        ApplicationManager.getApplication().executeOnPooledThread(() -> {
                            if (TrafficLightPanel.this.disposed || project.isDisposed()) return;
                            String branch = ProjectUtil.getInstance(project).getBranchOfFile(file);

                            if (branch != null && !branch.equals(TrafficLightPanel.this.currentBranch.get())) {
                                TrafficLightPanel.this.currentBranch.set(branch);
                                scheduleRefresh();
                            }
                        });
                    }
                }
        );

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                showTrafficDetailsNotificationBubble();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (TrafficLightPanel.this.swissAsStorage.getFourLetterCode().isEmpty()) {
                    ShowSettingsUtil.getInstance()
                            .showSettingsDialog(project, SwissAsConfig.class);
                }
            }
        });

        this.backgroundAlarm.addRequest(this::initProjectData, 0);
        scheduleUserRefresh(0);
    }
    
    @Override
    public void uiSettingsChanged(@NotNull UISettings uiSettings) {
        scheduleRefresh();
    }

    /**
     * Reads everything that needs the file system: it must not run on the EDT.
     */
    private void initProjectData() {
        if (this.disposed || this.project.isDisposed()) return;
        this.currentBranch.set(ProjectUtil.getInstance(this.project).getBranchOfFile(null));
        fillSharedProperties();
        scheduleRefresh();
    }

    private void fillSharedProperties() {
        ProjectUtil projectUtil = ProjectUtil.getInstance(this.project);
        if (!projectUtil.isAmosProject()) {
            return;
        }
        Module shared = projectUtil.getShared();
        if (shared == null) {
            return;
        }
        VirtualFile[] sourceRoots = ReadAction.computeBlocking(
                () -> ModuleRootManager.getInstance(shared).getSourceRoots());
        if (sourceRoots.length == 0) {
            return;
        }
        VirtualFile propertyFile = sourceRoots[0].findFileByRelativePath(SHARED_TRANSLATION_PATH);
        if (propertyFile == null) { //older amos release, nothing to do for now
            return;
        }
        Properties properties = new Properties();
        try (InputStream in = propertyFile.getInputStream()) {
            properties.load(in);
            this.swissAsStorage.setNewTranslation(true);
            this.swissAsStorage.setShareProperties(properties);
        } catch (IOException e) {
            LOGGER.warn("Unable to read " + propertyFile.getPath(), e);
        }
    }

    private void scheduleUserRefresh(long delay) {
        if (this.disposed) return;
        this.backgroundAlarm.addRequest(() -> {
            if (this.disposed || this.project.isDisposed()) return;
            if (ProjectUtil.getInstance(this.project).isAmosProject()) {
                NetworkUtil.getInstance().refreshUserMap();
            }
            scheduleUserRefresh(USER_REFRESH_DELAY_MS);
        }, delay);
    }

    private void scheduleRefresh() {
        if (this.disposed) return;

        this.refreshAlarm.cancelAllRequests();
        this.refreshAlarm.addRequest(this::refreshContentSafe, 0);
    }

    /**
     * Runs on the alarm pooled thread, then re-schedules itself so that the traffic light
     * really is refreshed every 30 seconds.
     */
    private void refreshContentSafe() {
        if (this.disposed || this.project.isDisposed()) return;

        try {
            if (!ProjectUtil.getInstance(this.project).isAmosProject()) return;

            if (this.swissAsStorage.getFourLetterCode().isEmpty()) {
                ApplicationManager.getApplication().invokeLater(this::showNotConfiguredPopup);
                return;
            }

            readTrafficValues();
            ApplicationManager.getApplication().invokeLater(this::applyTrafficValues);
        } finally {
            if (!this.disposed) {
                this.refreshAlarm.addRequest(this::refreshContentSafe, REFRESH_DELAY_MS);
            }
        }
    }

    private void applyTrafficValues() {
        if (this.disposed || this.statusBar == null) return;

        this.green.changeState(getStateForColor(GREEN));
        this.red.changeState(getStateForColor(RED));
        this.yellow.changeState(getStateForColor(YELLOW));

        this.isRedOrYellowOn =
                !OFF.equals(getStateForColor(RED)) ||
                !OFF.equals(getStateForColor(YELLOW));

        if (this.informWhenReady) {
            displayCommitDialogWhenReady();
        }

        this.statusBar.updateWidget(ID());
    }

    private void showNotConfiguredPopup() {
        if (this.disposed) return;
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(
                        ResourceBundle.getBundle("texts").getString("4lc.not.configured"),
                        MessageType.ERROR,
                        null
                )
                .createBalloon()
                .show(RelativePoint.getCenterOf(getComponent()), Balloon.Position.above);
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


    public Map<String, String> getSmartTrafficLightColor(Elements tickerDetail) {
        return computeLampColors(tickerDetail, this.currentBranch.get());
    }

    /**
     * Green when the build server is happy, red when the current branch is broken, yellow when only another branch is.
     */
    static Map<String, String> computeLampColors(Elements tickerDetail, String branch) {
        Map<String, String> lampColors = new HashMap<>();
        if(tickerDetail != null) {
            Elements trs = tickerDetail.select("tr");
            if (trs.size() == 1 && trs.html().contains("happy.jpg")) {
                lampColors.put(GREEN, ON);
            } else if (branch != null) {
                //the branch can contain upper case letters (ex : V12-10) when the html is read in lower case
                boolean issueOnSameBranch = trs.html().toLowerCase().contains(branch.toLowerCase());
                lampColors.put(YELLOW, issueOnSameBranch ? OFF : ON);
                lampColors.put(RED, issueOnSameBranch ? ON : OFF);
            }
        }

        return lampColors;
    }

    private String getStateForColor(String color) {
        return this.status.getOrDefault(color, OFF);
    }

    private void displayCommitDialogWhenReady() {
        if (!this.isRedOrYellowOn) {
            this.informWhenReady = false;
            if (this.checkinProjectPanel == null) {
                return;
            }

            Runnable showCommit = () ->
                    AbstractVcsHelper.getInstance(this.project).commitChanges(
                            this.checkinProjectPanel.getSelectedChanges(),
                            LocalChangeList.createEmptyChangeList(this.project, ""),
                            this.checkinProjectPanel.getCommitMessage(),
                            null
                    );

            ApplicationManager.getApplication()
                    .invokeAndWait(showCommit, ModalityState.nonModal());
        }
    }

    private void showTrafficDetailsNotificationBubble() {
        String details = this.trafficDetails;
        if (details == null) return;

        JEditorPane pane = new JEditorPane(
                "text/html",
                "<html><body style='font-size:14px;'>" + details + "</body></html>"
        );

        pane.setEditable(false);
        pane.setOpaque(false);
        pane.addHyperlinkListener(this::openTrafficDetailLink);

        Balloon balloon = JBPopupFactory.getInstance()
                .createBalloonBuilder(pane)
                .setCloseButtonEnabled(true)
                .setTitle("Swiss-as Traffic Light")
                .setDisposable(this.project)
                .setHideOnAction(true)
                .setHideOnClickOutside(true)
                .setHideOnLinkClick(true)
                .setHideOnKeyOutside(true)
                .setFadeoutTime(3000)
                .createBalloon();

        balloon.show(RelativePoint.getCenterOf(getComponent()), Balloon.Position.above);
    }

    private void openTrafficDetailLink(HyperlinkEvent event) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
            URL url = event.getURL();

            if (url != null) {
                BrowserUtil.browse(url);
            } else if (!this.swissAsStorage.getFourLetterCode().isEmpty()) {
                BrowserUtil.browse(this.clickUrl + this.swissAsStorage.getFourLetterCode());
            }
        }
    }

    public void setOrientation() {
        int radius = this.swissAsStorage.isHorizontalOrientation() ? RADIUS_HORIZONTAL : RADIUS_VERTICAL;
        int border = this.swissAsStorage.isHorizontalOrientation() ? BORDER_HORIZONTAL : BORDER_VERTICAL;

        this.green.setRadiusAndBorder(radius, border);
        this.yellow.setRadiusAndBorder(radius, border);
        this.red.setRadiusAndBorder(radius, border);

        this.lamps.setLayout(
                this.swissAsStorage.isHorizontalOrientation()
                        ? new GridLayout(1, 3)
                        : new GridLayout(3, 1)
        );
    }
    
    public boolean isRedOrYellowOn() {
        return this.isRedOrYellowOn;
    }
    
    public void setInformWhenReady(CheckinProjectPanel panel) {
        this.informWhenReady = true;
        this.checkinProjectPanel = panel;
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
    }

    @Override
    public void dispose() {
        this.disposed = true;
        this.refreshAlarm.cancelAllRequests();
        this.status.clear();
        this.trafficDetails = null;
        this.currentBranch.set(null);
        this.checkinProjectPanel = null;
        this.statusBar = null;
    }
}
