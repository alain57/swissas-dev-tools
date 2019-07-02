package com.swissas.config;

import java.util.Objects;
import java.util.ResourceBundle;

import javax.swing.*;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBList;
import com.swissas.beans.LabelData;
import com.swissas.toolwindow.WarningContent;
import com.swissas.ui.MyListCellRenderer;
import com.swissas.util.AutoCompletion;
import com.swissas.util.PositiveNumberVerifier;
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

    private ComboBox<String> fourLetterCode;
    private JComboBox orientation;
	private JCheckBox chkFixAuthor;
    private JCheckBox chxFixThis;
    private JCheckBox chkFixOverride;
    private JCheckBox chkFixUnused;
    private JPanel generalPanel;
    private JPanel jenkinsPanel;


    private SwissAsStorage swissAsStorage;
    private JPanel root;
    private JTextField minTranslationSize;
    private JPanel warningPanel;


    private JCheckBox chkShowIgnoreLists;
    private JList<LabelData> lstIgnoreValues;
    private final Project project;

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
    
    public SwissAsConfig(Project project){
        this.project = project;
    }
    
    
    
    private JPanel getWarningPanel() {
        JPanel warningPanel = new JPanel();
        warningPanel.setBorder(IdeBorderFactory.createTitledBorder(RESOURCE_BUNDLE.getString("sas.warning.settings")));
        warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.PAGE_AXIS));
        this.chkShowIgnoreLists = new JCheckBox(RESOURCE_BUNDLE.getString("show.ignored.warnings.in.red.striked.format"));
        warningPanel.add(this.chkShowIgnoreLists);
        DefaultListModel<LabelData> model = new DefaultListModel ();
        model.addElement ( new LabelData( LabelData.WarningType.CRITICAL, 1, "long text to see" ) );
        model.addElement ( new LabelData ( LabelData.WarningType.WARNING, 0, "text2" ) );
        model.addElement ( new LabelData ( LabelData.WarningType.SONAR, 555, "text3" ) );

        this.lstIgnoreValues = new JBList<>(model);
        this.lstIgnoreValues.setCellRenderer(new MyListCellRenderer(this.lstIgnoreValues));
        warningPanel.add(new JLabel(RESOURCE_BUNDLE.getString("ignore.list")));
        warningPanel.add(this.lstIgnoreValues);
        return  warningPanel;
    }
    
    
    private void updateUIState(){
        if(this.swissAsStorage != null && this.fourLetterCode != null) {

            this.fourLetterCode.setSelectedItem(this.swissAsStorage.getFourLetterCode());
            this.orientation.setSelectedIndex(this.swissAsStorage.isHorizontalOrientation() ? 0 : 1);
            this.minTranslationSize.setText(this.swissAsStorage.getMinWarningSize());
            this.chxFixThis.setSelected(this.swissAsStorage.isFixMissingThis());
            this.chkFixAuthor.setSelected(this.swissAsStorage.isFixMissingAuthor());
            this.chkFixOverride.setSelected(this.swissAsStorage.isFixMissingOverride());
            this.chkFixUnused.setSelected(this.swissAsStorage.isFixMissingThis());
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
        return this.root;
    }

    @Override
    public boolean isModified() {
        return  this.swissAsStorage != null &&
                !this.swissAsStorage.getFourLetterCode().equals(getFourLetterCodeSelectedItem()) ||
                this.swissAsStorage.isHorizontalOrientation() == (this.orientation.getSelectedIndex() == 1) ||
                this.swissAsStorage.isFixMissingThis() != this.chxFixThis.isSelected() ||
                this.swissAsStorage.isFixMissingAuthor() != this.chkFixAuthor.isSelected() ||
                !this.swissAsStorage.getMinWarningSize().equals(this.minTranslationSize.getText()) ||
                this.swissAsStorage.isFixMissingOverride() != this.chkFixOverride.isSelected() ||
                this.swissAsStorage.isFixUnusedSuppressWarning() != this.chkFixUnused.isSelected();/* ||
                this.storage.isShowIgnoredValues() != this.chkShowIgnoreLists.isSelected();*/
    }

    @Override
    public void apply() {
        if(this.swissAsStorage != null) {
            this.swissAsStorage.setFourLetterCode(getFourLetterCodeSelectedItem());
            this.swissAsStorage.setHorizontalOrientation(this.orientation.getSelectedIndex() == 0);
            this.swissAsStorage.setFixMissingThis(this.chxFixThis.isSelected());
            this.swissAsStorage.setFixMissingOverride(this.chkFixOverride.isSelected());
            this.swissAsStorage.setFixUnusedSuppressWarning(this.chkFixUnused.isSelected());
            this.swissAsStorage.setFixMissingAuthor(this.chkFixAuthor.isSelected());
            this.swissAsStorage.setMinWarningSize(this.minTranslationSize.getText());
            refreshWarningContent();
        }
    }

    private String getFourLetterCodeSelectedItem(){
        return this.fourLetterCode.getSelectedItem() == null ? "" : this.fourLetterCode.getSelectedItem().toString().toUpperCase().trim();
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

    private void createUIComponents() {
        this.swissAsStorage = SwissAsStorage.getInstance(this.project);
        this.minTranslationSize = new JTextField("5");
        PositiveNumberVerifier verifier = new PositiveNumberVerifier();
        this.minTranslationSize.setInputVerifier(verifier);
        this.fourLetterCode = new ComboBox<>();
        this.swissAsStorage.getUserMap().keySet().forEach(this.fourLetterCode::addItem);
        AutoCompletion.enable(this.fourLetterCode);
        SwingUtilities.invokeLater(this::updateUIState);
    }
}
