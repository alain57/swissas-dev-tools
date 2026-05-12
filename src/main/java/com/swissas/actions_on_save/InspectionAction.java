package com.swissas.actions_on_save;

import com.swissas.util.SwissAsStorage;

/**
 * the different inspection action that the plugin handle
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */

public enum InspectionAction {
	
	UNQUALIFIED_FIELD_ACCESS("Add this to field access"),
	MISSING_OVERRIDE_ANNOTATION("Add missing @Override annotations"),
	MISSING_AUTHOR("add missing @Author"),
	SUPPRESS_ANNOTATION("Remove unused suppress warning annotation"),
	USE_TEAM_AUTHOR("Replace Author with team author")
	;
	
	private final String text;
	
	InspectionAction(String text){
		this.text = text;
	}
	
	public String getText(){
		return this.text;
	}
	
	public boolean isEnabled() {
		SwissAsStorage storage = SwissAsStorage.getInstance();
		if (storage == null) {
			return false;
		}
        return switch (this) {
            case MISSING_AUTHOR -> storage.isFixMissingAuthor();
            case MISSING_OVERRIDE_ANNOTATION -> storage.isFixMissingOverride();
            case UNQUALIFIED_FIELD_ACCESS -> storage.isFixMissingThis();
            case SUPPRESS_ANNOTATION -> storage.isFixUnusedSuppressWarning();
            case USE_TEAM_AUTHOR -> storage.isConvertToTeam();
            default -> throw new IllegalArgumentException("case not defined");
        };
	}
	
}
