package com.swissas.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.swissas.dialog.EditableDialogChooser;
import com.swissas.util.ShowLetterCodeInformationHelper;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Class for showing more information about a 4LC
 * @author Tavan Alain
 */
public class WhoIsThisAction extends LetterCodeAction {
	protected WhoIsThisAction() {
	}
	
	@Override
	protected void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext){
		EditableDialogChooser dialogChooser = new EditableDialogChooser(editor.getProject(),"Type a Letter Code", 
				"Letter Code Information", SwissAsStorage.getInstance().getUserMap().keySet());
		dialogChooser.show();
		String authorString = dialogChooser.getInputValue();
		if(authorString != null) {
			ShowLetterCodeInformationHelper.displayInformation(authorString, null);
		}
	}
}
