package com.swissas.config;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.MemoryUsagePanel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBList;
import com.swissas.beans.LabelData;
import com.swissas.toolwindow.WarningContent;
import com.swissas.ui.MyListCellRenderer;
import com.swissas.util.SpringUtilities;
import com.swissas.util.Storage;
import com.swissas.widget.TrafficLightPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.intellij.openapi.actionSystem.Anchor.BEFORE;


/**
 * The configuration class of the plugin
 *
 * @author Tavan Alain
 */

public class SwissAsConfig implements Configurable {
    
    private final Storage storage;
    private JPanel root;
    private JTextField fourLetterCode;
    private JComboBox<String> orientation;
    
    private JCheckBox chkFixAuthor;
    private JCheckBox chxFixThis;
    private JCheckBox chkFixOverride;
    private JCheckBox chkFixUnused;
    
    private JCheckBox chkShowIgnoreLists;
    private JList<String> lstIgnoreValues;

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
    
    public SwissAsConfig(){
        this.storage = ServiceManager.getService(Storage.class);
        initComponent();
        updateUIState();
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
        this.fourLetterCode.setText(this.storage.getFourLetterCode());
        this.orientation.setSelectedIndex(this.storage.isHorizontalOrientation() ? 0 : 1);
        this.chxFixThis.setSelected(this.storage.isFixMissingThis());
        this.chkFixAuthor.setSelected(this.storage.isFixMissingAuthor());
        this.chkFixOverride.setSelected(this.storage.isFixMissingOverride());
        this.chkFixUnused.setSelected(this.storage.isFixMissingThis());
    }
    
    
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return RESOURCE_BUNDLE.getString("setting.title");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return this.root;
    }

    @Override
    public boolean isModified() {
        return !this.storage.getFourLetterCode().equals(this.fourLetterCode.getText().toUpperCase().trim()) ||
                this.storage.isHorizontalOrientation() == (this.orientation.getSelectedIndex() == 1) ||
                this.storage.isFixMissingThis() != this.chxFixThis.isSelected() ||
                this.storage.isFixMissingAuthor() != this.chkFixAuthor.isSelected() ||
                this.storage.isFixMissingOverride() != this.chkFixOverride.isSelected() ||
                this.storage.isFixUnusedSuppressWarning() != this.chkFixUnused.isSelected();/* ||
                this.storage.isShowIgnoredValues() != this.chkShowIgnoreLists.isSelected();*/
    }

    @Override
    public void apply() {
        this.storage.setFourLetterCode(this.fourLetterCode.getText().toUpperCase().trim());
        this.storage.setHorizontalOrientation(this.orientation.getSelectedIndex() == 0);
        this.storage.setFixMissingThis(this.chxFixThis.isSelected());
        this.storage.setFixMissingOverride(this.chkFixOverride.isSelected());
        this.storage.setFixUnusedSuppressWarning(this.chkFixUnused.isSelected());
        this.storage.setFixMissingAuthor(this.chkFixAuthor.isSelected());
        refreshWarningContent();

    }

    private void refreshWarningContent() {
        try {
            DataContext context = DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(2000);
            Project project = Objects.requireNonNull(context).getData(DataKeys.PROJECT);
            if (project != null){
                IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
                TrafficLightPanel trafficLightPanel = (TrafficLightPanel)ideFrame.getStatusBar().getWidget(TrafficLightPanel.WIDGET_ID);
                if(trafficLightPanel != null) {
                    trafficLightPanel.setOrientation();
                    ideFrame.getStatusBar().updateWidget(TrafficLightPanel.WIDGET_ID);
                }
                WarningContent warningContent = (WarningContent) ToolWindowManager.getInstance(project).getToolWindow(WarningContent.ID).getContentManager().getContent(0).getComponent();
                warningContent.refresh();

            }
        }catch (ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
