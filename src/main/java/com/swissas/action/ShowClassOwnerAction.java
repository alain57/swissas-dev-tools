package com.swissas.action;

import java.util.Objects;


import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.ShowLetterCodeInformation;
import org.jetbrains.annotations.Nullable;

/**
 * Class that displays information about the class owner
 *
 * @author Tavan Alain
 */

class ShowClassOwnerAction extends LetterCodeAction {

	protected ShowClassOwnerAction() {
		super();
	}
	
	@Override
	protected void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext){
		PsiFile file = PsiManager.getInstance(Objects.requireNonNull(editor.getProject())).findFile(((EditorEx)editor).getVirtualFile());
		String errorText = null;
		String authorString = null;
		//find the author
		PsiDocTag author = PsiTreeUtil.collectElementsOfType(file, PsiDocTag.class).stream()
				.filter(e -> e.getName().equalsIgnoreCase("author")).findFirst().orElse(null);
		if(author != null){
			authorString = author.getFirstChild().getNextSibling().getNextSibling().getText(); //author is the entire line, the author tag is the first child, the next is a blank and the next is the letter code
		}else {
			errorText = "The plugin was not able to find the class author code";
		}
		ShowLetterCodeInformation.displayInformation(authorString, errorText);
	}
}
