package com.swissas.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests of the project util for the projects that are not stored in git (the default branch is in the path).
 */
class ProjectUtilPathTest {
	
	private static ProjectUtil projectWithRoot(String rootPath) {
		VirtualFile root = mock(VirtualFile.class);
		when(root.getPath()).thenReturn(rootPath);
		ProjectUtil projectUtil = new ProjectUtil(null) {
			@Override
			protected VirtualFile[] getContentRoots(Module module) {
				return new VirtualFile[]{root};
			}
		};
		projectUtil.shared = mock(Module.class);
		projectUtil.shouldSearchDefaultBranch = true;
		return projectUtil;
	}
	
	@Test
	void defaultBranchIsReadFromThePathOfTheSharedModule() {
		ProjectUtil projectUtil = projectWithRoot("x:/svn/19.6/amos_shared/");
		
		assertThat(projectUtil.getProjectDefaultBranch()).isEqualTo("19.6");
		assertThat(projectUtil.getBranchOfFile(null)).isEqualTo("19.6");
		assertThat(projectUtil.isPreviewProject()).isFalse();
	}
	
	@Test
	void defaultBranchIsSearchedOnlyOnce() {
		ProjectUtil projectUtil = projectWithRoot("x:/svn/19.6/amos_shared/");
		projectUtil.getProjectDefaultBranch();
		projectUtil.projectDefaultBranch = "20.1";
		
		assertThat(projectUtil.getProjectDefaultBranch()).isEqualTo("20.1");
	}
	
	@Test
	void trunkIsThePreviewBranch() {
		ProjectUtil projectUtil = projectWithRoot("x:/svn/trunk/amos_shared/");
		
		assertThat(projectUtil.getBranchOfFile(null)).isEqualTo("preview");
		assertThat(projectUtil.isPreviewProject()).isTrue();
	}
	
	@Test
	void unknownPathFallsBackOnThePreviewBranch() {
		ProjectUtil projectUtil = projectWithRoot("x:/somewhere/else/");
		
		assertThat(projectUtil.getProjectDefaultBranch()).isNull();
		assertThat(projectUtil.getBranchOfFile(null)).isEqualTo("preview");
	}
	
	@Test
	void gitProjectIsDetectedFromThePathOfTheSharedModule() {
		assertThat(projectWithRoot("c:/git/amos/amos_shared/").isGitProject()).isTrue();
		assertThat(projectWithRoot("c:/svn/19.6/amos_shared/").isGitProject()).isFalse();
	}
	
	@Test
	void projectWithoutSharedModuleIsNotAGitProject() {
		ProjectUtil projectUtil = new ProjectUtil(null);
		
		assertThat(projectUtil.isGitProject()).isFalse();
		assertThat(projectUtil.getShared()).isNull();
	}
	
	@Test
	void projectWithoutContentRootIsNotAGitProject() {
		ProjectUtil projectUtil = new ProjectUtil(null) {
			@Override
			protected VirtualFile[] getContentRoots(Module module) {
				return new VirtualFile[0];
			}
		};
		projectUtil.shared = mock(Module.class);
		
		assertThat(projectUtil.isGitProject()).isFalse();
	}
	
	@Test
	void missingProjectIsNotAnAmosProject() {
		assertThat(new ProjectUtil(null).isAmosProject()).isFalse();
	}
	
	@Test
	void missingPropertiesFileFallsBackOnPreview() {
		VirtualFile root = mock(VirtualFile.class);
		when(root.getPath()).thenReturn("c:/git/amos/amos_shared/");
		ProjectUtil projectUtil = new ProjectUtil(null) {
			@Override
			protected VirtualFile[] getContentRoots(Module module) {
				return new VirtualFile[]{root};
			}
		};
		projectUtil.shared = mock(Module.class);
		projectUtil.shouldSearchDefaultBranch = true;
		
		assertThat(projectUtil.getProjectDefaultBranch()).isNull();
		assertThat(projectUtil.getBranchOfFile(null)).isEqualTo("preview");
	}
	
	@Test
	void branchNamesAreNormalized() {
		ProjectUtil projectUtil = new ProjectUtil(null);
		
		assertThat(projectUtil.convertToCorrectBranch("v19_6")).isEqualTo("19.6");
		assertThat(projectUtil.convertToCorrectBranch("20 1")).isEqualTo("20.1");
		assertThat(projectUtil.convertToCorrectBranch("V12-10")).isEqualTo("V12-10");
		assertThat(projectUtil.convertToCorrectBranch("  ")).isEqualTo("preview");
		assertThat(projectUtil.convertToCorrectBranch("feature/my-branch")).isEqualTo("preview");
	}
}
