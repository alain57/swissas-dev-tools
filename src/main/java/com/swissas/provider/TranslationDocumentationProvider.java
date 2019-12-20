package com.swissas.provider;

import java.util.List;
import java.util.regex.Pattern;

import com.intellij.lang.java.JavaDocumentationProvider;
import com.intellij.lang.properties.psi.impl.PropertyValueImpl;
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
	private static final List<String> MULTILANG_CLASSES = List.of("MultiLangText", "MultiLangToolTip");
	private static final Pattern POSSIBLE_MULTILANG = Pattern.compile("[A-Z0-9_]+_TX?T(_\\d+)?");


	@Override
	@Nullable
	public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
		if(element instanceof PropertyValueImpl && isSasMultiLang(originalElement)) {
			return replaceReferences(((PropertyValueImpl)element).getText());
		}
		return super.getQuickNavigateInfo(element, originalElement);
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
				repl = SwissAsStorage.getInstance().getShareProperties().get(link).toString();
			}else {
				//do something else
				repl = "<i>special case for key: "+ link + "not implemented yet</i>";
			}
			phrase = phrase.substring(0, index) + repl + phrase.substring(index2 + 1);
			
		}
		return phrase;
	}		
	
	
	public static boolean isSasMultiLang(PsiElement element){
		boolean result = false;
		PsiElement elementToCheck = element;
		String text = element.getText();
		if(text != null && POSSIBLE_MULTILANG.matcher(text).find()){
			PsiFieldImpl field = null;
			if(elementToCheck instanceof PsiIdentifierImpl){
				elementToCheck = PsiTreeUtil.getParentOfType(elementToCheck, PsiReferenceExpression.class);
			}
			if(elementToCheck instanceof PsiReferenceExpressionImpl) {
				field = (PsiFieldImpl) ((PsiReferenceExpressionImpl) elementToCheck).resolve();
			}
			if(field != null) {
				result = MULTILANG_CLASSES.contains(field.getType().getPresentableText());
			}
		}
		return result;
	}
}
