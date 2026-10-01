package com.swissas.widget;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.swissas.util.ProjectUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * The factory that installs the traffic light on the bottom of the IDE.
 * <p>
 * This factory is application wide and shared by every opened project, therefore it must
 * stay stateless: everything project related lives in the {@link TrafficLightPanel} itself.
 *
 * @author Tavan Alain
 */

public class TrafficLightPanelWidgetFactory implements StatusBarWidgetFactory {
	
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
		return ProjectUtil.getInstance(project).isAmosProject();
	}
	
	@Override
	public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
		return new TrafficLightPanel(project);
	}
}
