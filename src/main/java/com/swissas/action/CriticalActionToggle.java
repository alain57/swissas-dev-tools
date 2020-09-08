package com.swissas.action;


import java.util.ResourceBundle;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.swissas.toolwindow.WarningContentTreeView;
import icons.SwissAsIcons;
import org.jetbrains.annotations.NotNull;

/**
 * Toggle button that will be available on the SAS Warning panel. 
 * It purposes is to switch from critical to non-critical issues.
 * @author Tavan Alain
 */
public class CriticalActionToggle extends AnAction {

    private static final String TEXTS = "texts";
    private boolean isCriticalOnly = false;
    private WarningContentTreeView parent;
    
    public CriticalActionToggle(){
        getTemplatePresentation().setIcon(SwissAsIcons.CRITICAL);
        getTemplatePresentation().setText(ResourceBundle.getBundle(TEXTS).getString("sonar.critical.tooltip"));
    }
    
    public void setWarningContentTreeView(WarningContentTreeView parent){
        this.parent = parent;
    }
    
   
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.isCriticalOnly = !this.isCriticalOnly;
        this.parent.refreshTree();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setIcon(this.isCriticalOnly ? SwissAsIcons.WARNING : SwissAsIcons.CRITICAL);
        e.getPresentation().setText(this.isCriticalOnly ? ResourceBundle.getBundle(TEXTS).getString("sonar.warnings.tooltip") : ResourceBundle.getBundle(TEXTS).getString("sonar.critical.tooltip"));
        
    }
    
    public boolean isCriticalOnly(){
        return this.isCriticalOnly;
    }
}
