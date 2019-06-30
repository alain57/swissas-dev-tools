package com.swissas.action;


import java.util.ResourceBundle;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.SwissAsIcons;
import com.swissas.toolwindow.WarningContent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Toggle button that will be available on the SAS Warning panel. 
 * It purpose is to switch from critical to non critical issues.
 * @author Tavan Alain
 */
public class criticalActionToggle extends AnAction {

    @NonNls
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");

    private boolean isCriticalOnly = false;
    private WarningContent parent;
    
    public criticalActionToggle(){
        super();
        getTemplatePresentation().setIcon(SwissAsIcons.CRITICAL);
        getTemplatePresentation().setText(RESOURCE_BUNDLE.getString("sonar.critical.tooltip"));
    }
    
    public void setWarningContent(WarningContent parent){
        this.parent = parent;
    }
    
   
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.isCriticalOnly = !this.isCriticalOnly;
        this.parent.refresh();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setIcon(this.isCriticalOnly ? SwissAsIcons.WARNING : SwissAsIcons.CRITICAL);
        e.getPresentation().setText(this.isCriticalOnly ? RESOURCE_BUNDLE.getString("sonar.warnings.tooltip") : RESOURCE_BUNDLE.getString("sonar.critical.tooltip"));
        
    }
    
    public boolean isCriticalOnly(){
        return this.isCriticalOnly;
    }
}
