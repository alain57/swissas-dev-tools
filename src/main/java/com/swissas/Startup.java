package com.swissas;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.swissas.actions_on_save.Processor;
import com.swissas.actions_on_save.SaveActionManager;
import org.jetbrains.annotations.NotNull;

import static com.intellij.AppTopics.FILE_DOCUMENT_SYNC;

public class Startup implements StartupActivity {
	
	@Override
	public void runActivity(@NotNull Project project) {
		registerSaveActionStuff();
		
	}
	
	public void registerSaveActionStuff() {
		SaveActionManager manager = SaveActionManager.getInstance();
		manager.addProcessors(Processor.stream());
		
		MessageBus bus = ApplicationManager.getApplication().getMessageBus();
		MessageBusConnection connection = bus.connect();
		connection.subscribe(FILE_DOCUMENT_SYNC, manager);
	}
}
