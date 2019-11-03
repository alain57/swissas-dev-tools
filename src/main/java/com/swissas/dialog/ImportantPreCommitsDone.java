package com.swissas.dialog;

import java.io.File;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.swissas.util.SwissAsStorage;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Dialog for the actions that may be mandatory before each commits.
 *
 * @author Tavan Alain
 */

public class ImportantPreCommitsDone extends DialogWrapper {
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
	private final CheckinProjectPanel checkinProjectPanel;
	private final boolean informQANeeded;
	private final boolean reviewSelected;
	
	private final Action informQAAction;
	
	public ImportantPreCommitsDone(CheckinProjectPanel checkinProjectPanel){
		super(true);
		this.checkinProjectPanel = checkinProjectPanel;
		this.informQANeeded = informQANeeded();
		this.reviewSelected = SwissAsStorage.getInstance().isPreCommitCodeReview();
		this.informQAAction = new DialogWrapperExitAction(RESOURCE_BUNDLE.getString("precommit.inform_qa"), DialogWrapper.NEXT_USER_EXIT_CODE);
		setTitle("PreCommit Important Tasks");
		setOKButtonText("Yes");
		setCancelButtonText("No");
		
		init();
		
		
	}
	
	private boolean informQANeeded(){
		if(SwissAsStorage.getInstance().isPreCommitInformQA()) {
			Predicate<String> isClientOrWebPredicate = path -> path.contains("amos" + File.separator + "client") || path.contains("amos" + File.separator + "web");
			return this.checkinProjectPanel.getFiles().stream().map(File::getPath).anyMatch(isClientOrWebPredicate);
		}
		return false;
	}
	
	
	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		JPanel panel = new JPanel(new VerticalLayout());
		StringBuilder text = new StringBuilder("<html>");
		if(this.reviewSelected){
			text.append("Code review done?<br/>");
		}
		if(this.informQANeeded){
			text.append("QA was informed?");
		}
		JLabel label = new JLabel(text.toString());
		panel.add(label);
		return panel;
	}
	
	@NotNull
	@Override
	protected Action[] createActions() {
		return this.informQANeeded ? new Action[]{getOKAction(), getInformQAAction(), getCancelAction()} :
				new Action[]{getOKAction(), getCancelAction()};
	}
	
	private Action getInformQAAction() {
		return this.informQAAction;
	}
}
