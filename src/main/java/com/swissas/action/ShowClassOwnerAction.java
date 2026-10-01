package com.swissas.action;

import java.util.Objects;


import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.ShowLetterCodeInformationHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Action that will display information about the class owner
 *
 * @author Tavan Alain
 */

class ShowClassOwnerAction extends LetterCodeAction {

	protected ShowClassOwnerAction() {
	}
	
	@Nullable
	private static String getAuthorValue(PsiDocTag author) {
		PsiElement value = author.getValueElement();
		if (value == null) {
			//author is the entire line, the author tag is the first child, the next is a blank sign followed by the letter code
			PsiElement blank = author.getFirstChild() == null ? null : author.getFirstChild().getNextSibling();
			value = blank == null ? null : blank.getNextSibling();
		}
		return value == null ? null : value.getText();
	}

	@Override
	protected void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext){
		VirtualFile virtualFile = editor.getVirtualFile();
		PsiFile file = virtualFile == null ? null
		                                   : PsiManager.getInstance(Objects.requireNonNull(editor.getProject())).findFile(virtualFile);
		String errorText = null;
		String authorString = null;
		//find the author
		PsiDocTag author = file == null ? null : PsiTreeUtil.collectElementsOfType(file, PsiDocTag.class).stream()
				.filter(e -> "author".equalsIgnoreCase(e.getName())).findFirst().orElse(null);
		if(author != null){
			authorString = getAuthorValue(author);
		}
		if(authorString == null) {
			errorText = "The plugin was not able to find the class author code";
		}
		ShowLetterCodeInformationHelper.displayInformation(editor.getProject(), authorString, errorText);
	}
}
