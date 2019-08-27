package com.swissas.actions_on_save;

import com.swissas.util.SwissAsStorage;

/**
 * the different inspection action that the plugin handle
 * (based on the code from the save action plugin)
 * @author Tavan Alain
 */

public enum InspectionAction {
	
	unqualifiedFieldAccess("Add this to field access"),
	missingOverrideAnnotation("Add missing @Override annotations"),
	missingAuthor("add missing @Author"),
	suppressAnnotation("Remove unused suppress warning annotation");
	
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
			case missingAuthor:
				return this.storage.isFixMissingAuthor();
			case missingOverrideAnnotation:
				return this.storage.isFixMissingOverride();
			case unqualifiedFieldAccess:
				return this.storage.isFixMissingThis();
			case suppressAnnotation:
				return this.storage.isFixUnusedSuppressWarning();
			default:
				throw new IllegalArgumentException("case not defined");
		}
	}
	
}
