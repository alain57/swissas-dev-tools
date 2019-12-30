package com.swissas.action;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateDtoFromCurrentBo extends AnAction {

    private PsiClass psiClass = null;

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        boolean isVisible = false;
        Presentation presentation = e.getPresentation();
        if(psiFile instanceof PsiJavaFile){
            PsiJavaFile javaFile = (PsiJavaFile)psiFile;
            if(javaFile.getClasses().length > 0) {
                this.psiClass = javaFile.getClasses()[0];
                isVisible = InheritanceUtil.isInheritor(this.psiClass,
                                                        "amos.share.databaseAccess.bo.AbstractAmosBusinessObject");
                String name = javaFile.getName();
                name = name.substring(0, name.indexOf(".java"));
                presentation.setText("Generate DTO for " + name);
            }
        }
        presentation.setVisible(isVisible);
        super.update(e);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if(this.psiClass != null) {
            List<String> getters = getGetters();
            //TODO: show a view with the getters and the entityTag choice and generate the DTO based on the checkboxes
        }

    }

    private List<String> getGetters() {
        if(this.psiClass == null) {
            return Collections.emptyList();
        }
        List<PsiMethod> psiMethods = new ArrayList<>(PsiTreeUtil.collectElementsOfType(this.psiClass, PsiMethod.class));
        return psiMethods.stream().filter(this::isGetter).map(PsiMethod::getName).sorted().collect(Collectors.toList());

    }

    private boolean isGetter(PsiMethod psiMethod) {
        boolean result = false;
        if(psiMethod != null && psiMethod.hasModifier(JvmModifier.PUBLIC) &&
                !psiMethod.hasModifier(JvmModifier.STATIC) &&
                psiMethod.getParameterList().isEmpty()) {
            String name = psiMethod.getName();
            result = name.matches("^(get|is|has).*$");
        }
        return result;
    }
}
