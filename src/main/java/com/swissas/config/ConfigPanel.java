package com.swissas.config;

import java.util.Set;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.TextFieldWithAutoCompletion.StringsCompletionProvider;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.swissas.beans.LabelData;
import com.swissas.beans.User;
import com.swissas.util.NetworkUtil;
import com.swissas.util.PositiveNumberVerifier;
import com.swissas.util.SwissAsStorage;

/**
 * Configuration panel java part
 *
 * @author
 */

class ConfigPanel {
	private TextFieldWithCompletion fourLetterCode;
	private ComboBox<String> orientation;
	private JCheckBox chkFixAuthor;
	private JCheckBox chxFixThis;
	private JCheckBox chkFixOverride;
	private JCheckBox chkFixUnused;
	private JPanel mainPanel;
	private JPanel generalPanel;
	private JPanel jenkinsPanel;
	private JTextField minTranslationSize;
	private JPanel translationPanel;
	private JCheckBox chkTranslateOnlyModifiedLines;
	private JPanel preCommitPanel;
	private JCheckBox preCommitCodeReviewCheckbox;
	private JCheckBox preCommitInformOtherPersonCheckbox;
	private TextFieldWithCompletion qualityLetterBox;
	private TextFieldWithCompletion supportLetterBox;
	private TextFieldWithCompletion documentationLetterBox;
	
	private JCheckBox chkShowIgnoreLists;
	private JList<LabelData> lstIgnoreValues;
	
	private final Project project;
	
	
	public ConfigPanel(Project project) {
		this.project = project;
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
		return this.fourLetterCode;
	}
	
	public TextFieldWithCompletion getQualityLetterBox() {
		return this.qualityLetterBox;
	}
	
	public TextFieldWithCompletion getSupportLetterBox() {
		return this.supportLetterBox;
	}
	
	public TextFieldWithCompletion getDocumentationLetterBox() {
		return this.documentationLetterBox;
	}
	
	public ComboBox<String> getOrientation() {
		return this.orientation;
	}

	public JCheckBox getChkFixAuthor() {
		return this.chkFixAuthor;
	}

	public JCheckBox getChxFixThis() {
		return this.chxFixThis;
	}

	public JCheckBox getChkFixOverride() {
		return this.chkFixOverride;
	}

	public JCheckBox getChkFixUnused() {
		return this.chkFixUnused;
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
	
	private void createUIComponents() {
		if(SwissAsStorage.getInstance().getUserMap().isEmpty()){
			NetworkUtil.getInstance().refreshUserMap();
		}
		this.minTranslationSize = new JTextField("5");
		Set<String> qaUsersLcAndNames = new TreeSet<>();
		Set<String> supportUsersLcAndNames = new TreeSet<>();
		Set<String> documentationUsersLcAndNames = new TreeSet<>();
		Set<String> allUsers = new TreeSet<>();
		
		for (User user : SwissAsStorage.getInstance().getUserMap().values()) {
			allUsers.add(user.getLc());
			String lcAndName = user.getLCAndName();
			if(user.isInTeam("QA")){
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
		this.fourLetterCode = new TextFieldWithCompletion(this.project, allUserProvider, "", false, true, true,  false);
		this.qualityLetterBox = new TextFieldWithCompletion(this.project, qualityUserProvider, "", false, true,  true, false);
		this.supportLetterBox = new TextFieldWithCompletion(this.project, supportUserProvider, "", false, true,  true, false);
		this.documentationLetterBox = new TextFieldWithCompletion(this.project, documentationUserProvider, "", false, true,  true, false);
		PositiveNumberVerifier verifier = new PositiveNumberVerifier();
		this.minTranslationSize.setInputVerifier(verifier);
	}
}
