package com.swissas.provider;

import java.awt.Window;
import java.util.Arrays;
import java.util.List;

import com.intellij.lang.java.JavaDocumentationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Documentation provider to handle translations
 *
 * @author Tavan Alain
 */

class TranslationDocumentationProvider extends JavaDocumentationProvider {
	private static final List<String> MULTILANG_CLASSES = Arrays.asList("MultiLangText", "MultiLangToolTip");
	private String translationToSearch = null;
	private Project activeProject = null;


	@Override
	@Nullable
	public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
		
		getNeededVariables();
		PsiFile currentPropertiesFile = element.getContainingFile().getContainingDirectory().findFile("Standard.properties");
		if(isSasMultiLang(element)) {
			if(currentPropertiesFile != null) {
				String translation = getTranslationFromContent(currentPropertiesFile.getText(), this.translationToSearch);
				return replaceReferences(translation);
			}else {
				return "No Standard.properties defined in current working directory. Are you on older AMOS ? ";
			}
		}else{
			return super.getQuickNavigateInfo(element, originalElement);
		}
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
	
	private String getTranslationFromContent(String content, String key){
		if(content == null || content.isEmpty() || !content.contains(key)){
			return "<i>impossible to translate key " + key + "</i>";
		}
		return content.split(key + "=")[1].split("\n")[0];
	}

	private String replaceReferences(String phrase) {
		if (phrase == null) {
			return null;
		}

		int index = 0;
		while ((index = phrase.indexOf('@', index)) != -1) {
			int index2 = phrase.indexOf('@', index + 1);
			if (index2 == -1) {
				break;
			}
			String repl;
			String link = phrase.substring(index + 1, index2).trim();
			int sepId = link.indexOf('.');
			if (sepId == -1) {
				repl = SwissAsStorage.getInstance(this.activeProject).getShareProperties().get(link).toString();
			}else {
				//do something else
				repl = "<i>special case for key: "+ link + "not implemented yet</i>";
			}
			phrase = phrase.substring(0, index) + repl + phrase.substring(index2 + 1);
			
		}
		return phrase;
	}		
	
	
	private boolean isSasMultiLang(PsiElement elementAt){
		boolean result = false;
		try {
			String classNameUnderCaretPosition = ((PsiFieldImpl) elementAt).getType().getPresentableText();
			if (MULTILANG_CLASSES.contains(classNameUnderCaretPosition)) {
				this.translationToSearch = PsiTreeUtil.getChildOfType(elementAt, PsiIdentifier.class).getText();
				result = true;
			}
		}catch (Exception e){
			this.translationToSearch = null;
		}

		return result;
	}
}
