package com.swissas.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.InheritanceUtil;
import org.jetbrains.annotations.NotNull;

public class GenerateDtoFromCurrentBo extends AnAction {
    private boolean isVisible = false;

    @Override
    public void update(@NotNull AnActionEvent e) {
        checkStatus(e);
        Presentation presentation = e.getPresentation();
        presentation.setVisible(this.isVisible);
        super.update(e);
    }

    private void checkStatus(AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if(psiFile instanceof PsiJavaFile){
            PsiJavaFile javaFile = (PsiJavaFile)psiFile;
            this.isVisible = InheritanceUtil.isInheritor(javaFile.getClasses()[0], "amos.share.databaseAccess.bo.AbstractAmosBusinessObject");
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }
}
