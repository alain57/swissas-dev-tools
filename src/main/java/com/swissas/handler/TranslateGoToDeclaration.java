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
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.swissas.provider.TranslationDocumentationProvider;
import org.jetbrains.annotations.Nullable;

/**
 * class that replace the go to declaration for multilang stuff in order to go directly to the correct line of the standard file
 *
 * @author Tavan Alain
 */

class TranslateGoToDeclaration implements GotoDeclarationHandler {
		
	@Nullable
	@Override
	public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
		IElementType elementType = sourceElement.getNode().getElementType();
		Language language = elementType.getLanguage();

		if (language.equals(JavaLanguage.INSTANCE) && elementType.toString().equals("IDENTIFIER") && TranslationDocumentationProvider.isSasMultiLang(sourceElement)) {
			PsiFile currentPropertiesFile = sourceElement.getContainingFile().getContainingDirectory().findFile("Standard.properties");
			if(currentPropertiesFile != null) { //when null then we may be on old stable version without properties file
				PropertiesFile propertiesFile = (PropertiesFile) currentPropertiesFile;
				PsiElement psiElement = Optional.ofNullable(propertiesFile.findPropertyByKey(sourceElement.getText())).map(IProperty::getPsiElement).orElse(null);
				if (psiElement != null) {
					Property property = (Property) psiElement;
					return new PsiElement[]{property.getLastChild()};
				}
			}
		}
		return new PsiElement[0];
	}
}
