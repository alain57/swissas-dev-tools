package com.swissas.actions_on_save;

import com.swissas.TestApplication;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InspectionActionTest {
	
	@Test
	void actionsAreEnabledAccordingToTheSettings() {
		try (TestApplication application = TestApplication.install()) {
			var storage = application.getStorage();
			
			assertThat(InspectionAction.MISSING_AUTHOR.isEnabled()).isTrue();
			assertThat(InspectionAction.MISSING_OVERRIDE_ANNOTATION.isEnabled()).isTrue();
			assertThat(InspectionAction.UNQUALIFIED_FIELD_ACCESS.isEnabled()).isTrue();
			assertThat(InspectionAction.SUPPRESS_ANNOTATION.isEnabled()).isFalse();
			assertThat(InspectionAction.USE_TEAM_AUTHOR.isEnabled()).isFalse();
			
			storage.setFixMissingAuthor(false);
			storage.setFixMissingOverride(false);
			storage.setFixMissingThis(false);
			storage.setFixUnusedSuppressWarning(true);
			storage.setConvertToTeam(true);
			
			assertThat(InspectionAction.MISSING_AUTHOR.isEnabled()).isFalse();
			assertThat(InspectionAction.MISSING_OVERRIDE_ANNOTATION.isEnabled()).isFalse();
			assertThat(InspectionAction.UNQUALIFIED_FIELD_ACCESS.isEnabled()).isFalse();
			assertThat(InspectionAction.SUPPRESS_ANNOTATION.isEnabled()).isTrue();
			assertThat(InspectionAction.USE_TEAM_AUTHOR.isEnabled()).isTrue();
		}
	}
	
	@Test
	void everyActionHasADescription() {
		for (InspectionAction action : InspectionAction.values()) {
			assertThat(action.getText()).isNotBlank();
		}
	}
}
