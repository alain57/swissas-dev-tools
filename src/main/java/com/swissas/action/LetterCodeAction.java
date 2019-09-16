package com.swissas.action;

import java.util.Map;

import javax.swing.JEditorPane;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.swissas.util.SwissAsStorage;
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
	
	void showLetterCodeInformation(String authorString, String errorText){
		String authorTxt = null;
		if(authorString != null){
			authorString = authorString.toUpperCase();
			Map<String, String> userMap = SwissAsStorage.getInstance().getUserMap();
			if(userMap.containsKey(authorString)){
				authorTxt = userMap.get(authorString);
			}else{
				errorText = "Could not find \"" + authorString + "\" in the internal phone book";
			}
		}
		JEditorPane pane = new JEditorPane("text/html", authorTxt == null ? errorText : authorTxt);
		
		ComponentPopupBuilder componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(pane, null);
		JBPopup popup = componentPopupBuilder.createPopup();
		popup.showInFocusCenter();
	}
}
