package com.swissas.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.swissas.beans.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The storage class where the plugin settings are saved with getters and setters to access the values within the plugin
 *
 * @author Tavan Alain
 */

@State(name = "SwissAsStorage", storages = @Storage("swissas_settings.xml"))
public class SwissAsStorage implements PersistentStateComponent<SwissAsStorage> {
    
    public static final String PASSWORD_FOR_MAIL = "passwordForMail";
    private String fourLetterCode = "";
    private String qaLetterCode = "";
    private boolean horizontalOrientation = true;
    private String minWarningSize = "5";
    private boolean fixMissingOverride = true;
    private boolean fixMissingThis = true;
    private boolean fixUnusedSuppressWarning = false;
    private boolean fixMissingAuthor = true;
    private boolean translationOnlyCheckChangedLine = false;
    private boolean preCommitInformQA = false;
    private boolean preCommitCodeReview = true;
    
    private boolean showIgnoredValues = false;
    private List<String> ignoredValues = new ArrayList<>();
    private Map<String, User> userMap = new HashMap<>();
    private Properties shareProperties; 
    private boolean isAmosProject = false;
    private boolean isNewTranslation = false;
    
    
    
    public static SwissAsStorage getInstance(){
        return ServiceManager.getService(SwissAsStorage.class);
        
    }
    
    @Nullable
    @Override
    public SwissAsStorage getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SwissAsStorage state)  {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getFourLetterCode() {
        return this.fourLetterCode;
    }
    
    public String getPassword() {
        CredentialAttributes credentialAttributes = createCredentialAttributes(PASSWORD_FOR_MAIL);
        return PasswordSafe.getInstance().getPassword(credentialAttributes);
    }
    
    public String getQaLetterCode() {
        return this.qaLetterCode;
    }
    
    public void setFourLetterCode(String fourLetterCode){
        this.fourLetterCode = fourLetterCode;
    }
    
    public void setPassword(String password) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(PASSWORD_FOR_MAIL); // see previous sample
        Credentials credentials = new Credentials(this.fourLetterCode, password);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }
    
    public void setQaLetterCode(String qaLetterCode) {
        this.qaLetterCode = qaLetterCode;
    }

    public boolean isHorizontalOrientation() {
        return this.horizontalOrientation;
    }
    
    public void setHorizontalOrientation(boolean horizontalOrientation){
        this.horizontalOrientation = horizontalOrientation;
    }

    public boolean isFixMissingOverride() {
        return this.fixMissingOverride;
    }

    public boolean isFixMissingThis() {
        return this.fixMissingThis;
    }
    
    public void setFixMissingThis(boolean fixMissingThis){
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


    public Map<String, User> getUserMap() {
        return this.userMap;
    }
    
    public void setUserMap(Map<String, User> userMap){
        this.userMap = userMap;
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
        this.ignoredValues = ignoredValues;
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

    public boolean isAmosProject() {
        return this.isAmosProject;
    }

    public void setAmosProject(boolean amosProject) {
        this.isAmosProject = amosProject;
    }

    public boolean isNewTranslation() {
        return this.isNewTranslation;
    }

    public void setNewTranslation(boolean newTranslation) {
        this.isNewTranslation = newTranslation;
    }
    
    public boolean isPreCommitInformQA() {
        return this.preCommitInformQA;
    }
    
    public void setPreCommitInformQA(boolean preCommitInformQA) {
        this.preCommitInformQA = preCommitInformQA;
    }
    
    public boolean isPreCommitCodeReview() {
        return this.preCommitCodeReview;
    }
    
    public void setPreCommitCodeReview(boolean preCommitCodeReview) {
        this.preCommitCodeReview = preCommitCodeReview;
    }
    
    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("Swiss-AS-Dev-Tools", key));
    }
}
