package com.swissas.checkin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.swissas.beans.Failure;
import com.swissas.util.SwissAsStorage;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;

public class FailureDialogWrapper extends DialogWrapper {
	Set<Failure> failuresToMarkAsFix = new HashSet<>();

	protected FailureDialogWrapper() {
		super(true);
		setTitle("Choose the Failure(S) This Commit Should Fix");
	}

	@Override
	protected @Nullable JComponent createCenterPanel() {
		JBPanel dialogPanel = new JBPanel(new MigLayout());
		List<JBCheckBox> checkBoxList = new ArrayList<>();
		for (Failure failure : SwissAsStorage.getInstance().getMyFailures()) {
			JBCheckBox checkBox = new JBCheckBox(failure.getName());
			checkBox.setHorizontalTextPosition(SwingConstants.LEFT);
			checkBox.addActionListener(l -> {
				if(checkBox.isSelected()){
					this.failuresToMarkAsFix.add(failure);
				}else{
					this.failuresToMarkAsFix.remove(failure);
				}
				checkBoxList.add(checkBox);
			});
		}

		JBCheckBox all = new JBCheckBox("Fix all issues");
		all.addActionListener(l -> checkBoxList.forEach(c -> c.setSelected(all.isSelected())));
		
		dialogPanel.add(all, "wrap");
		checkBoxList.forEach(c -> dialogPanel.add(c, "wrap"));
		
		return dialogPanel;
	}
	
	public Set<Failure> getMarkAsFixed() {
		return this.failuresToMarkAsFix;
	}
}
