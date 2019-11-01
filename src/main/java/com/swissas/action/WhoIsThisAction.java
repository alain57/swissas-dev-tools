package com.swissas.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.swissas.dialog.EditableDialogChooser;
import com.swissas.util.ShowLetterCodeInformation;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Class for showing more information about a 4LC
 * @author TALA
 */
public class WhoIsThisAction extends LetterCodeAction {
	protected WhoIsThisAction() {
		super();
	}
	
	@Override
	protected void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext){
		String[] choices = SwissAsStorage.getInstance().getUserMap().keySet().stream().sorted().toArray(String[]::new);
		EditableDialogChooser dialogChooser = new EditableDialogChooser("Select/Type a Letter Code", "Letter Code Information", choices);
		String authorString = dialogChooser.getInputValue();
		if(authorString != null) {
			ShowLetterCodeInformation.displayInformation(authorString, null);
		}
	}
	
}
