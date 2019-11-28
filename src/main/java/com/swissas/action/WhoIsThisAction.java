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
 * @author Tavan Alain
 */
public class WhoIsThisAction extends LetterCodeAction {
	protected WhoIsThisAction() {
		super();
	}
	
	@Override
	protected void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext){
		EditableDialogChooser dialogChooser = new EditableDialogChooser(editor.getProject(),"Type a Letter Code", 
				"Letter Code Information", SwissAsStorage.getInstance().getUserMap().keySet());
		dialogChooser.show();
		String authorString = dialogChooser.getInputValue();
		if(authorString != null) {
			ShowLetterCodeInformation.displayInformation(authorString, null);
		}
	}
}
