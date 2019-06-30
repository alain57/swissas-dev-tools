package com.swissas.config;

import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

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
import com.swissas.util.SpringUtilities;
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
    
    private SwissAsStorage swissAsStorage;
    private JPanel root;
    private JTextField fourLetterCode;
    private JComboBox<String> orientation;
    
    private JCheckBox chkFixAuthor;
    private JCheckBox chxFixThis;
    private JCheckBox chkFixOverride;
    private JCheckBox chkFixUnused;
    
    private JCheckBox chkShowIgnoreLists;
    private JList<String> lstIgnoreValues;
    private Project project;

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
    
    public SwissAsConfig(Project project){
        try {
            this.project = project;
            this.swissAsStorage = SwissAsStorage.getInstance(project);
            initComponent();
            updateUIState();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    private JPanel getGeneralPanel(){
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        JPanel traffic = new JPanel();
        traffic.setBorder(IdeBorderFactory.createTitledBorder(RESOURCE_BUNDLE.getString("general")));
        traffic.setLayout(new SpringLayout());

        JLabel fourLetterLabel = new JLabel(RESOURCE_BUNDLE.getString("enter.your.4lc.here"));
        this.fourLetterCode = new JTextField();
        this.fourLetterCode.setColumns(4);
        traffic.add(fourLetterLabel);
        traffic.add(this.fourLetterCode);


        JLabel orientationLabel = new JLabel(RESOURCE_BUNDLE.getString("choose.traffic.light.orientation"));
        traffic.add(orientationLabel);
        this.orientation = new ComboBox<>();
        this.orientation.addItem(RESOURCE_BUNDLE.getString("horizontal"));
        this.orientation.addItem(RESOURCE_BUNDLE.getString("vertical"));
        traffic.add(this.orientation);
        contentPanel.add(traffic);
        SpringUtilities.makeCompactGrid(traffic, //parent
                2, 2,
                3, 3,  //initX, initY
                3, 3); //xPad, yPad
        return contentPanel;
    }
    
    
    
    private JPanel getJenkinsPanel(){
        JPanel jenkinsPanel = new JPanel();
        jenkinsPanel.setBorder(IdeBorderFactory.createTitledBorder(RESOURCE_BUNDLE.getString("jenkins.fixes")));
        jenkinsPanel.setLayout(new BoxLayout(jenkinsPanel, BoxLayout.PAGE_AXIS));
        this.chkFixAuthor = new JCheckBox(RESOURCE_BUNDLE.getString("add.missing.author"));
        jenkinsPanel.add(this.chkFixAuthor);
        this.chxFixThis = new JCheckBox(RESOURCE_BUNDLE.getString("add.missing.this"));
        jenkinsPanel.add(this.chxFixThis);
        this.chkFixOverride = new JCheckBox(RESOURCE_BUNDLE.getString("add.missing.override"));
        jenkinsPanel.add(this.chkFixOverride);
        this.chkFixUnused = new JCheckBox(RESOURCE_BUNDLE.getString("remove.unused.annotation"));
        jenkinsPanel.add(this.chkFixUnused);
        jenkinsPanel.add(Box.createHorizontalGlue());
        jenkinsPanel.setMinimumSize(new Dimension(Short.MAX_VALUE, 0));
        return jenkinsPanel;
    }
    
    private JPanel getWarningPanel() {
        JPanel warningPanel = new JPanel();
        warningPanel.setBorder(IdeBorderFactory.createTitledBorder(RESOURCE_BUNDLE.getString("sas.warning.settings")));
        warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.PAGE_AXIS));
        this.chkShowIgnoreLists = new JCheckBox(RESOURCE_BUNDLE.getString("show.ignored.warnings.in.red.striked.format"));
        warningPanel.add(this.chkShowIgnoreLists);
        DefaultListModel model = new DefaultListModel ();
        model.addElement ( new LabelData( LabelData.WarningType.CRITICAL, 1, "long text to see" ) );
        model.addElement ( new LabelData ( LabelData.WarningType.WARNING, 0, "text2" ) );
        model.addElement ( new LabelData ( LabelData.WarningType.SONAR, 555, "text3" ) );

        this.lstIgnoreValues = new JBList<>(model);
        this.lstIgnoreValues.setCellRenderer(new MyListCellRenderer(this.lstIgnoreValues));
        warningPanel.add(new JLabel(RESOURCE_BUNDLE.getString("ignore.list")));
        warningPanel.add(this.lstIgnoreValues);
        return  warningPanel;
    }

    private void initComponent() {
        this.root = new JPanel();
        this.root.setLayout(new BoxLayout(this.root, BoxLayout.PAGE_AXIS));
        this.root.add(getGeneralPanel());
        this.root.add(getJenkinsPanel());
        //this.root.add(getWarningPanel());
    }
    
    private void updateUIState(){
        this.fourLetterCode.setText(this.swissAsStorage.getFourLetterCode());
        this.orientation.setSelectedIndex(this.swissAsStorage.isHorizontalOrientation() ? 0 : 1);
        this.chxFixThis.setSelected(this.swissAsStorage.isFixMissingThis());
        this.chkFixAuthor.setSelected(this.swissAsStorage.isFixMissingAuthor());
        this.chkFixOverride.setSelected(this.swissAsStorage.isFixMissingOverride());
        this.chkFixUnused.setSelected(this.swissAsStorage.isFixMissingThis());
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
        return !this.swissAsStorage.getFourLetterCode().equals(this.fourLetterCode.getText().toUpperCase().trim()) ||
                this.swissAsStorage.isHorizontalOrientation() == (this.orientation.getSelectedIndex() == 1) ||
                this.swissAsStorage.isFixMissingThis() != this.chxFixThis.isSelected() ||
                this.swissAsStorage.isFixMissingAuthor() != this.chkFixAuthor.isSelected() ||
                this.swissAsStorage.isFixMissingOverride() != this.chkFixOverride.isSelected() ||
                this.swissAsStorage.isFixUnusedSuppressWarning() != this.chkFixUnused.isSelected();/* ||
                this.storage.isShowIgnoredValues() != this.chkShowIgnoreLists.isSelected();*/
    }

    @Override
    public void apply() {
        this.swissAsStorage.setFourLetterCode(this.fourLetterCode.getText().toUpperCase().trim());
        this.swissAsStorage.setHorizontalOrientation(this.orientation.getSelectedIndex() == 0);
        this.swissAsStorage.setFixMissingThis(this.chxFixThis.isSelected());
        this.swissAsStorage.setFixMissingOverride(this.chkFixOverride.isSelected());
        this.swissAsStorage.setFixUnusedSuppressWarning(this.chkFixUnused.isSelected());
        this.swissAsStorage.setFixMissingAuthor(this.chkFixAuthor.isSelected());
        refreshWarningContent();

    }

    private void refreshWarningContent() {
        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(this.project);
        TrafficLightPanel trafficLightPanel = (TrafficLightPanel)ideFrame.getStatusBar().getWidget(TrafficLightPanel.WIDGET_ID);
        if(trafficLightPanel != null) {
            trafficLightPanel.setOrientation();
            ideFrame.getStatusBar().updateWidget(TrafficLightPanel.WIDGET_ID);
        }
        WarningContent warningContent = (WarningContent) ToolWindowManager.getInstance(this.project).getToolWindow(WarningContent.ID).getContentManager().getContent(0).getComponent();
        warningContent.refresh();
    }
}
