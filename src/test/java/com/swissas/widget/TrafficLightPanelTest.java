package com.swissas.widget;

import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficLightPanelTest {
	
	private static Elements body(String html) {
		return Jsoup.parse(html).select("body");
	}
	
	private static final String HAPPY = "<table><tr><td><img src=\"happy.jpg\"></td></tr></table>";
	private static final String PREVIEW_BROKEN = "<table><tr><td>Build failed on preview</td></tr><tr><td>by ABC</td></tr></table>";
	private static final String OLD_BRANCH_BROKEN = "<table><tr><td>Build failed on v12-10</td></tr><tr><td>by ABC</td></tr></table>";
	
	@Test
	void happyServerTurnsTheGreenLightOn() {
		Map<String, String> colors = TrafficLightPanel.computeLampColors(body(HAPPY), "preview");
		
		assertThat(colors).containsExactly(Map.entry("green", "on"));
	}
	
	@Test
	void brokenCurrentBranchTurnsTheRedLightOn() {
		Map<String, String> colors = TrafficLightPanel.computeLampColors(body(PREVIEW_BROKEN), "preview");
		
		assertThat(colors).containsEntry("red", "on").containsEntry("yellow", "off").doesNotContainKey("green");
	}
	
	@Test
	void brokenOtherBranchTurnsTheYellowLightOn() {
		Map<String, String> colors = TrafficLightPanel.computeLampColors(body(PREVIEW_BROKEN), "19.6");
		
		assertThat(colors).containsEntry("yellow", "on").containsEntry("red", "off");
	}
	
	@Test
	void branchNameIsComparedIgnoringTheCase() {
		Map<String, String> colors = TrafficLightPanel.computeLampColors(body(OLD_BRANCH_BROKEN), "V12-10");
		
		assertThat(colors).containsEntry("red", "on").containsEntry("yellow", "off");
	}
	
	@Test
	void unknownBranchTurnsNothingOn() {
		assertThat(TrafficLightPanel.computeLampColors(body(PREVIEW_BROKEN), null)).isEmpty();
	}
	
	@Test
	void missingContentTurnsNothingOn() {
		assertThat(TrafficLightPanel.computeLampColors(null, "preview")).isEmpty();
	}
	
	@Test
	void severalRowsAreNotConsideredAsHappyEvenWithTheHappyPicture() {
		String html = "<table><tr><td><img src=\"happy.jpg\"></td></tr><tr><td>Build failed on preview</td></tr></table>";
		
		Map<String, String> colors = TrafficLightPanel.computeLampColors(body(html), "preview");
		
		assertThat(colors).doesNotContainKey("green").containsEntry("red", "on");
	}
}
