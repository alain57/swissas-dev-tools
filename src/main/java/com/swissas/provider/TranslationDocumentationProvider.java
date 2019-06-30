package com.swissas.provider;

import java.awt.Window;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.intellij.lang.java.JavaDocumentationProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Documentation provider to handle some specific Swiss AS stuff
 * For example translations
 *
 * @author Tavan Alain
 */

public class TranslationDocumentationProvider extends JavaDocumentationProvider {
	private static final List<String> MULTILANG_CLASSES = Arrays.asList("MultiLangText", "MultiLangToolTip");
	private String translationToSearch = null;
	Project activeProject = null;
	String mainPropertiesContent = null;
	
	
	
	@Override
	@Nullable
	public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
		getNeededVariables();
		if(isSasMultiLang(element)) {
			PsiFile currentTranslationFile = element.getContainingFile().getContainingDirectory().findFile("Standard.properties");
			String translation = getTranslationFromContent(currentTranslationFile.getText(), this.translationToSearch);
			return replaceReferences(translation);
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
		Module shared = Stream.of(ModuleManager.getInstance(this.activeProject).getModules()).filter(e -> e.getName().contains("shared")).findFirst().orElse(null);
		VirtualFile sourceRoots = ModuleRootManager.getInstance(shared).getSourceRoots()[0];
		VirtualFile propertieFile = sourceRoots.findFileByRelativePath("amos/share/multiLanguage/Standard.properties");
		this.mainPropertiesContent = PsiManager.getInstance(this.activeProject).findFile(propertieFile).getText();
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
			String repl = null;
			String link = phrase.substring(index + 1, index2).trim();
			int sepId = link.indexOf('.');
			if (sepId == -1) {
				repl = getTranslationFromContent(this.mainPropertiesContent, link);
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
