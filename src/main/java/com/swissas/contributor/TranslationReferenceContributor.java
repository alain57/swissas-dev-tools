package com.swissas.contributor;


import com.intellij.lang.properties.psi.impl.PropertyKeyImpl;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TranslationReferenceContributor extends PsiReferenceContributor {
	
	private static final String STD = "Standard.properties";
	private static final String CLASSNAME = "PropertyKeyImpl";
	
	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
		registrar.registerReferenceProvider(PlatformPatterns.psiElement().and(new FilterPattern(new ElementFilter() {
			@Override
			public boolean isAcceptable(Object element, @Nullable PsiElement context) {
				if (element instanceof PropertyKeyImpl) {
					PropertyKeyImpl propertyKey = (PropertyKeyImpl) element;
					PsiFile propertyFile = propertyKey.getContainingFile();
					return STD.equals(propertyFile.getName());
				}
				return false;
			}
			
			@Override
			public boolean isClassAcceptable(Class hintClass) {
				return CLASSNAME.equals(hintClass.getSimpleName());
			}
		})), new PropertyKeyToMessageProvider());
	}
}
