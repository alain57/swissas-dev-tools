package com.swissas.toolwindow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.Alarm;
import com.swissas.beans.Directory;
import com.swissas.beans.Type;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * The warning content panel where all the different code warning coming from the company will be displayed.
 * <p>
 * One instance exists per project, it is created by {@link WarningContentToolWindowFactory}.
 *
 * @author Tavan Alain
 */

public class WarningContent extends JBTabbedPane implements Disposable {
	
	private static final Logger LOGGER = Logger.getInstance("Swiss-as");
	private static final String TODO_SEARCH_MESSAGE = "Complete the task associated to this TODO comment.";

	private static final String MESSAGE_URL = ResourceBundle.getBundle("urls").getString("url.warnings");
	public static final  String ID          = "SAS Warnings";
	public static final String CODE_CHECK = "type[ident~=[^CODE_CHECK]]";

	private static final long REFRESH_DELAY_MS = TimeUnit.DAYS.toMillis(1);

	private static final String USERNAME = "jenkinsreadonly";
	private static final String TOKEN = readToken();
	
	private final Set<Type> types = new TreeSet<>();
	private int baseTabCount = 0;
	
	private final SwissAsStorage swissAsStorage;
	private final Project        project;
	private final Alarm          alarm;

	private static String readToken() {
		try {
			return ResourceBundle.getBundle("token").getString("finding_token");
		} catch (MissingResourceException e) {
			LOGGER.warn("token.properties is missing (copy token.properties.example), the warnings can't be read");
			return "";
		}
	}

	public WarningContent(@NotNull Project project) {
		this.project = project;
		this.swissAsStorage = SwissAsStorage.getInstance();
		this.alarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
		scheduleRefresh(0);
	}

	private void scheduleRefresh(long delay) {
		if (this.alarm.isDisposed()) {
			return;
		}
		this.alarm.addRequest(() -> {
			try {
				doRefresh();
			} finally {
				scheduleRefresh(REFRESH_DELAY_MS);
			}
		}, delay);
	}

	/**
	 * Refreshes the content, the network access is always done outside the EDT.
	 */
	public void refresh() {
		if (this.alarm.isDisposed()) {
			return;
		}
		this.alarm.addRequest(this::doRefresh, 0);
	}

	private void doRefresh() {
		if (this.project.isDisposed() || this.swissAsStorage.getFourLetterCode().isEmpty()) {
			return;
		}
		Set<Type> newTypes = readWarningsAndFindings();
		if (newTypes == null) {
			return; //nothing could be read (network issue), keep what is currently displayed
		}
		//the types are only modified and read on the EDT
		ApplicationManager.getApplication().invokeLater(() -> {
			if (this.project.isDisposed()) {
				return;
			}
			this.types.clear();
			this.types.addAll(newTypes);
			int selectedTab = getSelectedIndex() == -1 ? 0 : getSelectedIndex();
			fillView();
			if (getTabCount() > selectedTab) {
				setSelectedIndex(selectedTab);
			}
		});
	}
	
	private Elements readUrlAndUseCssSelector(String url){
		Elements result = null;
		var encoding = Base64.getEncoder().encodeToString((USERNAME + ":" + TOKEN).getBytes(StandardCharsets.UTF_8));
		try{
			result = Jsoup.connect(url)
					.timeout(20_000)
					.header("Authorization", "Basic " + encoding)
					.post().select(WarningContent.CODE_CHECK);
		}catch (IOException e){
			LOGGER.info(e);
		}
		
		return result;
	}
	
	/**
	 * @return the warning types read from the server or null if nothing could be read at all.
	 */
	private Set<Type> readWarningsAndFindings() {
		String fourLetterCode = this.swissAsStorage.getFourLetterCode();
		if (fourLetterCode.isEmpty()) {
			return null;
		}
		Elements allFindings = new Elements();
		List<String> letterCodes = new ArrayList<>();
		letterCodes.add(fourLetterCode);
		if (this.swissAsStorage.hasTeam()) {
			letterCodes.add(this.swissAsStorage.getMyTeam());
		}
		letterCodes.addAll(this.swissAsStorage.getMyTeamMembers(false, false));
		//one HTTP call per letter code: done in parallel instead of one after the other
		List<Elements> results = letterCodes.parallelStream()
		                                    .map(code -> readUrlAndUseCssSelector(String.format(MESSAGE_URL, code)))
		                                    .toList();
		boolean anythingRead = false;
		for (Elements findings : results) {
			if (findings != null) {
				anythingRead = true;
				allFindings.addAll(findings);
			}
		}
		if (!anythingRead) {
			return null;
		}
		Set<Type> newTypes = new TreeSet<>();
		Map<String, Directory> directories = new TreeMap<>();
		for (Element type : allFindings) {
			WarningContentHelper.generateTypeFromElementTypeAndAddItToTypeSet(type, newTypes, directories);
		}
		return newTypes;
	}

	
	private void fillView() {
		removeAll();
		if (!this.types.isEmpty()) {
			Type sonarType = null;
			for(Type type : this.types) {
				String typeName = type.getMainAttribute();
				if("Sonar".equals(typeName)) {
					sonarType = type;
				}
				add(typeName, new WarningContentTreeView(this.project, type, this, null));
			}
			if (sonarType != null) {
				add("Team TODOS", new WarningContentTreeView(this.project, sonarType, this, TODO_SEARCH_MESSAGE));
			}
		}
		this.baseTabCount = getTabCount();
	}
	
	public void filterSimilar(String message) {
		while(getTabCount() > this.baseTabCount) {
			removeTabAt(this.baseTabCount);
		}
		for(Type type : this.types) {
			String typeName = type.getMainAttribute();
			add(String.format( "Similar %s result", typeName), new WarningContentTreeView(this.project, type, this, message, true));
		}
	}

	@Override
	public void dispose() {
		this.types.clear();
	}
}
