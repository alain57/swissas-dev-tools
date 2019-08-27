package com.swissas.actions_on_save;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import static com.intellij.AppTopics.FILE_DOCUMENT_SYNC;
import static com.swissas.actions_on_save.SaveActionManager.LOGGER;

/**
 * The action that will execute the different inspection on save.
 * (based on the code from the save action plugin)
 *
 * @author Tavan Alain
 */

public class DoOnSave implements ApplicationComponent {
    private static final String COMPONENT_NAME = "Do On Save";
    
    @Override
    public void initComponent() {
        LOGGER.info("Starting component: " + COMPONENT_NAME);
        
        SaveActionManager manager = SaveActionManager.getInstance();
        manager.addProcessors(Processor.stream());
        
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = bus.connect();
        connection.subscribe(FILE_DOCUMENT_SYNC, manager);
    }
    
    @Override
    public void disposeComponent() {
        LOGGER.info("Stopping component: " + COMPONENT_NAME);
    }
    
    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }
    
}
