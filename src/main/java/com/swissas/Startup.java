package com.swissas;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.swissas.actions_on_save.SaveActionManager;
import com.swissas.toolwindow.WarningContent;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Startup implements ProjectActivity {
	
	private static final String WARNINGS_SHOWN_ONCE_KEY = "swissas.warnings.toolwindow.shown";
	
	/**
	 * Simply makes sure the application level {@link SaveActionManager} exists.
	 * The manager registers itself (once) on the message bus using its own disposable,
	 * so nothing has to be registered nor unregistered here.
	 */
	@Nullable
	@Override
	public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
		SaveActionManager.getInstance();
		showWarningsToolWindowOnFirstOpening(project);
		return null;
	}
	
	/**
	 * Opens the tool window only the very first time a project is opened. Afterward the IDE
	 * remembers the user's choice (closed stays closed), so we never force it open again.
	 */
	private static void showWarningsToolWindowOnFirstOpening(Project project) {
		PropertiesComponent properties = PropertiesComponent.getInstance(project);
		if (properties.getBoolean(WARNINGS_SHOWN_ONCE_KEY)) {
			return;
		}
		ToolWindowManager manager = ToolWindowManager.getInstance(project);
		manager.invokeLater(() -> {
			ToolWindow toolWindow = manager.getToolWindow(WarningContent.ID);
			if (toolWindow != null) {
				properties.setValue(WARNINGS_SHOWN_ONCE_KEY, true);
				toolWindow.show();
			}
		});
	}
}
