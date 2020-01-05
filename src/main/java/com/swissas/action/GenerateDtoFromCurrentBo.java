package com.swissas.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.swissas.dialog.DtoGeneratorForm;
import com.swissas.util.PsiHelper;
import com.swissas.util.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Generate Dto From BO action menu that is visible on right click on a BO Editor
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
            DtoGeneratorForm generatorForm = new DtoGeneratorForm(this.javaFile, PsiHelper.getInstance().getGettersForPsiClass(this.psiClass));
            if(generatorForm.showAndGet()){
                WriteCommandAction.runWriteCommandAction(this.javaFile.getProject(),
                                                                      generatorForm::saveFiles);
            }
            generatorForm.disposeIfNeeded();
        }
    }
}
