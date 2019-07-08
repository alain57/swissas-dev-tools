package com.swissas.config;

import java.util.Objects;
import java.util.ResourceBundle;

import javax.swing.*;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.swissas.toolwindow.WarningContent;
import com.swissas.util.SwissAsStorage;
import com.swissas.widget.TrafficLightPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;


/**
 * The configuration class of the plugin
 *
 * @author Tavan Alain
 */

public class SwissAsConfig implements Configurable {
    private final SwissAsStorage swissAsStorage;
    private final Project project;
    private final ConfigPanel configPanel;

    
    public SwissAsConfig(Project project){
        this.project = project;
        this.swissAsStorage = SwissAsStorage.getInstance(project);
        this.configPanel = new ConfigPanel(this.swissAsStorage);
        updateUIState();
    }
    
    private void updateUIState(){
        this.configPanel.getFourLetterCode().setSelectedItem(this.swissAsStorage.getFourLetterCode());
        this.configPanel.getOrientation().setSelectedIndex(this.swissAsStorage.isHorizontalOrientation() ? 0 : 1);
        this.configPanel.getMinTranslationSize().setText(this.swissAsStorage.getMinWarningSize());
        this.configPanel.getChxFixThis().setSelected(this.swissAsStorage.isFixMissingThis());
        this.configPanel.getChkFixAuthor().setSelected(this.swissAsStorage.isFixMissingAuthor());
        this.configPanel.getChkFixOverride().setSelected(this.swissAsStorage.isFixMissingOverride());
        this.configPanel.getChkFixUnused().setSelected(this.swissAsStorage.isFixMissingThis());
    }
    
    
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return ResourceBundle.getBundle("texts").getString("setting.title");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return this.configPanel.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return  !this.swissAsStorage.getFourLetterCode().equals(getFourLetterCodeSelectedItem()) ||
                this.swissAsStorage.isHorizontalOrientation() == (this.configPanel.getOrientation().getSelectedIndex() == 1) ||
                this.swissAsStorage.isFixMissingThis() != this.configPanel.getChxFixThis().isSelected() ||
                this.swissAsStorage.isFixMissingAuthor() != this.configPanel.getChkFixAuthor().isSelected() ||
                !this.swissAsStorage.getMinWarningSize().equals(this.configPanel.getMinTranslationSize().getText()) ||
                this.swissAsStorage.isFixMissingOverride() != this.configPanel.getChkFixOverride().isSelected() ||
                this.swissAsStorage.isFixUnusedSuppressWarning() != this.configPanel.getChkFixUnused().isSelected();/* ||
                this.storage.isShowIgnoredValues() != this.chkShowIgnoreLists.isSelected();*/
    }

    @Override
    public void apply() {
        this.swissAsStorage.setFourLetterCode(getFourLetterCodeSelectedItem());
        this.swissAsStorage.setHorizontalOrientation(this.configPanel.getOrientation().getSelectedIndex() == 0);
        this.swissAsStorage.setFixMissingThis(this.configPanel.getChxFixThis().isSelected());
        this.swissAsStorage.setFixMissingOverride(this.configPanel.getChkFixOverride().isSelected());
        this.swissAsStorage.setFixUnusedSuppressWarning(this.configPanel.getChkFixUnused().isSelected());
        this.swissAsStorage.setFixMissingAuthor(this.configPanel.getChkFixAuthor().isSelected());
        this.swissAsStorage.setMinWarningSize(this.configPanel.getMinTranslationSize().getText());
        refreshWarningContent();
    }

    private String getFourLetterCodeSelectedItem(){
        return this.configPanel.getFourLetterCode().getSelectedItem() == null ? "" : this.configPanel.getFourLetterCode().getSelectedItem().toString().toUpperCase().trim();
    }

    private void refreshWarningContent() {
        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(this.project);
        TrafficLightPanel trafficLightPanel = (TrafficLightPanel)ideFrame.getStatusBar().getWidget(TrafficLightPanel.WIDGET_ID);
        if(trafficLightPanel != null) {
            trafficLightPanel.setOrientation();
            ideFrame.getStatusBar().updateWidget(TrafficLightPanel.WIDGET_ID);
        }
        WarningContent warningContent = (WarningContent) Objects.requireNonNull(ToolWindowManager.getInstance(this.project).getToolWindow(WarningContent.ID).getContentManager().getContent(0)).getComponent();
        if(warningContent != null) {
            warningContent.refresh();
        }
    }
}
