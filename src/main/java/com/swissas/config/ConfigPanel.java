package com.swissas.config;

import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.intellij.openapi.ui.ComboBox;
import com.swissas.beans.LabelData;
import com.swissas.util.AutoCompletion;
import com.swissas.util.PositiveNumberVerifier;
import com.swissas.util.SwissAsStorage;

/**
 * TODO: write you class description here
 *
 * @author
 */

class ConfigPanel {
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
	
	private ComboBox<String> fourLetterCode;
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

	private JCheckBox chkShowIgnoreLists;
	private JList<LabelData> lstIgnoreValues;
	
	private final SwissAsStorage storage;


	public ConfigPanel(SwissAsStorage storage) {
		this.storage = storage;
	}

	public JPanel getMainPanel() {
		return this.mainPanel;
	}

	public JTextField getMinTranslationSize() {
		return this.minTranslationSize;
	}

	public JPanel getWarningPanel() {
		return this.warningPanel;
	}

	public ComboBox<String> getFourLetterCode() {
		return this.fourLetterCode;
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

	public JPanel getGeneralPanel() {
		return this.generalPanel;
	}

	public JPanel getJenkinsPanel() {
		return this.jenkinsPanel;
	}


	private void createUIComponents() {
		this.minTranslationSize = new JTextField("5");
		PositiveNumberVerifier verifier = new PositiveNumberVerifier();
		this.minTranslationSize.setInputVerifier(verifier);
		this.fourLetterCode = new ComboBox<>();
		this.storage.getUserMap().keySet().forEach(this.fourLetterCode::addItem);
		AutoCompletion.enable(this.fourLetterCode);
	}
	
}
