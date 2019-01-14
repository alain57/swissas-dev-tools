package com.swissas.widget;

import com.intellij.AppTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.MemoryUsagePanel;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.swissas.actions_on_save.DoOnSave;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 *  The main class of the plugin that installs the traffic light on the bottom of the IDE
 *  It also register the DoOnSave, behavior and checks the traffic light server all 30 seconds
 *
 *
 * @author TALA
 */

public class SwissAsWidget implements ProjectComponent {
    private static final String BEFORE = "before ";
    private static final String TRAFFIC_LIGHT_CHECKER = "trafficLightChecker";
    private final Project project;
    private IdeFrame ideFrame;
    private final TrafficLightPanel trafficLightPanel;
    private static final String WIDGET_NAME = "trafficLight";
    public static final Logger LOGGER = Logger.getInstance(SwissAsWidget.class);
    

    public SwissAsWidget(Project project) {
        this.project = project;
        this.trafficLightPanel = new TrafficLightPanel(project);
    }

    @Override
    public void initComponent() {
        //register the 
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = bus.connect();
        connection.subscribe(AppTopics.FILE_DOCUMENT_SYNC,  new DoOnSave());
    }
    

    @Override
    @NotNull
    @NonNls
    public String getComponentName() {
        return WIDGET_NAME;
    }

    @Override
    public void projectOpened() {
        addWidgetToFrame();
    }

    @Override
    public void projectClosed() {
        final StatusBar statusBar = this.ideFrame.getStatusBar();
        if (statusBar != null) {
            statusBar.removeWidget(TrafficLightPanel.WIDGET_ID);
        }
        Disposer.dispose(this.trafficLightPanel);
    }
    
    
    private void addWidgetToFrame(){
        this.ideFrame = WindowManager.getInstance().getIdeFrame(this.project);
        final StatusBar statusBar = this.ideFrame.getStatusBar();
        statusBar.addWidget(this.trafficLightPanel, BEFORE + MemoryUsagePanel.WIDGET_ID);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                SwissAsWidget.this.trafficLightPanel.refreshContent();
            }
        };
        Timer timer = new Timer(TRAFFIC_LIGHT_CHECKER);
        timer.schedule(timerTask, 30, 30_000);
    }

}
