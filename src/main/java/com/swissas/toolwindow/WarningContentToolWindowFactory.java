package com.swissas.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swissas.util.ProjectUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Creates one {@link WarningContent} panel per project.
 * <p>
 * A {@link ToolWindowFactory} is instantiated only once for the whole application, so it
 * must never keep any project related state, and above all it must not be the UI itself.
 *
 * @author Tavan Alain
 */
public class WarningContentToolWindowFactory implements ToolWindowFactory {

	@Override
	public boolean shouldBeAvailable(@NotNull Project project) {
		return ProjectUtil.getInstance(project).isAmosProject();
	}

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		if (!ProjectUtil.getInstance(project).isAmosProject()) {
			return;
		}
		WarningContent warningContent = new WarningContent(project);
		Content content = ContentFactory.getInstance().createContent(warningContent, "", false);
		content.setDisposer(warningContent);
		toolWindow.getContentManager().addContent(content);
	}
}
