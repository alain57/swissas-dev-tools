package com.swissas.ui;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;

/**
 * own Editor class to prevent typing all this in other classes
 * @author Tavan Alain
 */
public class JavaEditorTextField extends EditorTextField {
	
	public JavaEditorTextField(Project project){
		super(project, JavaFileType.INSTANCE);
	}
	
	@Override
	protected @NotNull EditorEx createEditor() {
		EditorEx editor = super.createEditor();
		editor.setVerticalScrollbarVisible(true);
		editor.setHorizontalScrollbarVisible(true);
		EditorSettings settings = editor.getSettings();
		settings.setAutoCodeFoldingEnabled(false);
		settings.setAllowSingleLogicalLineFolding(false);
		return editor;
	}
}
