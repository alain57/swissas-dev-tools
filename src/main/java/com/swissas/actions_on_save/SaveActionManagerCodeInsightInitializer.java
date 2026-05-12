package com.swissas.actions_on_save;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.ide.ApplicationInitializedListener;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SaveActionManagerCodeInsightInitializer implements ApplicationInitializedListener {

    @Nullable
    @Override
    public Object execute(@NotNull Continuation<? super Unit> continuation) {
        ApplicationManager.getApplication().invokeLater(() -> {
        SaveActionManager manager = ApplicationManager.getApplication().getService(SaveActionManager.class);

        if (manager != null) {
            manager.addProcessors(Processor.stream());

            ApplicationManager.getApplication().getMessageBus()
                    .connect()
                    .subscribe(FileDocumentManagerListener.TOPIC, manager);
        }
        });

        return Unit.INSTANCE;
    }
}