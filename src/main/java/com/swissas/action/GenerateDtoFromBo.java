package com.swissas.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiClass;
import com.swissas.dialog.DtoGeneratorForm;
import com.swissas.util.PsiHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Generic Bo to DTO action where the user will type a BO name
 * @author Tavan Alain
 */
public class GenerateDtoFromBo extends AnAction {
	
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		
		Map<String, PsiClass> boMapForProjectUp = Optional.ofNullable(e.getProject())
				                                          .map(PsiHelper.getInstance()::getBoMapForProjectUp)
														  .orElse(new HashMap<>());
		DtoGeneratorForm generatorForm = new DtoGeneratorForm(e.getProject(), boMapForProjectUp);
		generatorForm.show();
	}
}
