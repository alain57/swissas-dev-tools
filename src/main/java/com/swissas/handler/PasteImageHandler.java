package com.swissas.handler;

import java.awt.datatransfer.Transferable;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler;
import com.intellij.util.Producer;
import com.swissas.ui.DragDropTextPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasteImageHandler extends EditorActionHandler implements EditorTextInsertHandler {
	
	private final EditorActionHandler originalActionHandler;
	
	public PasteImageHandler(EditorActionHandler originalActionHandler){
		this.originalActionHandler = originalActionHandler;
	}
	
	@Override
	public void execute(Editor editor, DataContext dataContext, Producer<Transferable> producer) {
		Caret caret =  editor.getCaretModel().getPrimaryCaret();
		doExecute(editor, caret, dataContext);
	}
	
	@Override
	protected void doExecute(@NotNull Editor editor, @Nullable Caret caret,
	                         DataContext dataContext) {
		if(editor.getComponent() instanceof DragDropTextPane) {
			((DragDropTextPane) editor.getComponent()).pastePicture();
		}
		
		if (this.originalActionHandler != null) {
			this.originalActionHandler.execute(editor, null, dataContext);
		}
	}
}
