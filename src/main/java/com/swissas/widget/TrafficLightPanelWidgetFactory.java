package com.swissas.widget;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.swissas.util.NetworkUtil;
import com.swissas.util.ProjectUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * The main class of the plugin that installs the traffic light on the bottom of the IDE
 * It also checks the traffic light all 30 seconds, and the user list every day
 *
 * @author Tavan Alain
 */

public class TrafficLightPanelWidgetFactory implements StatusBarWidgetFactory {
	private final Properties properties;
	private final SwissAsStorage swissAsStorage;
	private Project project;
	
	public TrafficLightPanelWidgetFactory() {
		this.properties = new Properties();
		this.swissAsStorage = SwissAsStorage.getInstance();
	}
	
	private void setupSchedulerTask() {
		Timer retrieveUserDataTimer = new Timer("UserTimer");
		TimerTask refreshUserMapTimerTask = new TimerTask() {
			@Override
			public void run() {
				if(ProjectUtil.getInstance().isAmosProject(TrafficLightPanelWidgetFactory.this.project)) {
					NetworkUtil.getInstance().refreshUserMap();
				}
			}
		};
		retrieveUserDataTimer.schedule(refreshUserMapTimerTask, 30, 24 * 60 * 60_000L);
	}
	
	
	private void fillSharedProperties() {
		if(ProjectUtil.getInstance().isAmosProject(this.project)) {
			Module shared = ProjectUtil.getInstance().getShared();
			VirtualFile sourceRoots = ModuleRootManager.getInstance(shared).getSourceRoots()[0];
			VirtualFile propertyFile = sourceRoots.findFileByRelativePath("amos/share/multiLanguage/Standard.properties");
			try {
				if (propertyFile != null) { //if null then older amos release maybe add this case in the future... For now don't handle it.
					FileInputStream in = new FileInputStream(propertyFile.getPath());
					this.properties.load(in);
					in.close();
					this.swissAsStorage.setNewTranslation(true);
				}
			} catch (IOException e) {
				Logger.getInstance("Swiss-as").error(e);
			}
			setupSchedulerTask();
			this.swissAsStorage.setShareProperties(this.properties);
		}
	}
	
	@Override
	public @NotNull @NonNls String getId() {
		return TrafficLightPanel.WIDGET_ID;
	}
	
	@Override
	public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
		return TrafficLightPanel.WIDGET_DISPLAY_NAME;
	}
	
	@Override
	public boolean isAvailable(@NotNull Project project) {
		return ProjectUtil.getInstance().isAmosProject(project);
	}
	
	@Override
	public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
		this.project = project;
		fillSharedProperties();
		return new TrafficLightPanel(project);
	}
}
