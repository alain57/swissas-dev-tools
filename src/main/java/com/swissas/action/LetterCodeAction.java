package com.swissas.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract class to display information about letter code 
 * @author TALA
 */
abstract class LetterCodeAction extends EditorAction {
	LetterCodeAction(){
		super(null);
		setupHandler(new EditorActionHandler() {
			@Override
			protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
				final Runnable runnable = () -> executeWriteAction(editor, caret, dataContext);
				new EditorWriteActionHandler(){
					@Override
					public void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
						runnable.run();
					}
				}.doExecute(editor, caret, dataContext);
			}
		});
	}
	
	void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext){
		throw new RuntimeException("you need to override this method");
	}
}
