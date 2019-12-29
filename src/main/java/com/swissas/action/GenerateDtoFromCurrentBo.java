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

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        boolean isVisible = false;
        Presentation presentation = e.getPresentation();
        if(psiFile instanceof PsiJavaFile){
            PsiJavaFile javaFile = (PsiJavaFile)psiFile;
            if(javaFile.getClasses().length > 0) {
                isVisible = InheritanceUtil.isInheritor(javaFile.getClasses()[0],
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

    }
}
