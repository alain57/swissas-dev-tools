package com.swissas.config;

import java.awt.*;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.TextFieldWithAutoCompletion.StringsCompletionProvider;
import com.intellij.ui.components.*;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.swissas.beans.User;
import com.swissas.util.NetworkUtil;
import com.swissas.util.SwissAsStorage;

/**
 * Configuration panel java part
 *
 * @author Tavan Alain
 */

class ConfigPanel {
	
	private static final int FIXED       = GridConstraints.SIZEPOLICY_FIXED;
	private static final int SHRINK_GROW = GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW;
	private static final int GROW_WANT   = GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW;
	
    private JPanel mainPanel;
    private ComboBox<String> orientation;
    private EditorTextField fourLetterCode;
    private JBCheckBox chkAnnotation;
    private JBTextField similarValue;
    private JBCheckBox chkFixAuthor;
    private JBCheckBox chkFixThis;
    private JBCheckBox chkFixOverride;
    private JBCheckBox convertToTeamCheckbox;
    private JBCheckBox chkFixUnused;
    private JBCheckBox chkTranslateOnlyModifiedLines;
    private JBTextField minTranslationSize;
    private JBCheckBox preCommitCodeReviewCheckbox;
    private JBCheckBox preCommitInformOtherPersonCheckbox;
    private EditorTextField qualityLetterBox;
    private EditorTextField supportLetterBox;
    private EditorTextField documentationLetterBox;
	
	public ConfigPanel(Project project) {
		initComponents(project);
		this.preCommitInformOtherPersonCheckbox.addActionListener(e -> enableOrDisableOtherPersonFields());
	}

	public void enableOrDisableOtherPersonFields() {
		if(this.qualityLetterBox != null && this.preCommitInformOtherPersonCheckbox != null) {
			this.qualityLetterBox.setEnabled(this.preCommitInformOtherPersonCheckbox.isSelected());
			this.supportLetterBox.setEnabled(this.preCommitInformOtherPersonCheckbox.isSelected());
			this.documentationLetterBox.setEnabled(this.preCommitInformOtherPersonCheckbox.isSelected());
		}
	}
	
	public JPanel getMainPanel() {
		return this.mainPanel;
	}

	public JTextField getMinTranslationSize() {
		return this.minTranslationSize;
	}

	public TextFieldWithCompletion getFourLetterCode() {
		return (TextFieldWithCompletion)this.fourLetterCode;
	}
	
	public TextFieldWithCompletion getQualityLetterBox() {
		return (TextFieldWithCompletion)this.qualityLetterBox;
	}
	
	public TextFieldWithCompletion getSupportLetterBox() {
		return (TextFieldWithCompletion)this.supportLetterBox;
	}
	
	public TextFieldWithCompletion getDocumentationLetterBox() {
		return (TextFieldWithCompletion)this.documentationLetterBox;
	}
	
	public ComboBox<String> getOrientation() {
		return this.orientation;
	}

	public JCheckBox getChkFixAuthor() {
		return this.chkFixAuthor;
	}

	public JCheckBox getChkFixThis() {
		return this.chkFixThis;
	}

	public JCheckBox getChkFixOverride() {
		return this.chkFixOverride;
	}

	public JCheckBox getChkFixUnused() {
		return this.chkFixUnused;
	}
	
	public JCheckBox getChkAnnotation() { return this.chkAnnotation; }

	public JBTextField getSimilarValue() {
	    return this.similarValue;
    }
	
	public JCheckBox getChkTranslateOnlyModifiedLines() {
		return this.chkTranslateOnlyModifiedLines;
	}
	
	public JCheckBox getPreCommitCodeReviewCheckbox() {
		return this.preCommitCodeReviewCheckbox;
	}
	
	public JCheckBox getPreCommitInformOtherPersonCheckbox() {
		return this.preCommitInformOtherPersonCheckbox;
	}
	
	public JCheckBox getConvertToTeamCheckbox() {
		return this.convertToTeamCheckbox;
	}
	
	private void createUIComponents(Project project) {
		if(SwissAsStorage.getInstance().getUserMap().isEmpty()){
			NetworkUtil.getInstance().refreshUserMap();
		}
		this.minTranslationSize = new JBTextField("5");
		Set<String> qaUsersLcAndNames = new TreeSet<>();
		Set<String> supportUsersLcAndNames = new TreeSet<>();
		Set<String> documentationUsersLcAndNames = new TreeSet<>();
		Set<String> allUsers = new TreeSet<>();
		
		for (User user : SwissAsStorage.getInstance().getUserMap().values()) {
			allUsers.add(user.getLc());
			String lcAndName = user.getLCAndName();
			if(user.isInTeam("QC")){
				qaUsersLcAndNames.add(lcAndName);
			}else if(user.isInTeam("SUP")){
				supportUsersLcAndNames.add(lcAndName);
			}else if(user.isInTeam("DE")){
				documentationUsersLcAndNames.add(lcAndName);
			}
		}
		StringsCompletionProvider allUserProvider = new StringsCompletionProvider(allUsers, null);
		StringsCompletionProvider qualityUserProvider = new StringsCompletionProvider(qaUsersLcAndNames, null);
		StringsCompletionProvider supportUserProvider = new StringsCompletionProvider(supportUsersLcAndNames, null);
		StringsCompletionProvider documentationUserProvider = new StringsCompletionProvider(documentationUsersLcAndNames, null);
		this.fourLetterCode = new TextFieldWithCompletion(project, allUserProvider, "", true, true, true,  true);
		this.qualityLetterBox = new TextFieldWithCompletion(project, qualityUserProvider, "", true, true,  true, true);
		this.supportLetterBox = new TextFieldWithCompletion(project, supportUserProvider, "", true, true,  true, true);
		this.documentationLetterBox = new TextFieldWithCompletion(project, documentationUserProvider, "", true, true,  true, true);
	}

	private void initComponents(Project project) {
		createUIComponents(project);
		
		ResourceBundle bundle = ResourceBundle.getBundle("texts");
		this.mainPanel = new JPanel(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
		this.mainPanel.add(createGeneralPanel(bundle), constraints(0, 0, GridConstraints.ANCHOR_NORTH,
		                                                           GridConstraints.FILL_HORIZONTAL,
		                                                           SHRINK_GROW, FIXED));
		this.mainPanel.add(createJenkinsFixesPanel(bundle), constraints(2, 0, GridConstraints.ANCHOR_CENTER,
		                                                                GridConstraints.FILL_BOTH,
		                                                                SHRINK_GROW, SHRINK_GROW));
		this.mainPanel.add(createTranslationsPanel(bundle), constraints(1, 0, GridConstraints.ANCHOR_CENTER,
		                                                                GridConstraints.FILL_BOTH,
		                                                                SHRINK_GROW, SHRINK_GROW));
		this.mainPanel.add(createPreCommitPanel(bundle), constraints(3, 0, GridConstraints.ANCHOR_CENTER,
		                                                             GridConstraints.FILL_BOTH,
		                                                             SHRINK_GROW, SHRINK_GROW));
	}
	
	private JPanel createGeneralPanel(ResourceBundle bundle) {
		JPanel panel = createTitledPanel(bundle.getString("general"), 4, 2);
		
		panel.add(new JLabel(bundle.getString("enter.your.4lc.here")),
		          constraints(0, 0, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, FIXED, FIXED));
		panel.add(new JLabel(bundle.getString("choose.traffic.light.orientation")),
		          constraints(1, 0, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, FIXED, FIXED));
		
		this.orientation = new ComboBox<>();
		this.orientation.setEditable(false);
		this.orientation.setInheritsPopupMenu(false);
		this.orientation.setModel(new DefaultComboBoxModel<>(new String[] {"Horizontal", "Vertical"}));
		panel.add(this.orientation, constraints(1, 1, GridConstraints.ANCHOR_WEST,
		                                        GridConstraints.FILL_HORIZONTAL,
		                                        GridConstraints.SIZEPOLICY_CAN_GROW, FIXED));
		
		this.fourLetterCode.setBackground(null);
		panel.add(this.fourLetterCode, constraints(0, 1, GridConstraints.ANCHOR_WEST,
		                                           GridConstraints.FILL_HORIZONTAL, GROW_WANT, FIXED));
		
		this.chkAnnotation = new JBCheckBox();
		this.chkAnnotation.setText(bundle.getString("ConfigPanel.chkAnnotation.text"));
		panel.add(this.chkAnnotation, constraints(2, 0, GridConstraints.ANCHOR_CENTER,
		                                          GridConstraints.FILL_NONE, SHRINK_GROW, SHRINK_GROW));
		
		JLabel similarLabel = new JLabel(bundle.getString("ConfigPanel.label7.text"));
		panel.add(similarLabel, constraints(3, 0, GridConstraints.ANCHOR_CENTER,
		                                    GridConstraints.FILL_NONE, SHRINK_GROW, SHRINK_GROW));
		
		this.similarValue = new JBTextField();
		this.similarValue.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		this.similarValue.setText("0.8");
		this.similarValue.setToolTipText("higher value = similar closer to identical");
		panel.add(this.similarValue, constraints(3, 1, GridConstraints.ANCHOR_WEST,
		                                         GridConstraints.FILL_HORIZONTAL, SHRINK_GROW, SHRINK_GROW));
		return panel;
	}
	
	private JPanel createJenkinsFixesPanel(ResourceBundle bundle) {
		JPanel panel = createTitledPanel(bundle.getString("jenkins.fixes"), 6, 1);
		
		this.chkFixAuthor = new JBCheckBox();
		this.chkFixAuthor.setText(bundle.getString("add.missing.author"));
		panel.add(this.chkFixAuthor, constraints(0, 0, GridConstraints.ANCHOR_WEST,
		                                         GridConstraints.FILL_NONE, SHRINK_GROW, FIXED));
		
		this.chkFixThis = new JBCheckBox();
		this.chkFixThis.setText(bundle.getString("add.missing.this"));
		panel.add(this.chkFixThis, constraints(1, 0, GridConstraints.ANCHOR_WEST,
		                                       GridConstraints.FILL_NONE, SHRINK_GROW, FIXED));
		
		this.chkFixOverride = new JBCheckBox();
		this.chkFixOverride.setText(bundle.getString("add.missing.override"));
		panel.add(this.chkFixOverride, constraints(2, 0, GridConstraints.ANCHOR_WEST,
		                                           GridConstraints.FILL_NONE, SHRINK_GROW, FIXED));
		
		this.convertToTeamCheckbox = new JBCheckBox();
		this.convertToTeamCheckbox.setText(bundle.getString("ConfigPanel.convertToTeamCheckbox.text"));
		this.convertToTeamCheckbox.setToolTipText("When modifying a class of your team, the author will be transferred to your team account");
		panel.add(this.convertToTeamCheckbox, constraints(3, 0, GridConstraints.ANCHOR_WEST,
		                                                  GridConstraints.FILL_NONE, SHRINK_GROW, SHRINK_GROW));
		
		this.chkFixUnused = new JBCheckBox();
		this.chkFixUnused.setEnabled(false);
		this.chkFixUnused.setSelected(false);
		this.chkFixUnused.setText(bundle.getString("remove.unused.annotation"));
		panel.add(this.chkFixUnused, constraints(4, 0, GridConstraints.ANCHOR_WEST,
		                                         GridConstraints.FILL_NONE, SHRINK_GROW, FIXED));
		
		panel.add(new Spacer(), constraints(5, 0, GridConstraints.ANCHOR_CENTER,
		                                    GridConstraints.FILL_VERTICAL,
		                                    GridConstraints.SIZEPOLICY_CAN_SHRINK, GROW_WANT));
		return panel;
	}
	
	private JPanel createTranslationsPanel(ResourceBundle bundle) {
		JPanel panel = createTitledPanel(bundle.getString("translations"), 2, 2);
		
		this.chkTranslateOnlyModifiedLines = new JBCheckBox();
		this.chkTranslateOnlyModifiedLines.setText(bundle.getString("only.line.change"));
		panel.add(this.chkTranslateOnlyModifiedLines, constraints(0, 0, GridConstraints.ANCHOR_WEST,
		                                                          GridConstraints.FILL_NONE, SHRINK_GROW, FIXED));
		panel.add(new Spacer(), horizontalSpacerConstraints(0, 1));
		
		panel.add(new JLabel(bundle.getString("min.translation")),
		          constraints(1, 0, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, FIXED, FIXED));
		
		this.minTranslationSize.setBackground(null);
		panel.add(this.minTranslationSize, constraints(1, 1, GridConstraints.ANCHOR_WEST,
		                                               GridConstraints.FILL_HORIZONTAL, GROW_WANT, FIXED));
		return panel;
	}
	
	private JPanel createPreCommitPanel(ResourceBundle bundle) {
		JPanel panel = createTitledPanel(bundle.getString("precommit.setting"), 3, 6);
		
		this.preCommitCodeReviewCheckbox = new JBCheckBox();
		this.preCommitCodeReviewCheckbox.setText(bundle.getString("precommit.review_needed"));
		panel.add(this.preCommitCodeReviewCheckbox, constraints(0, 0, GridConstraints.ANCHOR_WEST,
		                                                        GridConstraints.FILL_NONE, SHRINK_GROW, FIXED));
		panel.add(new Spacer(), horizontalSpacerConstraints(0, 1));
		
		this.preCommitInformOtherPersonCheckbox = new JBCheckBox();
		this.preCommitInformOtherPersonCheckbox.setText(bundle.getString("precommit.inform_other_needed"));
		panel.add(this.preCommitInformOtherPersonCheckbox, constraints(2, 0, GridConstraints.ANCHOR_WEST,
		                                                               GridConstraints.FILL_NONE, SHRINK_GROW, FIXED));
		panel.add(this.qualityLetterBox, letterBoxConstraints(1));
		panel.add(this.supportLetterBox, letterBoxConstraints(3));
		panel.add(new Spacer(), horizontalSpacerConstraints(2, 2));
		panel.add(this.documentationLetterBox, letterBoxConstraints(5));
		panel.add(new Spacer(), horizontalSpacerConstraints(2, 4));
		
		JLabel qualityLabel = new JLabel("Quality");
		qualityLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(qualityLabel, labelConstraints(1));
		panel.add(new JLabel("Support"), labelConstraints(3));
		panel.add(new JLabel("Documentation"), labelConstraints(5));
		return panel;
	}
	
	private static JPanel createTitledPanel(String title, int rows, int columns) {
		JPanel panel = new JPanel(new GridLayoutManager(rows, columns, new Insets(0, 0, 0, 0), -1, -1));
		panel.setBorder(new TitledBorder(title));
		return panel;
	}
	
	private static GridConstraints horizontalSpacerConstraints(int row, int column) {
		return constraints(row, column, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
		                   GROW_WANT, GridConstraints.SIZEPOLICY_CAN_SHRINK);
	}
	
	private static GridConstraints letterBoxConstraints(int column) {
		return constraints(2, column, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
		                   GROW_WANT, FIXED);
	}
	
	private static GridConstraints labelConstraints(int column) {
		return constraints(1, column, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, FIXED, FIXED);
	}
	
	private static GridConstraints constraints(int row, int column, int anchor, int fill,
	                                           int horizontalPolicy, int verticalPolicy) {
		return new GridConstraints(row, column, 1, 1, anchor, fill, horizontalPolicy, verticalPolicy,
		                           null, null, null);
	}
}
