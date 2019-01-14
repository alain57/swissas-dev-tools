package com.swissas.util;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The storage class where the plugin settings are saved with getters and setters to access the values within the plugin
 *
 * @author Tavan Alain
 */
@State(name ="Swiss-AS", storages = {@com.intellij.openapi.components.Storage(file = "./swissas_settings.xml")})
public class Storage implements PersistentStateComponent<Storage> {
    
    
    private String fourLetterCode = "";
    private boolean horizontalOrientation;
    private boolean fixMissingOverride;
    private boolean fixMissingThis;
    private boolean fixUnusedSuppressWarning;
    private boolean fixMissingAuthor;
    private boolean showIgnoredValues;
    private List<String> ignoredValues;

    
    @Nullable
    @Override
    public Storage getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull Storage state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getFourLetterCode() {
        return this.fourLetterCode;
    }

    public void setFourLetterCode(String fourLetterCode) {
        this.fourLetterCode = fourLetterCode;
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

    public void setFixMissingOverride(boolean fixMissingOverride) {
        this.fixMissingOverride = fixMissingOverride;
    }

    public boolean isFixMissingThis() {
        return this.fixMissingThis;
    }

    public void setFixMissingThis(boolean fixMissingThis) {
        this.fixMissingThis = fixMissingThis;
    }

    public boolean isFixUnusedSuppressWarning() {
        return this.fixUnusedSuppressWarning;
    }

    public void setFixUnusedSuppressWarning(boolean fixUnusedSuppressWarning) {
        this.fixUnusedSuppressWarning = fixUnusedSuppressWarning;
    }
    
    public static Storage getStorageFromProject(Project project) {
        return ServiceManager.getService(project, Storage.class);
    }

    public boolean isFixMissingAuthor() {
        return this.fixMissingAuthor;
    }

    public void setFixMissingAuthor(boolean fixMissingAuthor) {
        this.fixMissingAuthor = fixMissingAuthor;
    }

    public boolean isShowIgnoredValues() {
        return this.showIgnoredValues;
    }

    public void setShowIgnoredValues(boolean showIgnoredValues) {
        this.showIgnoredValues = showIgnoredValues;
    }

    public List<String> getIgnoredValues() {
        return this.ignoredValues;
    }

    public void setIgnoredValues(List<String> ignoredValues) {
        this.ignoredValues = ignoredValues;
    }
}
