package com.swissas.action;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.swissas.toolwindow.WarningContent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

import static com.swissas.widget.SwissAsWidget.LOGGER;

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
    private ImageIcon warning;
    private ImageIcon critical;
    
    public criticalActionToggle(){
        super();
        try {
            this.warning = new ImageIcon(ImageIO.read(criticalActionToggle.class.getResource("/warning.png"/*NON-NLS*/)).getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            this.critical = new ImageIcon(ImageIO.read(criticalActionToggle.class.getResource("/critical.png"/*NON-NLS*/)).getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        }catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        getTemplatePresentation().setIcon(this.critical);
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
        e.getPresentation().setIcon(this.isCriticalOnly ? this.warning : this.critical);
        e.getPresentation().setText(this.isCriticalOnly ? RESOURCE_BUNDLE.getString("sonar.warnings.tooltip") : RESOURCE_BUNDLE.getString("sonar.critical.tooltip"));
        
    }
    
    public boolean isCriticalOnly(){
        return this.isCriticalOnly;
    }
}
