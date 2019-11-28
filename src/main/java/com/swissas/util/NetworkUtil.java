package com.swissas.util;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.swissas.beans.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * A Utility class to do some networking stuff
 *
 * @author Tavan Alain
 */

public class NetworkUtil {
	private static final Pattern PATTERN = Pattern.compile(".*LC:\\s+([A-Z]+).*Team:\\s+([A-Z]+).*");
	private static final ResourceBundle URL_BUNDLE = ResourceBundle.getBundle("urls");
	private static final String STAFF_URL = URL_BUNDLE.getString("url.staff");
	private static final String TRAFFIC_LIGHT_URL = URL_BUNDLE.getString("url.trafficlight");
	private static final String TRAFFIC_LIGHT_CLICK_URL = URL_BUNDLE.getString("url.trafficlight.click");
	private static final NetworkUtil INSTANCE = new NetworkUtil();
	
	private NetworkUtil() {
	}
	
	public static NetworkUtil getInstance() {
		return INSTANCE;
	}
	
	public void refreshUserMap() {
		Map<String, User> userMap = new TreeMap<>();
		try {
			Document doc = Jsoup.connect(STAFF_URL).get();
			Elements select = doc.select("tr.filterrow");
			for (Element element : select) {
				String withinTitleHtmlText = element.attr("title").replaceAll("\n+", "<br/>");
				Matcher matcher = PATTERN.matcher(withinTitleHtmlText);
				if(matcher.find()) {
					String lc = matcher.group(1);
					String team = matcher.group(2);
					String allInfos = "<html><body>" + withinTitleHtmlText + "</body></html>";
					userMap.put(lc, new User(lc, team, allInfos));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwissAsStorage.getInstance().setUserMap(userMap);
	}

	public Elements getTrafficLightContent() {
		Elements body = null;
		try {
			body = Jsoup.connect(TRAFFIC_LIGHT_CLICK_URL+ SwissAsStorage.getInstance().getFourLetterCode()).get().select("body");
		}catch (IOException e){
			e.printStackTrace();
		}
		return body;
	}
	
	
}
