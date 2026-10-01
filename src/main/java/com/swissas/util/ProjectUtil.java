package com.swissas.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.branch.BranchData;
import com.intellij.vcs.branch.BranchStateProvider;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Project utility class.
 * <p>
 * This is a project level service (registered in the plugin.xml, not as a light service as light services
 * must be final and this class is extended in the unit tests): each project owns its state, which avoids
 * sharing (and leaking) the module of one project with another one.
 *
 * @author Tavan Alain
 */
public class ProjectUtil {
	private static final Logger  LOGGER                        = Logger.getInstance("Swiss-as");
	private static final Pattern AMOS_SHARED_DIRECTORY_PATTERN = Pattern
			.compile("/([^/]+)/[^/]*amos_shared/", Pattern.CASE_INSENSITIVE);
	private static final Pattern STABLE_VERSION_PATTERN        = Pattern
			.compile("^v?(\\d{2})[_\\-. ]?(\\d{1,2})$", Pattern.CASE_INSENSITIVE);
	
	private static final String PREVIEW = "preview";
	
	private final Project project;
	
	protected Module  shared;
	protected String  projectDefaultBranch      = null;
	protected boolean isAmosProject             = false;
	protected boolean shouldSearchDefaultBranch = false;
	
	public ProjectUtil(@Nullable Project project) {
		this.project = project;
	}
	
	public static ProjectUtil getInstance(@NotNull Project project) {
		return project.getService(ProjectUtil.class);
	}
	
	public String getBranchOfFile(@Nullable VirtualFile file) {
		String branchName = getProjectDefaultBranch();
		if (file != null && this.project != null) {
			FilePath filePath = VcsUtil.getFilePath(file);
			AbstractVcs vcsFor = VcsUtil.getVcsFor(this.project, filePath);
			if (vcsFor != null) {
				branchName = BranchStateProvider.EP_NAME.getExtensionList(this.project)
				                                        .stream().filter(Objects::nonNull)
				                                        .filter(p -> p.getClass().getName()
				                                                      .contains(vcsFor.getName()))
				                                        .map(p -> p.getCurrentBranch(filePath))
				                                        .filter(Objects::nonNull)
				                                        .map(BranchData::getBranchName)
				                                        .filter(Objects::nonNull)
				                                        .map(String::toLowerCase).findFirst()
				                                        .orElse(null);
			}
		}
		return convertToCorrectBranch(branchName);
	}
	
	String convertToCorrectBranch(String branchName) {
		String result = PREVIEW;
		if(branchName == null || branchName.isBlank()) {
			return result;
		}
		Matcher matcher = STABLE_VERSION_PATTERN.matcher(branchName);
		if (matcher.find() && matcher.groupCount() == 2) {
			int majorVersion = Integer.parseInt(matcher.group(1));
			int minorVersion = Integer.parseInt(matcher.group(2));
			return getBranchOfMajorAndMinorVersion(majorVersion, minorVersion);
		}
		return result;
	}
	
	String getBranchOfMajorAndMinorVersion(int majorVersion, int minorVersion) {
		return majorVersion >= 19 ? majorVersion + "." + minorVersion
		                          : "V" + majorVersion + "-" + minorVersion;
	}
	
	/**
	 * @return true when the current project is an amos project.
	 * A positive answer is cached, a negative one is re-evaluated because the modules
	 * may not be loaded yet the first time this method is called.
	 */
	public boolean isAmosProject() {
		if (this.isAmosProject) {
			return true;
		}
		if (this.project == null || this.project.isDisposed()) {
			return false;
		}
		Optional<Module> amosShared = Stream.of(ModuleManager.getInstance(this.project).getModules())
		                                    .filter(e -> e.getName().contains("amos_shared"))
		                                    .findFirst();
		if (amosShared.isPresent()) {
			this.shared = amosShared.get();
			this.projectDefaultBranch = null;
			this.shouldSearchDefaultBranch = true;
			this.isAmosProject = true;
		}
		return this.isAmosProject;
	}
	
	public boolean isPreviewProject() {
		return PREVIEW.equalsIgnoreCase(convertToCorrectBranch(getProjectDefaultBranch()));
	}

	public boolean isGitProject() {
		return Optional.ofNullable(this.shared)
				.flatMap(this::getModuleRoot)
				.map(VirtualFile::getPath)
				.map(String::toLowerCase)
				.map(path -> path.contains("git"))
				.orElse(false);
	}
	
	String getProjectDefaultBranch() {
		if (this.shouldSearchDefaultBranch) {
			this.shouldSearchDefaultBranch = false;
			if(isGitProject()) {
				readDefaultBranchFromAmosProperties();
			}else {
				Matcher matcher =
						Optional.ofNullable(this.shared)
								.flatMap(this::getModuleRoot)
								.map(VirtualFile::getPath)
								.map(AMOS_SHARED_DIRECTORY_PATTERN::matcher)
								.orElse(null);

				if (matcher != null && matcher.find()) {
					this.projectDefaultBranch = matcher.group(1);
				}
			}
		}
		return this.projectDefaultBranch;
	}
	
	private void readDefaultBranchFromAmosProperties() {
		VirtualFile propertiesFile = Optional.ofNullable(this.shared)
		                                     .flatMap(this::getModuleRoot)
		                                     .map(root -> root.findChild("amos.properties"))
		                                     .orElse(null);
		if (propertiesFile == null) {
			LOGGER.info("Could not find amos.properties, falling back to the preview branch");
			return;
		}
		Properties prop = new Properties();
		try (InputStream inputStream = propertiesFile.getInputStream()) {
			prop.load(inputStream);
			this.projectDefaultBranch = prop.getProperty("target.branch");
		} catch (IOException e) {
			LOGGER.warn("Unable to read " + propertiesFile.getPath(), e);
		}
	}
	
	public Module getShared() {
		return this.shared;
	}

	protected VirtualFile[] getContentRoots(Module module) {
		return ModuleRootManager
				.getInstance(module)
				.getContentRoots();
	}

	private Optional<VirtualFile> getModuleRoot(Module module) {
		VirtualFile[] roots = getContentRoots(module);

		return roots.length > 0
				? Optional.of(roots[0])
				: Optional.empty();
	}
	
}
