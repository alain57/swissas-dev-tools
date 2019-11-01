package com.swissas.dialog;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.swissas.util.AutoCompletion;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

/**
 * a simple dialog with an auto combobox
 *
 * @author TALA
 */

public class EditableDialogChooser extends DialogWrapper {
	private final String message;
	private final String[] values;
	private JComboBox<String> comboBox;
	
	public EditableDialogChooser(String message,
								 @Nls(capitalization = Nls.Capitalization.Title) String title,
								 String[] values){
		super(true);
		setTitle(title);
		this.message = message;
		this.values = values;
		init();
	}
	
	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		JPanel content = new JPanel(new BorderLayout());
		JLabel label = new JLabel(this.message);
		this.comboBox = new ComboBox<>(this.values);
		AutoCompletion.enable(this.comboBox);
		content.add(label, BorderLayout.WEST);
		content.add(this.comboBox, BorderLayout.EAST);
		return content;
	}
	
	public String getInputValue(){
		return showAndGet() &&  this.comboBox.getSelectedItem() != null ? this.comboBox.getSelectedItem().toString() : null;
	}
	

}
