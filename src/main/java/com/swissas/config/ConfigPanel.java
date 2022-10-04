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
	
	private final Project project;
	
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel mainPanel;
    private ComboBox<String> orientation;
    private ComboBox<String> committerOption;
    private EditorTextField fourLetterCode;
    private JBCheckBox chkAnnotation;
    private JLabel label7;
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
    // JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public ConfigPanel(Project project) {
		this.project = project;
		initComponents();
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

    public ComboBox<String> getCommitterOption() { return this.committerOption; }

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
	
	private void createUIComponents() {
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
		this.fourLetterCode = new TextFieldWithCompletion(this.project, allUserProvider, "", true, true, true,  true);
		this.qualityLetterBox = new TextFieldWithCompletion(this.project, qualityUserProvider, "", true, true,  true, true);
		this.supportLetterBox = new TextFieldWithCompletion(this.project, supportUserProvider, "", true, true,  true, true);
		this.documentationLetterBox = new TextFieldWithCompletion(this.project, documentationUserProvider, "", true, true,  true, true);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        createUIComponents();

        ResourceBundle bundle = ResourceBundle.getBundle("texts");
        this.mainPanel = new JPanel();
        var panel1 = new JPanel();
        var label1 = new JLabel();
        var label2 = new JLabel();
        var label3 = new JLabel();
        this.orientation = new ComboBox();
        this.committerOption = new ComboBox();
        this.chkAnnotation = new JBCheckBox();
        this.label7 = new JLabel();
        this.similarValue = new JBTextField();
        var panel2 = new JPanel();
        this.chkFixAuthor = new JBCheckBox();
        this.chkFixThis = new JBCheckBox();
        this.chkFixOverride = new JBCheckBox();
        this.convertToTeamCheckbox = new JBCheckBox();
        this.chkFixUnused = new JBCheckBox();
        var vSpacer1 = new Spacer();
        var panel3 = new JPanel();
        this.chkTranslateOnlyModifiedLines = new JBCheckBox();
        var hSpacer1 = new Spacer();
        var panel4 = new JPanel();
        this.preCommitCodeReviewCheckbox = new JBCheckBox();
        var hSpacer2 = new Spacer();
        this.preCommitInformOtherPersonCheckbox = new JBCheckBox();
        var hSpacer3 = new Spacer();
        var hSpacer4 = new Spacer();
        var label4 = new JLabel();
        var label5 = new JLabel();
        var label6 = new JLabel();

        //======== mainPanel ========
        {
            this.mainPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));

            //======== panel1 ========
            {
                panel1.setBorder(new TitledBorder(bundle.getString("general")));
                panel1.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));

                //---- label1 ----
                label1.setText(bundle.getString("enter.your.4lc.here"));
                panel1.add(label1, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- label2 ----
                label2.setText(bundle.getString("committer.option"));
                panel1.add(label2, new GridConstraints(1, 0, 1, 1,
                        GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

                //---- label3 ----
                label3.setText(bundle.getString("choose.traffic.light.orientation"));
                panel1.add(label3, new GridConstraints(2, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- orientation ----
                this.orientation.setEditable(false);
                this.orientation.setInheritsPopupMenu(false);
                this.orientation.setModel(new DefaultComboBoxModel<>(new String[] {
                    "Horizontal",
                    "Vertical"
                }));
                panel1.add(this.orientation, new GridConstraints(2, 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- committer option ----
                this.committerOption.setEditable(false);
                this.committerOption.setInheritsPopupMenu(false);
                this.committerOption.setModel(new DefaultComboBoxModel<>(new String[] {
                        "User",
                        "Team"
                }));
                panel1.add(this.committerOption, new GridConstraints(1, 1, 1, 1,
                        GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

                //---- fourLetterCode ----
                this.fourLetterCode.setBackground(null);
                panel1.add(this.fourLetterCode, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- chkAnnotation ----
                this.chkAnnotation.setText(bundle.getString("ConfigPanel.chkAnnotation.text"));
                panel1.add(this.chkAnnotation, new GridConstraints(2, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- label7 ----
                this.label7.setText(bundle.getString("ConfigPanel.label7.text"));
                panel1.add(this.label7, new GridConstraints(3, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- similarValue ----
                this.similarValue.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                this.similarValue.setText("0.8");
                this.similarValue.setToolTipText("higher value = similar closer to identenical");
                panel1.add(this.similarValue, new GridConstraints(3, 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            }
            this.mainPanel.add(panel1, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //======== panel2 ========
            {
                panel2.setBorder(new TitledBorder(bundle.getString("jenkins.fixes")));
                panel2.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));

                //---- chkFixAuthor ----
                this.chkFixAuthor.setText(bundle.getString("add.missing.author"));
                panel2.add(this.chkFixAuthor, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- chkFixThis ----
                this.chkFixThis.setText(bundle.getString("add.missing.this"));
                panel2.add(this.chkFixThis, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- chkFixOverride ----
                this.chkFixOverride.setText(bundle.getString("add.missing.override"));
                panel2.add(this.chkFixOverride, new GridConstraints(2, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- convertToTeamCheckbox ----
                this.convertToTeamCheckbox.setText(bundle.getString("ConfigPanel.convertToTeamCheckbox.text"));
                this.convertToTeamCheckbox.setToolTipText("When modifying a class of your team, the author will be transfered to your team account");
                panel2.add(this.convertToTeamCheckbox, new GridConstraints(3, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- chkFixUnused ----
                this.chkFixUnused.setEnabled(false);
                this.chkFixUnused.setSelected(false);
                this.chkFixUnused.setText(bundle.getString("remove.unused.annotation"));
                panel2.add(this.chkFixUnused, new GridConstraints(4, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
                panel2.add(vSpacer1, new GridConstraints(5, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    null, null, null));
            }
            this.mainPanel.add(panel2, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //======== panel3 ========
            {
                panel3.setBorder(new TitledBorder(bundle.getString("translations")));
                panel3.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));

                //---- chkTranslateOnlyModifiedLines ----
                this.chkTranslateOnlyModifiedLines.setText(bundle.getString("only.line.change"));
                panel3.add(this.chkTranslateOnlyModifiedLines, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
                panel3.add(hSpacer1, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK,
                    null, null, null));

                //---- label3 ----
                label3.setText(bundle.getString("min.translation"));
                panel3.add(label3, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- minTranslationSize ----
                this.minTranslationSize.setBackground(null);
                panel3.add(this.minTranslationSize, new GridConstraints(1, 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
            }
            this.mainPanel.add(panel3, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //======== panel4 ========
            {
                panel4.setBorder(new TitledBorder(bundle.getString("precommit.setting")));
                panel4.setLayout(new GridLayoutManager(3, 6, new Insets(0, 0, 0, 0), -1, -1));

                //---- preCommitCodeReviewCheckbox ----
                this.preCommitCodeReviewCheckbox.setText(bundle.getString("precommit.review_needed"));
                panel4.add(this.preCommitCodeReviewCheckbox, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
                panel4.add(hSpacer2, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK,
                    null, null, null));

                //---- preCommitInformOtherPersonCheckbox ----
                this.preCommitInformOtherPersonCheckbox.setText(bundle.getString("precommit.inform_other_needed"));
                panel4.add(this.preCommitInformOtherPersonCheckbox, new GridConstraints(2, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
                panel4.add(this.qualityLetterBox, new GridConstraints(2, 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
                panel4.add(this.supportLetterBox, new GridConstraints(2, 3, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
                panel4.add(hSpacer3, new GridConstraints(2, 2, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK,
                    null, null, null));
                panel4.add(this.documentationLetterBox, new GridConstraints(2, 5, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
                panel4.add(hSpacer4, new GridConstraints(2, 4, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK,
                    null, null, null));

                //---- label4 ----
                label4.setHorizontalAlignment(SwingConstants.CENTER);
                label4.setText("Quality");
                panel4.add(label4, new GridConstraints(1, 1, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- label5 ----
                label5.setText("Support");
                panel4.add(label5, new GridConstraints(1, 3, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

                //---- label6 ----
                label6.setText("Documentation");
                panel4.add(label6, new GridConstraints(1, 5, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
            }
            this.mainPanel.add(panel4, new GridConstraints(3, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        }
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}
}
