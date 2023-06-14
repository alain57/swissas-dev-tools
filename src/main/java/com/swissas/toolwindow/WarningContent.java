package com.swissas.toolwindow;

import java.io.IOException;
import java.util.*;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.ContentFactory;
import com.swissas.beans.Directory;
import com.swissas.beans.Type;
import com.swissas.util.ProjectUtil;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * The warning content panel where all the different code warning coming from the company will be displayed
 *
 * @author Tavan Alain
 */

public class WarningContent extends JBTabbedPane implements ToolWindowFactory {
	
	private static final String WARNING_CONTENT_TIMER = "WarningContentTimer";
	private static final String TODO_SEARCH_MESSAGE = "Complete the task associated to this TODO comment.";

	private static final String MESSAGE_URL = ResourceBundle.getBundle("urls").getString("url.warnings");
	public static final  String ID          = "SAS Warnings";
	public static final String CODE_CHECK = "type[ident~=[^CODE_CHECK]]";
	
	private final Set<Type>              types       = new TreeSet<>();
	private final Map<String, Directory> directories = new TreeMap<>();
	
	private       SwissAsStorage       swissAsStorage;
	private Project                    project;

	private static final String USERNAME = "jenkinsreadonly";
	private static final String TOKEN = ResourceBundle.getBundle("token").getString("finding_token");

	
	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		if (ProjectUtil.getInstance().isAmosProject(project)) {
			this.project = project;
			this.swissAsStorage = SwissAsStorage.getInstance();
			var contentFactory = ContentFactory.getInstance();
			var content = contentFactory.createContent(this, "", false);
			toolWindow.getContentManager().addContent(content);
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					WarningContent.this.refresh();
				}
			};
			var timer = new Timer(WARNING_CONTENT_TIMER);
			timer.schedule(timerTask, 30, 24 * 60 * 60_000L);
		}else {
			this.project = null;
		}
	}
	
	public void refresh() {
		ApplicationManager.getApplication().invokeLater(this::doRefresh);
	}
	private void doRefresh() {
		if (!"".equals(this.swissAsStorage.getFourLetterCode())) {
			int selectedTab = this.getSelectedIndex() == -1 ? 0 : this.getSelectedIndex();
			if(readWarningsAndFindings()) {
				fillView();
				if (this.getTabCount() > selectedTab) {
					this.setSelectedIndex(selectedTab);
				}
			}
		}
	}
	
	private Elements readUrlAndUseCssSelector(String url, String cssSelector){
		Elements result = null;
		var encoding = Base64.getEncoder().encodeToString((USERNAME + ":" + TOKEN).getBytes());
		try{
			result = Jsoup.connect(url)
					.header("Authorization", "Basic " + encoding)
					.post().select(cssSelector);
		}catch (IOException e){
			Logger.getInstance("Swiss-as").info(e);
		}
		
		return result;
	}
	
	private boolean readWarningsAndFindings() {
		if (!this.swissAsStorage.getFourLetterCode().isEmpty()) {
			this.types.clear();
			Elements myFindings = readUrlAndUseCssSelector(String.format(MESSAGE_URL, this.swissAsStorage.getFourLetterCode())  , CODE_CHECK);
			Elements teamAccountFindings = readUrlAndUseCssSelector(String.format(MESSAGE_URL, this.swissAsStorage.getMyTeam()) , CODE_CHECK);
			assert teamAccountFindings != null;
			if(myFindings != null) {
				teamAccountFindings.addAll(myFindings);
			}
			for(var member : this.swissAsStorage.getMyTeamMembers(false, false)) {
				Elements userWarnings = readUrlAndUseCssSelector(String.format(MESSAGE_URL, member), CODE_CHECK);
				if(userWarnings != null) {
					teamAccountFindings.addAll(userWarnings);
				}
			}
			for (Element type : teamAccountFindings) {
				WarningContentHelper.generateTypeFromElementTypeAndAddItToTypeSet(type, this.types, this.directories);
			}
		}
		return true;
	}

	
	private void fillView() {
		removeAll();
		if (!this.types.isEmpty()) {
			Type sonarType = null;
			for(Type type : this.types) {
				String typeName = type.getMainAttribute();
				if(type.getMainAttribute().equals("Sonar")) {
					sonarType = type;
				}
				add(typeName, new WarningContentTreeView(this.project, type, this, null));
			}
			assert sonarType != null;
			add("Team TODOS", new WarningContentTreeView(this.project, sonarType, this, TODO_SEARCH_MESSAGE)); 
		}
	}
	
	public void filterSimilar(String message) {
		int tabsThatShouldNotBeRemoved = this.types.size() + 1; //+1 because of the team to do tab  
		while(getTabCount() > tabsThatShouldNotBeRemoved) {
			removeTabAt(tabsThatShouldNotBeRemoved);
		}
		for(Type type : this.types) {
			String typeName = type.getMainAttribute();
			add(String.format( "Similar %s result", typeName), new WarningContentTreeView(this.project, type, this, message));
		}
	}
}
