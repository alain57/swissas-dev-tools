package com.swissas.checkin;


import java.io.File;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.PairConsumer;
import com.swissas.dialog.ConfirmationDialog;
import com.swissas.dialog.ImportantPreCommits;
import com.swissas.util.ProjectUtil;
import com.swissas.util.SwissAsStorage;
import com.swissas.widget.TrafficLightPanel;
import org.jetbrains.annotations.NonNls;

/**
 * The pre commit checking that will prevent committing with empty message, incorrect traffic light indicator
 * or if the definition of done is not respected.
 *
 * @author Tavan Alain
 */
class PreCommitCheckingHandler extends CheckinHandler {
	
	public static final String FIX_RELEASE_BLOCKER = "fix release blocker";
	
	private static final ResourceBundle RESOURCE_BUNDLE  = ResourceBundle.getBundle("texts");
	@NonNls
	private static final String         TITLE            = RESOURCE_BUNDLE
			.getString("commit.title");
	@NonNls
	private static final String         EMPTY_COMMIT_MSG = RESOURCE_BUNDLE
			.getString("commit.without.message");
	
	private final Project             project;
	private final CheckinProjectPanel checkinProjectPanel;
	
	private TrafficLightPanel trafficLightPanel = null;
	private ImportantPreCommits importantPreCommitsDialog;
	
	PreCommitCheckingHandler(CheckinProjectPanel checkinProjectPanel) {
		this.project = checkinProjectPanel.getProject();
		this.checkinProjectPanel = checkinProjectPanel;
		IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(this.project);
		if (ideFrame != null && ideFrame.getStatusBar() != null) {
			this.trafficLightPanel = (TrafficLightPanel) ideFrame.getStatusBar().getWidget(
					TrafficLightPanel.WIDGET_ID);
		}
	}
	
	@Override
	public void checkinSuccessful() {
		super.checkinSuccessful();
		this.importantPreCommitsDialog.sendMail();
	}
	
	@Override
	public ReturnResult beforeCheckin(CommitExecutor executor,
	                                  PairConsumer<Object, Object> additionalDataConsumer) {
		if (DumbService.getInstance(this.project).isDumb()) {
			Messages.showErrorDialog(this.project, RESOURCE_BUNDLE.getString("commit.not.possible"),
			                         TITLE);
			return ReturnResult.CANCEL;
		}
		
		if (this.checkinProjectPanel.getCommitMessage().trim().isEmpty()) {
			Messages.showDialog(this.project,
			                    EMPTY_COMMIT_MSG,
			                    TITLE,
			                    new String[]{ RESOURCE_BUNDLE.getString("ok") },
			                    1,
			                    null);
			return ReturnResult.CANCEL;
		}
		ReturnResult result;
		if (this.trafficLightPanel != null && this.trafficLightPanel.isRedOrYellowOn()) {
			result = showTrafficLightDialog();
		} else {
			result = ReturnResult.COMMIT;
		}
		
		if (result == ReturnResult.COMMIT && !displayPreCommitChecksIfNeeded()) {
			result = ReturnResult.CANCEL;
		}
		
		return result;
	}
	
	
	private boolean displayPreCommitChecksIfNeeded() {
		boolean result = true;
		this.importantPreCommitsDialog = new ImportantPreCommits(this.checkinProjectPanel);
		if (ProjectUtil.getInstance().isAmosProject(this.project)) {
		    boolean informOther = informOtherPeopleNeeded(); 
			if(informOther || SwissAsStorage.getInstance().isPreCommitCodeReview()) {
				this.importantPreCommitsDialog.refreshContent(informOther);
				this.importantPreCommitsDialog.setVisible(true);
				result = this.importantPreCommitsDialog.getExitCode() != DialogWrapper.CANCEL_EXIT_CODE;
			}
		}
		
		return result;
	}
	
	private boolean informOtherPeopleNeeded() {
		if (SwissAsStorage.getInstance().isPreCommitInformOther()) {
			String clientPath = "amos" + File.separator + "client";
			Predicate<String> isClientOrWebPredicate = path -> path.contains(clientPath)
			                                                   && 
			                                                   !path.contains(
					                                                   clientPath + File.separator +
					                                                   "unpublic") 
			                                                   || path.contains(
					"amos" + File.separator + "web");
			return this.checkinProjectPanel.getFiles().stream().map(File::getPath)
			                               .anyMatch(isClientOrWebPredicate);
		}
		return false;
	}
	
	private ReturnResult showTrafficLightDialog() {
		ConfirmationDialog confirmationDialog = new ConfirmationDialog(
				this.trafficLightPanel.getTrafficDetails());
		confirmationDialog.show();
		int messageResult = confirmationDialog.getExitCode();
		if (messageResult == DialogWrapper.NEXT_USER_EXIT_CODE) {
			this.trafficLightPanel.setInformWhenReady(this.checkinProjectPanel);
		} else if (messageResult == ConfirmationDialog.FIX_RELEASE_EXIT_CODE) {
			if (!this.checkinProjectPanel.getCommitMessage().contains(FIX_RELEASE_BLOCKER)) {
				this.checkinProjectPanel.setCommitMessage(FIX_RELEASE_BLOCKER + " \n\n" +
				                                          this.checkinProjectPanel
						                                          .getCommitMessage());
			}
			messageResult = DialogWrapper.OK_EXIT_CODE;
		}
		return messageResult == DialogWrapper.OK_EXIT_CODE ? ReturnResult.COMMIT
		                                                   : ReturnResult.CLOSE_WINDOW;
	}
}
