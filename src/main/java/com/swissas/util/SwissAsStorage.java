package com.swissas.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.Consumer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.swissas.beans.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The storage class of the plugin.
 *
 * @author Tavan Alain
 */

@State(name = "SwissAsStorage", storages = @Storage("swissas_settings.xml"))
public class SwissAsStorage implements PersistentStateComponent<SwissAsStorage> {
	private static final String  MAIL_SUFFIX                     = "@swiss-as.com";
	private              String  fourLetterCode                  = "";
	private              String  myTeam                          = "";
	private              String  qaLetterCode                    = "";
	private              String  qaMail                          = "";
	private              String  docuLetterCode                  = "";
	private              String  docuMail                        = "";
	private              String  supportLetterCode               = "";
	private              String  supportMail                     = "";
	private              boolean horizontalOrientation           = true;
	private              String  minWarningSize                  = "5";
	private              boolean fixMissingOverride              = true;
	private              boolean fixMissingThis                  = true;
	private              boolean fixUnusedSuppressWarning        = false;
	private              boolean fixMissingAuthor                = true;
	private              boolean translationOnlyCheckChangedLine = false;
	private              boolean preCommitInformOther            = false;
	private              boolean preCommitCodeReview             = true;
	
	private       boolean           showIgnoredValues = false;
	private final List<String>      ignoredValues;
	private final Map<String, User> userMap;
	private       Properties        shareProperties;
	private       boolean           isNewTranslation  = false;
	
	public SwissAsStorage() {
		this.ignoredValues = new ArrayList<>();
		this.userMap = new HashMap<>();
	}
	
	public static SwissAsStorage getInstance() {
		return ServiceManager.getService(SwissAsStorage.class);
	}
	
	@Nullable
	@Override
	public SwissAsStorage getState() {
		return this;
	}
	
	@Override
	public void loadState(@NotNull SwissAsStorage state) {
		XmlSerializerUtil.copyBean(state, this);
	}
	
	public String getFourLetterCode() {
		return this.fourLetterCode;
	}
	
	public String getQaLetterCode() {
		return this.qaLetterCode;
	}
	
	
	public String getDocuLetterCode() {
		return this.docuLetterCode;
	}
	
	public void setDocuLetterCode(String docuLetterCode) {
		this.docuLetterCode = docuLetterCode;
		setLetterCodeToFunction(docuLetterCode, this::setDocuMail);
	}
	
	public void setSupportLetterCode(String supportLetterCode) {
		this.supportLetterCode = supportLetterCode;
		setLetterCodeToFunction(supportLetterCode, this::setSupportMail);
	}
	
	public void setQaLetterCode(String qaLetterCode) {
		this.qaLetterCode = qaLetterCode;
		setLetterCodeToFunction(qaLetterCode, this::setQaMail);
	}
	
	private void setLetterCodeToFunction(String letterCode, Consumer<String> consumer) {
		String valueToPass =  letterCode.isEmpty() ? null : letterCode.substring(0, letterCode.indexOf(' ')) + MAIL_SUFFIX;
		consumer.consume(valueToPass);
	}
	
	public String getSupportLetterCode() {
		return this.supportLetterCode;
	}
	
	public void setFourLetterCode(String fourLetterCode) {
		this.fourLetterCode = fourLetterCode;
		fillMyTeam();
	}
	
	private void fillMyTeam() {
		if (!this.fourLetterCode.isEmpty() && !this.userMap.isEmpty()
		    && this.userMap.containsKey(this.fourLetterCode)) {
			this.myTeam = this.userMap.get(this.fourLetterCode).getTeam();
		} else {
			this.myTeam = "";
		}
	}
	
	public Set<String> getMyTeamMembers() {
		if (!this.myTeam.isEmpty()) {
			return this.userMap.values().stream().filter(user -> user.isInTeam(this.myTeam))
                               .map(User::getLc).filter(lc -> !lc.equalsIgnoreCase(this.fourLetterCode))
			                   .collect(Collectors.toCollection(TreeSet::new));
		}
		return Collections.emptySet();
		
	}
	
	public boolean isHorizontalOrientation() {
		return this.horizontalOrientation;
	}
	
	public void setHorizontalOrientation(boolean horizontalOrientation) {
		this.horizontalOrientation = horizontalOrientation;
	}
	
	public boolean isFixMissingOverride() {
		return this.fixMissingOverride;
	}
	
	public boolean isFixMissingThis() {
		return this.fixMissingThis;
	}
	
	public void setFixMissingThis(boolean fixMissingThis) {
		this.fixMissingThis = fixMissingThis;
	}
	
	public boolean isTranslationOnlyCheckChangedLine() {
		return this.translationOnlyCheckChangedLine;
	}
	
	public void setTranslationOnlyCheckChangedLine(boolean translationOnlyCheckChangedLine) {
		this.translationOnlyCheckChangedLine = translationOnlyCheckChangedLine;
	}
	
	public boolean isFixUnusedSuppressWarning() {
		return this.fixUnusedSuppressWarning;
	}
	
	public boolean isFixMissingAuthor() {
		return this.fixMissingAuthor;
	}
	
	public boolean isShowIgnoredValues() {
		return this.showIgnoredValues;
	}
	
	
	public List<String> getIgnoredValues() {
		return this.ignoredValues;
	}
	
	
	public String getQaMail() {
		return this.qaMail;
	}
	
	public void setQaMail(String qaMail) {
		this.qaMail = qaMail;
	}
	
	public String getDocuMail() {
		return this.docuMail;
	}
	
	public void setDocuMail(String docuMail) {
		this.docuMail = docuMail;
	}
	
	public String getSupportMail() {
		return this.supportMail;
	}
	
	public String getMyMail() {
		return this.fourLetterCode + MAIL_SUFFIX;
	}
	
	public void setSupportMail(String supportMail) {
		this.supportMail = supportMail;
	}
	
	public Map<String, User> getUserMap() {
		return Collections.unmodifiableMap(this.userMap);
	}
	
	public void setUserMap(Map<String, User> userMap) {
		if (this.userMap.size() != userMap.size() || !this.userMap.equals(userMap)) {
			this.userMap.clear();
			this.userMap.putAll(userMap);
			fillMyTeam();
		}
	}
	
	public void setFixMissingOverride(boolean fixMissingOverride) {
		this.fixMissingOverride = fixMissingOverride;
	}
	
	public void setFixUnusedSuppressWarning(boolean fixUnusedSuppressWarning) {
		this.fixUnusedSuppressWarning = fixUnusedSuppressWarning;
	}
	
	public void setFixMissingAuthor(boolean fixMissingAuthor) {
		this.fixMissingAuthor = fixMissingAuthor;
		
	}
	
	public void setShowIgnoredValues(boolean showIgnoredValues) {
		this.showIgnoredValues = showIgnoredValues;
	}
	
	public void setIgnoredValues(List<String> ignoredValues) {
		if (this.ignoredValues.size() != ignoredValues.size() ||
		    !this.ignoredValues.equals(ignoredValues)) {
			this.ignoredValues.clear();
			this.ignoredValues.addAll(ignoredValues);
		}
	}
	
	public String getMinWarningSize() {
		return this.minWarningSize;
	}
	
	public void setMinWarningSize(String minWarningSize) {
		this.minWarningSize = minWarningSize;
	}
	
	public Properties getShareProperties() {
		return this.shareProperties;
	}
	
	public void setShareProperties(Properties shareProperties) {
		this.shareProperties = shareProperties;
	}
	
	public boolean isNewTranslation() {
		return this.isNewTranslation;
	}
	
	public void setNewTranslation(boolean newTranslation) {
		this.isNewTranslation = newTranslation;
	}
	
	public boolean isPreCommitInformOther() {
		return this.preCommitInformOther;
	}
	
	public void setPreCommitInformOther(boolean preCommitInformOther) {
		this.preCommitInformOther = preCommitInformOther;
	}
	
	public boolean isPreCommitCodeReview() {
		return this.preCommitCodeReview;
	}
	
	public void setPreCommitCodeReview(boolean preCommitCodeReview) {
		this.preCommitCodeReview = preCommitCodeReview;
	}
}
