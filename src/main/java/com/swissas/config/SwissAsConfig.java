package com.swissas.config;

import java.util.Objects;
import java.util.ResourceBundle;

import javax.swing.*;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.swissas.toolwindow.WarningContent;
import com.swissas.util.StringUtils;
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
    private ConfigPanel configPanel;

    
    public SwissAsConfig(Project project){
        this.project = project;
        this.swissAsStorage = SwissAsStorage.getInstance();
    }
    
    @Override
    public void reset() {
        if(this.configPanel != null) {
            this.configPanel.getFourLetterCode().setText(this.swissAsStorage.getFourLetterCode());
            this.configPanel.getQualityLetterBox().setText(this.swissAsStorage.getQaLetterCode());
            this.configPanel.getDocumentationLetterBox().setText(this.swissAsStorage.getDocuLetterCode());
            this.configPanel.getSupportLetterBox().setText(this.swissAsStorage.getSupportLetterCode());
            this.configPanel.getOrientation().setSelectedIndex(this.swissAsStorage.isHorizontalOrientation() ? 0 : 1);
            this.configPanel.getMinTranslationSize().setText(this.swissAsStorage.getMinWarningSize());
            this.configPanel.getChxFixThis().setSelected(this.swissAsStorage.isFixMissingThis());
            this.configPanel.getChkFixAuthor().setSelected(this.swissAsStorage.isFixMissingAuthor());
            this.configPanel.getChkFixOverride().setSelected(this.swissAsStorage.isFixMissingOverride());
            this.configPanel.getChkFixUnused().setSelected(this.swissAsStorage.isFixUnusedSuppressWarning());
            this.configPanel.getChkTranslateOnlyModifiedLines().setSelected(this.swissAsStorage.isTranslationOnlyCheckChangedLine());
            this.configPanel.getPreCommitInformOtherPersonCheckbox().setSelected(this.swissAsStorage.isPreCommitInformOther());
            this.configPanel.getPreCommitCodeReviewCheckbox().setSelected(this.swissAsStorage.isPreCommitCodeReview());
            this.configPanel.enableOrDisableOtherPersonFields();
            this.configPanel.getConvertToTeamCheckbox().setSelected(this.swissAsStorage.isConvertToTeam());
        }
    }
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return ResourceBundle.getBundle("texts").getString("setting.title");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if(this.configPanel == null){
            this.configPanel = new ConfigPanel(this.project);
        }
        return this.configPanel.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return  !this.swissAsStorage.getFourLetterCode().equals(getFourLetterCode()) ||
                !this.swissAsStorage.getQaLetterCode().equals(getQALetterCode()) ||
                !this.swissAsStorage.getSupportLetterCode().equals(getSupportLetterCode()) ||
                !this.swissAsStorage.getDocuLetterCode().equals(getDocuLetterCode()) ||
                this.swissAsStorage.isHorizontalOrientation() == (this.configPanel.getOrientation().getSelectedIndex() == 1) ||
                this.swissAsStorage.isFixMissingThis() != this.configPanel.getChxFixThis().isSelected() ||
                this.swissAsStorage.isFixMissingAuthor() != this.configPanel.getChkFixAuthor().isSelected() ||
                !this.swissAsStorage.getMinWarningSize().equals(this.configPanel.getMinTranslationSize().getText()) ||
                this.swissAsStorage.isFixMissingOverride() != this.configPanel.getChkFixOverride().isSelected() ||
                this.swissAsStorage.isFixUnusedSuppressWarning() != this.configPanel.getChkFixUnused().isSelected() ||
                this.swissAsStorage.isTranslationOnlyCheckChangedLine() != this.configPanel.getChkTranslateOnlyModifiedLines().isSelected() ||
                this.swissAsStorage.isPreCommitCodeReview() != this.configPanel.getPreCommitCodeReviewCheckbox().isSelected() ||
                this.swissAsStorage.isPreCommitInformOther() != this.configPanel.getPreCommitInformOtherPersonCheckbox().isSelected() ||
                this.swissAsStorage.isConvertToTeam() != this.configPanel.getConvertToTeamCheckbox().isSelected()
                ;/* ||
                this.storage.isShowIgnoredValues() != this.chkShowIgnoreLists.isSelected();*/
    }

    @Override
    public void apply() throws ConfigurationException{
        validateSettings();
        this.swissAsStorage.setFourLetterCode(getFourLetterCode());
        this.swissAsStorage.setQaLetterCode(getQALetterCode());
        this.swissAsStorage.setDocuLetterCode(getDocuLetterCode());
        this.swissAsStorage.setSupportLetterCode(getSupportLetterCode());
        this.swissAsStorage.setHorizontalOrientation(this.configPanel.getOrientation().getSelectedIndex() == 0);
        this.swissAsStorage.setFixMissingThis(this.configPanel.getChxFixThis().isSelected());
        this.swissAsStorage.setFixMissingOverride(this.configPanel.getChkFixOverride().isSelected());
        this.swissAsStorage.setFixUnusedSuppressWarning(this.configPanel.getChkFixUnused().isSelected());
        this.swissAsStorage.setFixMissingAuthor(this.configPanel.getChkFixAuthor().isSelected());
        this.swissAsStorage.setMinWarningSize(this.configPanel.getMinTranslationSize().getText());
        this.swissAsStorage.setTranslationOnlyCheckChangedLine(this.configPanel.getChkTranslateOnlyModifiedLines().isSelected());
        this.swissAsStorage.setPreCommitCodeReview(this.configPanel.getPreCommitCodeReviewCheckbox().isSelected());
        this.swissAsStorage.setPreCommitInformOther(this.configPanel.getPreCommitInformOtherPersonCheckbox().isSelected());
        this.swissAsStorage.setConvertToTeam(this.configPanel.getConvertToTeamCheckbox().isSelected());
        refreshWarningContent();
    }
    
    private void validateSettings() throws ConfigurationException {
        if(getFourLetterCode().isEmpty()){
            throw new ConfigurationException("Please fill the 4LC field");
        } else if(!StringUtils.getInstance().isLetterCode(getFourLetterCode())){
            throw new ConfigurationException("Invalid letter code, please choose one from the list");
        }
        
        if(this.configPanel.getPreCommitInformOtherPersonCheckbox().isSelected()) {
            if(getDocuLetterCode().isEmpty() && getQALetterCode().isEmpty() && getSupportLetterCode().isEmpty()) {
                throw new ConfigurationException(
                        "You need to fill at least one field between QA/Docu/Support");
            }
            if(!StringUtils.getInstance().isLetterCodeWithName(getDocuLetterCode())){
                throw new ConfigurationException(
                        "Please choose a proposed value for the Documentation field");
            }
            if(!StringUtils.getInstance().isLetterCodeWithName(getQALetterCode())){
                throw new ConfigurationException(
                        "Please choose a proposed value for the Quality field");
            }
            if(!StringUtils.getInstance().isLetterCodeWithName(getSupportLetterCode())){
                throw new ConfigurationException(
                        "Please choose a proposed value for the Support field");
            }
        }
        
        if(!StringUtils.getInstance().isPositiveNumber(this.configPanel.getMinTranslationSize().getText())){
            throw new ConfigurationException("the minimum translation size needs to be a positive integer");
        }
    }
    
    private String getFourLetterCode(){
        return this.configPanel.getFourLetterCode().getText().trim();
    }
    
    private String getQALetterCode() {
        return this.configPanel.getQualityLetterBox().getText().trim();
    }
    
    private String getDocuLetterCode() {
        return this.configPanel.getDocumentationLetterBox().getText().trim();
    }
    
    private String getSupportLetterCode() {
        return this.configPanel.getSupportLetterBox().getText().trim();
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
