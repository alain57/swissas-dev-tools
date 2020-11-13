package com.swissas.contributor;

import com.intellij.lang.properties.psi.impl.PropertyKeyImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The PsiReference class that link the standard property key and the _Message field together
 *
 * @author Tavan Alain
 */
class PsiPropertyKeyToMessageReference extends PsiReferenceBase<PropertyKeyImpl> {
	
	private final PsiField field;
	
	public PsiPropertyKeyToMessageReference(@NotNull PropertyKeyImpl element, PsiField field) {
		super(element);
		this.field = field;
	}
	
	@Nullable
	@Override
	public PsiElement resolve() {
		return this.field;
	}
}
