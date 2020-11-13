package com.swissas.contributor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.intellij.lang.properties.psi.impl.PropertyKeyImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * The PsiReferenceProvider that generate the reference between the standard property key and the 
 * _Message field
 *
 * @author Tavan Alain
 */
public class PropertyKeyToMessageProvider extends PsiReferenceProvider {
	private static final String MESS = "_Messages.java";
	private static final List<String> VALID_CLASSES = List.of("MultiLangText", "MultiLangToolTip");
	
	@Override
	@NotNull
	public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
	                                             @NotNull ProcessingContext context) {
		if(element instanceof PropertyKeyImpl) {
			PropertyKeyImpl key = (PropertyKeyImpl) element;
			return Optional.ofNullable(key.getContainingFile())
			                                .map(PsiFile::getContainingDirectory)
			                                .map(dir -> dir.findFile(MESS))
			                                .map(PsiJavaFile.class::cast)
			                                .stream()
			                                .flatMap(file -> Stream.of(file.getClasses()))
			                                .map(e -> e.findFieldByName(key.getText(), false))
			                                .filter(Objects::nonNull)
			                                .filter(e -> e.getModifierList() != null && 
			                                             e.getModifierList().hasModifierProperty(PsiModifier.FINAL) && 
			                                             e.getModifierList().hasModifierProperty(PsiModifier.STATIC))
			                                .filter(e -> e.getTypeElement() != null && VALID_CLASSES.contains(e.getTypeElement().getText()))
			                                .map(psiField -> new PsiPropertyKeyToMessageReference(key, psiField))
			                                .toArray(PsiReference[]::new);	
		}
		return PsiReference.EMPTY_ARRAY;
	}
	
}
