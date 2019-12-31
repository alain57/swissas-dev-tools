package com.swissas.action;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.swissas.dialog.DtoGeneratorForm;
import com.swissas.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The generate Dto From BO action menu that is visible on right click on a BO Editor
 * @author Tavan Alain
 */
public class GenerateDtoFromCurrentBo extends AnAction {

    private PsiClass psiClass = null;
    private PsiJavaFile javaFile = null;

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        boolean isVisible = false;
        Presentation presentation = e.getPresentation();
        if(psiFile instanceof PsiJavaFile){
            this.javaFile = (PsiJavaFile)psiFile;
            if(this.javaFile.getClasses().length > 0) {
                this.psiClass = this.javaFile.getClasses()[0];
                isVisible = InheritanceUtil.isInheritor(this.psiClass,
                                                        "amos.share.databaseAccess.bo.AbstractAmosBusinessObject");
                presentation.setText("Generate DTO for " 
                                     + StringUtils.getInstance().removeJavaEnding(this.javaFile.getName()));
            }
        }
        presentation.setVisible(isVisible);
        super.update(e);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if(this.psiClass != null) {
            DtoGeneratorForm dtoGenerator = DtoGeneratorForm.showDialog(e.getProject(), this.javaFile, getGetters());
            //TODO: show a view with the getters and the entityTag choice and generate the DTO based on the checkboxes
        }

    }

    private List<PsiMethod> getGetters() {
        if(this.psiClass == null) {
            return Collections.emptyList();
        }
        List<PsiMethod> psiMethods = new ArrayList<>(PsiTreeUtil.collectElementsOfType(this.psiClass, PsiMethod.class));
        return psiMethods.stream().filter(this::isGetter).sorted(Comparator.comparing(PsiMethod::getName)).collect(Collectors.toList());

    }

    private boolean isGetter(PsiMethod psiMethod) {
        boolean result = false;
        if(psiMethod != null && psiMethod.hasModifier(JvmModifier.PUBLIC) &&
                !psiMethod.hasModifier(JvmModifier.STATIC) &&
                !PsiType.VOID.equals(psiMethod.getReturnType()) &&
                psiMethod.getParameterList().isEmpty()) {
            String name = psiMethod.getName();
            result = name.matches("^(get|is|has).*$");
        }
        return result;
    }
}
