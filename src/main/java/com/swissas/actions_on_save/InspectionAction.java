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
	SUPPRESS_ANNOTATION("Remove unused suppress warning annotation");
	
	private final String text;
	private final SwissAsStorage storage;
	
	InspectionAction(String text){
		this.text = text;
		this.storage = SwissAsStorage.getInstance();
	}
	
	public String getText(){
		return this.text;
	}
	
	public boolean isEnabled() {
		switch (this) {
			case MISSING_AUTHOR:
				return this.storage.isFixMissingAuthor();
			case MISSING_OVERRIDE_ANNOTATION:
				return this.storage.isFixMissingOverride();
			case UNQUALIFIED_FIELD_ACCESS:
				return this.storage.isFixMissingThis();
			case SUPPRESS_ANNOTATION:
				return this.storage.isFixUnusedSuppressWarning();
			default:
				throw new IllegalArgumentException("case not defined");
		}
	}
	
}
