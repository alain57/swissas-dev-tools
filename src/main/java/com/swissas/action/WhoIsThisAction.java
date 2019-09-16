package com.swissas.action;

import java.util.Objects;
import java.util.Optional;


import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
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
		PsiFile file = PsiManager.getInstance(Objects.requireNonNull(editor.getProject())).findFile(((EditorEx)editor).getVirtualFile());
		PsiElement element = Objects.requireNonNull(file).findElementAt(editor.getCaretModel().getOffset());
		String errorText = null;
		String authorString = null;
		String text = Optional.ofNullable(element).map(PsiElement::getText).orElse("");
		if(!text.contains(" ") && text.length() >= 3 && text.length() <= 4){
			//seems to be a lc
			authorString = text;
		}else {
			authorString = Messages.showInputDialog("Please fill a Letter Code", "letter code information", null);
		}
		showLetterCodeInformation(authorString, errorText);
	}
	
}
