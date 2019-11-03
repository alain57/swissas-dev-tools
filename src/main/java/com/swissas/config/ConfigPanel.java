package com.swissas.config;

import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.TextFieldWithAutoCompletion.StringsCompletionProvider;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import com.swissas.beans.LabelData;
import com.swissas.beans.User;
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
	private JPanel warningPanel;
	private JPanel translationPanel;
	private JCheckBox chkTranslateOnlyModifiedLines;
	private JPanel preCommitPanel;
	private JCheckBox preCommitCodeReviewCheckbox;
	private JCheckBox preCommitInformQACheckbox;
	private TextFieldWithCompletion qaLetterBox;
	private JPasswordField accountPassword;
	
	private JCheckBox chkShowIgnoreLists;
	private JList<LabelData> lstIgnoreValues;
	
	private final Project project;
	private final SwissAsStorage storage;
	
	
	public ConfigPanel(Project project, SwissAsStorage storage) {
		this.project = project;
		this.storage = storage;
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
	
	public JPasswordField getAccountPassword() {
		return this.accountPassword;
	}
	
	public TextFieldWithCompletion getQaLetterBox() {
		return this.qaLetterBox;
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
	
	public JCheckBox getPreCommitInformQACheckbox() {
		return this.preCommitInformQACheckbox;
	}
	
	private void createUIComponents() {
		this.minTranslationSize = new JTextField("5");
		Set<String> allQACodes = this.storage.getUserMap().values().stream().filter(user -> user.hasTextInInfos("Team: QA"))
				.map(User::getLCAndName).collect(Collectors.toSet());
		StringsCompletionProvider allUserProvider = new StringsCompletionProvider(this.storage.getUserMap().keySet(), null);
		StringsCompletionProvider qaUserProvider = new StringsCompletionProvider(allQACodes, null);
		this.fourLetterCode = new TextFieldWithCompletion(this.project, allUserProvider, "", false, false, false, false);
		this.qaLetterBox = new TextFieldWithCompletion(this.project, qaUserProvider, "", false, false, false, false);
		PositiveNumberVerifier verifier = new PositiveNumberVerifier();
		this.minTranslationSize.setInputVerifier(verifier);
		this.accountPassword = new JPasswordField(this.storage.getPassword());
	}
}
