package com.swissas.handler;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.Language;
import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

/**
 * It will find usage of a Translation key used in the Properties file
 *
 * @author Tavan Alain
 */

public class PropertiesKeyToUsage implements GotoDeclarationHandler {

	@Nullable
	@Override
	public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
		if(sourceElement != null) {
			IElementType elementType = sourceElement.getNode().getElementType();
			Language language = elementType.getLanguage();
			if (PropertiesLanguage.INSTANCE.equals(language) && elementType.toString().contains(
					"KEY_CHARACTERS")) {
				PsiFile currentMessageFile = sourceElement.getContainingFile()
				                                          .getContainingDirectory()
				                                          .findFile("_Messages.java");
				PsiField correspondingField = PsiTreeUtil
						.collectElementsOfType(currentMessageFile, PsiField.class).
								stream().filter(e -> e.getText().contains(sourceElement.getText()))
						.findFirst().orElse(null);
				if (correspondingField != null) {
					//TODO: result is somehow ugly... not the same style as the references from _Message file. Need to find out what is going wrong here.
					return ReferencesSearch.search(correspondingField).findAll().stream()
					                       .map(PsiReference::getElement)
					                       .toArray(PsiElement[]::new);
				}
			}
		}
		return new PsiElement[0];
	}
}
