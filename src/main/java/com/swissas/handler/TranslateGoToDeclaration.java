package com.swissas.handler;

import java.util.Optional;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.swissas.provider.TranslationDocumentationProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Class replacing the go to declaration for Multilang stuff in order to go directly to the correct line of the standard file
 *
 * @author Tavan Alain
 */

class TranslateGoToDeclaration implements GotoDeclarationHandler {
		
	@Nullable
	@Override
	public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
		if(sourceElement != null) {
			IElementType elementType = sourceElement.getNode().getElementType();
			Language language = elementType.getLanguage();
			
			if (language.equals(JavaLanguage.INSTANCE) && elementType.toString()
			                                                         .equals("IDENTIFIER")
			    && TranslationDocumentationProvider.isSasMultiLang(sourceElement)) {
				PropertiesFile currentPropertiesFile = (PropertiesFile) sourceElement.getContainingFile()
				                                             .getContainingDirectory()
				                                             .findFile("Standard.properties");
				return Optional.ofNullable(currentPropertiesFile)
				        .map(f -> f.findPropertyByKey(sourceElement.getText()))
				        .map(IProperty::getPsiElement)
				        .map(Property.class::cast)
				        .map(PsiElement::getLastChild)
						.map(e -> new PsiElement[]{e})
				        .orElse(new PsiElement[0]);
			}
		}
		return new PsiElement[0];
	}
}
