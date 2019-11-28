package com.swissas.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.TextFieldWithAutoCompletion.StringsCompletionProvider;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

/**
 * a simple dialog with a text field completion
 *
 * @author Tavan Alain
 */

public class EditableDialogChooser extends DialogWrapper {
	private final String message;
	private final Collection<String> values;
	private final Project project;
	private TextFieldWithCompletion textFieldWithCompletion;
	
	public EditableDialogChooser(Project project, String message,
								 @Nls(capitalization = Nls.Capitalization.Title) String title,
								 Collection<String> values){
		super(true);
		setTitle(title);
		this.project = project;
		this.message = message;
		this.values = values;
		init();
	}
	
	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		JPanel content = new JPanel(new BorderLayout());
		JLabel label = new JLabel(this.message);
		StringsCompletionProvider provider = new StringsCompletionProvider(this.values, null);
		this.textFieldWithCompletion = new TextFieldWithCompletion(this.project, provider, "", true, true, false);
		this.textFieldWithCompletion.setPreferredSize(new Dimension(100, this.textFieldWithCompletion.getPreferredSize().height));
		
		content.add(label, BorderLayout.WEST);
		content.add(this.textFieldWithCompletion, BorderLayout.EAST);
		return content;
	}
	
	public String getInputValue(){
		return isOK() ? this.textFieldWithCompletion.getText() : null;
	}
	
	@Nullable
	@Override
	public JComponent getPreferredFocusedComponent() {
		return this.textFieldWithCompletion;
	}
}
