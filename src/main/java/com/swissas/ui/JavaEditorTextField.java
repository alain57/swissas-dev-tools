package com.swissas.ui;

import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;

/**
 * own Editor class to prevent typing all this in other classes
 * @author Tavan Alain
 */
public class JavaEditorTextField extends EditorTextField {
	
	public JavaEditorTextField(Project project){
		super(project, StdFileTypes.JAVA);
	}
	
	@Override
	protected EditorEx createEditor() {
		EditorEx editor = super.createEditor();
		editor.setVerticalScrollbarVisible(true);
		editor.setHorizontalScrollbarVisible(true);
		EditorSettings settings = editor.getSettings();
		settings.setAutoCodeFoldingEnabled(false);
		settings.setAllowSingleLogicalLineFolding(false);
		return editor;
	}
}
