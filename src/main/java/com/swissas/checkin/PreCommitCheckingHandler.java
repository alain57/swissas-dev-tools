package com.swissas.checkin;


import java.io.File;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Matcher;

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
import com.swissas.util.StringUtils;
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
	
	@NonNls
	private static final String         TITLE            = ResourceBundle.getBundle("texts")
			.getString("commit.title");
	@NonNls
	private static final String         EMPTY_COMMIT_MSG = ResourceBundle.getBundle("texts")
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
		if(this.importantPreCommitsDialog != null) {
			this.importantPreCommitsDialog.sendMail();
		}
	}
	
	@Override
	public ReturnResult beforeCheckin(CommitExecutor executor,
	                                  PairConsumer<Object, Object> additionalDataConsumer) {
		if (warnIfIndexInProgress() || warnIfCommitMessageEmpty()){
			return ReturnResult.CANCEL;
		}
		
		if (this.checkinProjectPanel.getCommitMessage().trim().isEmpty()) {
			Messages.showDialog(this.project,
			                    EMPTY_COMMIT_MSG,
			                    TITLE,
			                    new String[]{  ResourceBundle.getBundle("texts").getString("ok") },
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
	
	private boolean warnIfIndexInProgress() {
		boolean result = false;
		if (DumbService.getInstance(this.project).isDumb()) {
			Messages.showErrorDialog(this.project, ResourceBundle.getBundle("texts")
			                                                     .getString("commit.not.possible"),
			                         TITLE);
			result = true;
		}
		return result;
	}
	
	private boolean warnIfCommitMessageEmpty() {
		boolean result = false;
		if (this.checkinProjectPanel.getCommitMessage().trim().isEmpty()) {
			Messages.showDialog(this.project,
			                    EMPTY_COMMIT_MSG,
			                    TITLE,
			                    new String[]{ ResourceBundle.getBundle("texts").getString("ok") },
			                    1,
			                    null);
			result = true;
		}
		return result;
	}
	
	private boolean isCodeReviewRequired() {
		boolean result = false;
		if(SwissAsStorage.getInstance().isPreCommitCodeReview()) {
			Matcher matcher = ImportantPreCommits.REVIEWER
					.matcher(this.checkinProjectPanel.getCommitMessage());
			if(matcher.find()){
				String potentialReviewer = matcher.group(1);
				result = !StringUtils.getInstance().isLetterCode(potentialReviewer);
			}else {
				result = true;
			}
		}
		return result;
	}
	
	
	private boolean displayPreCommitChecksIfNeeded() {
		boolean result = true;
		this.importantPreCommitsDialog = new ImportantPreCommits(this.checkinProjectPanel);
		if (ProjectUtil.getInstance().isAmosProject(this.project)) {
		    boolean informOther = informOtherPeopleNeeded(); 
			if(informOther || isCodeReviewRequired()) {
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
		ConfirmationDialog confirmationDialog = new ConfirmationDialog(this.trafficLightPanel.getTrafficDetails(), this.checkinProjectPanel);
		confirmationDialog.show();
		int messageResult = confirmationDialog.getExitCode();
		if (messageResult == DialogWrapper.NEXT_USER_EXIT_CODE) {
			this.trafficLightPanel.setInformWhenReady(this.checkinProjectPanel);
		}
		return messageResult == DialogWrapper.OK_EXIT_CODE ? ReturnResult.COMMIT
		                                                   : ReturnResult.CLOSE_WINDOW;
	}
}
