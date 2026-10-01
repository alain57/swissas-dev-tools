package com.swissas.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.swissas.dialog.DtoGeneratorForm;
import com.swissas.util.PsiHelper;
import com.swissas.util.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Generate Dto From BO action menu that is visible on right-click on a BO Editor
 * @author Tavan Alain
 */
public class GenerateDtoFromCurrentBo extends AnAction {

    private static final String BO_PARENT_CLASS = "amos.server.databaseAccess.bo.AbstractAmosBusinessObject";

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * The BO is read from the event each time: an action is shared by all the editors, it must not keep a PSI element.
     */
    private static PsiClass findBoClass(@NotNull AnActionEvent e) {
        if (e.getData(CommonDataKeys.PSI_FILE) instanceof PsiJavaFile javaFile && javaFile.getClasses().length > 0) {
            PsiClass psiClass = javaFile.getClasses()[0];
            return InheritanceUtil.isInheritor(psiClass, BO_PARENT_CLASS) ? psiClass : null;
        }
        return null;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiClass boClass = findBoClass(e);
        Presentation presentation = e.getPresentation();
        presentation.setVisible(boClass != null);
        if (boClass != null) {
            presentation.setText("Generate DTO for "
                                 + StringUtils.getInstance().removeJavaEnding(boClass.getContainingFile().getName()));
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiClass boClass = findBoClass(e);
        if(boClass != null && boClass.getContainingFile() instanceof PsiJavaFile javaFile) {
            DtoGeneratorForm generatorForm = new DtoGeneratorForm(javaFile, PsiHelper.getInstance().getGettersForPsiClass(boClass));
            generatorForm.show();
        }
    }
}
