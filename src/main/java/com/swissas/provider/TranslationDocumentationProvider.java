package com.swissas.provider;

import java.awt.Window;
import java.util.Arrays;
import java.util.List;

import com.intellij.lang.java.JavaDocumentationProvider;
import com.intellij.lang.properties.psi.impl.PropertyValueImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Documentation provider to handle translations
 *
 * @author Tavan Alain
 */

public class TranslationDocumentationProvider extends JavaDocumentationProvider {
	private static final List<String> MULTILANG_CLASSES = Arrays.asList("MultiLangText", "MultiLangToolTip");
	private Project activeProject = null;


	@Override
	@Nullable
	public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
		if(isSasMultiLang(originalElement) && element instanceof PropertyValueImpl) {
			getNeededVariables();
			return replaceReferences(((PropertyValueImpl)element).getText());
		}
		return super.getQuickNavigateInfo(element, originalElement);
	}
	
	private void getNeededVariables(){
		if(this.activeProject == null){
			Project[] projects = ProjectManager.getInstance().getOpenProjects();
			for (Project project : projects) {
				Window window = WindowManager.getInstance().suggestParentWindow(project);
				if (window != null && window.isActive()) {
					this.activeProject = project;
					break;
				}
			}
		}
	}
	
	private String replaceReferences(String phrase) {
		if (phrase == null) {
			return null;
		}

		int index = 0;
		StringBuilder sb = new StringBuilder();
		while ((index = phrase.indexOf('@', index)) != -1) {
			int index2 = phrase.indexOf('@', index + 1);
			if (index2 == -1) {
				break;
			}
			String repl;
			String link = phrase.substring(index + 1, index2).trim();
			int sepId = link.indexOf('.');
			if (sepId == -1) {
				repl = SwissAsStorage.getInstance().getShareProperties().get(link).toString();
			}else {
				//do something else
				repl = "<i>special case for key: "+ link + "not implemented yet</i>";
			}
			sb.append(phrase, 0, index).append(repl).append(phrase.substring(index2 + 1));
			
		}
		return sb.toString();
	}		
	
	
	public static boolean isSasMultiLang(PsiElement element){
		boolean result = false;
		PsiElement elementToCheck = element;
		if(elementToCheck.getText() != null && elementToCheck.getText().matches("[A-Z0-9_]+_TX?T(_\\d+)?")){
			PsiFieldImpl field = null;
			if(elementToCheck instanceof PsiIdentifierImpl){
				elementToCheck = PsiTreeUtil.getParentOfType(elementToCheck, PsiReferenceExpression.class);
			}
			if(elementToCheck instanceof PsiReferenceExpressionImpl) {
				field = ((PsiFieldImpl) ((PsiReferenceExpressionImpl) elementToCheck).resolve());
			}
			if(field != null) {
				result = MULTILANG_CLASSES.contains(field.getType().getPresentableText());
			}
		}
		return result;
	}
}
