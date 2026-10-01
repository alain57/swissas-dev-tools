package com.swissas.actions_on_save;

import com.swissas.TestApplication;
import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SaveActionManagerTest {

	@Test
	void processingIsSubmittedToTheExecutorInsteadOfRunningInline() {
		try (TestApplication ignored = TestApplication.install()) {
			AtomicReference<Runnable> submittedTask = new AtomicReference<>();
			SaveActionManager manager = new SaveActionManager(submittedTask::set, false);

			manager.guardedProcessPsiFiles(mock(Project.class), new HashSet<>());

			assertThat(submittedTask.get()).isNotNull();
		}
	}
}
