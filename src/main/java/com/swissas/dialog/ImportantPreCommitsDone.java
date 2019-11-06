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
 * The Dialog for the actions that may be mandatory before each commit.
 *
 * @author Tavan Alain
 */

public class ImportantPreCommitsDone extends DialogWrapper {
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
	private final CheckinProjectPanel checkinProjectPanel;
	private final boolean informOtherPeopleNeeded;
	private final boolean reviewSelected;
	private final Action informOtherPeopleAction;
	
	public ImportantPreCommitsDone(CheckinProjectPanel checkinProjectPanel){
		super(true);
		this.checkinProjectPanel = checkinProjectPanel;
		this.informOtherPeopleNeeded = informOtherPeopleNeeded();
		this.reviewSelected = SwissAsStorage.getInstance().isPreCommitCodeReview();
		this.informOtherPeopleAction = new DialogWrapperExitAction(RESOURCE_BUNDLE.getString("precommit.send_information_mail"), DialogWrapper.NEXT_USER_EXIT_CODE);
		setTitle("PreCommit Important Tasks");
		setOKButtonText("Yes");
		setCancelButtonText("No");
		init();
	}
	
	private boolean informOtherPeopleNeeded(){
		if(SwissAsStorage.getInstance().isPreCommitInformOther()) {
			String clientPath = "amos" + File.separator + "client";
			Predicate<String> isClientOrWebPredicate = path -> path.contains(clientPath) && !path.contains(clientPath + File.separator + "unpublic") || path.contains("amos" + File.separator + "web");
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
			text.append("Code review done?<br/>");//TODO: once Jetbrains fix IDEA-221550 allow to type the Reviewer and modify the commit message.
		}
		if(this.informOtherPeopleNeeded){
			text.append("Was the graphical change Communicated?");
		}
		JLabel label = new JLabel(text.toString());
		panel.add(label);
		return panel;
	}
	
	@NotNull
	@Override
	protected Action[] createActions() {
		return this.informOtherPeopleNeeded ? new Action[]{getOKAction(), getInformOtherPeopleAction(), getCancelAction()} :
				new Action[]{getOKAction(), getCancelAction()};
	}
	
	private Action getInformOtherPeopleAction() {
		return this.informOtherPeopleAction;
	}
}
