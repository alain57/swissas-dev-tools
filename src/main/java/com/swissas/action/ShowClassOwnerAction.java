package com.swissas.action;

import java.util.Map;

import javax.swing.JEditorPane;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TODO: write you class description here
 *
 * @author Tavan Alain
 */

public class ShowClassOwnerAction extends EditorAction {

	protected ShowClassOwnerAction() {
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
	
	private void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext){
		PsiFile file = PsiManager.getInstance(editor.getProject()).findFile(((EditorEx)editor).getVirtualFile());
		PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
		String authorTxt = null;
		String errorText = null;
		String psiText = null;
		if(element instanceof PsiDocToken){
			String text = element.getText();
			if(!text.contains(" ") && text.length() >= 3 && text.length() <= 4){
				//seems to be a lc
				psiText = text;
			}else {
				errorText = "The plugin can't decode \"" + text + "\" as Letter code.";
				//warn that the plugin can't decode the string as letter code
			}
		}else {
			//user clicked somewhere else, then find the author (ignore co-author
			PsiDocTag author = PsiTreeUtil.collectElementsOfType(file, PsiDocTag.class).stream()
					.filter(e -> e.getName().equalsIgnoreCase("author")).findFirst().orElse(null);
			if(author != null){
				psiText = author.getFirstChild().getNextSibling().getNextSibling().getText(); //author is the entire line, the author tag is the first child, the next is a blank and the next is the lettercode
			}else {
				errorText = "The plugin was not able to find the class author code";
			}
		}
		
		if(psiText != null){
			Map<String, String> userMap = SwissAsStorage.getInstance(editor.getProject()).getUserMap();
			if(userMap.containsKey(psiText)){
				authorTxt = userMap.get(psiText).toString();
			}else{
				errorText = "Could not find \"" + psiText + "\" in the internal phone book";
			}
		}
		JEditorPane pane = new JEditorPane("text/html", authorTxt == null ? errorText : authorTxt);

		ComponentPopupBuilder componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(pane, null);
		JBPopup popup = componentPopupBuilder.createPopup();
		popup.showInFocusCenter();
	}
}
