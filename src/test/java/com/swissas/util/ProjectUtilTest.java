package com.swissas.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectUtilTest {

	private ProjectUtil projectUtil;
	private VirtualFile root;

    @BeforeEach
	void setUp() throws IOException {

        this.root = mock(VirtualFile.class);
        Module shared = mock(Module.class);

		when(this.root.getPath())
				.thenReturn("x:/somestuff/19.6/amos_shared/");

		VirtualFile props = mock(VirtualFile.class);
		when(this.root.findChild("amos.properties")).thenReturn(props);

		when(props.getInputStream()).thenReturn(
				new ByteArrayInputStream("target.branch=19.6".getBytes())
		);

        this.projectUtil = new ProjectUtil() {

			@Override
			protected VirtualFile[] getContentRoots(Module module) {
				return new VirtualFile[]{ProjectUtilTest.this.root};
			}

			@Override
			public boolean isGitProject() {
				return true; 
			}
		};

        this.projectUtil.shared = shared;
        this.projectUtil.shouldSearchDefaultBranch = true;
	}

	@Test
	void testConvertBranchShouldNotConvertName() {
		assertThat(this.projectUtil.convertToCorrectBranch("19.6"))
				.isEqualTo("19.6");
	}

	@Test
	void testConvertBranchShouldConvertNameToPreview() {
		assertThat(this.projectUtil.convertToCorrectBranch("trunk"))
				.isEqualTo("preview");
	}

	@Test
	void testConvertBranchShouldConvertNameWithNoSeparator() {
		assertThat(this.projectUtil.convertToCorrectBranch("196"))
				.isEqualTo("19.6");
	}

	@Test
	void testConvertBranchShouldConvertNameWithWrongSeparator() {
		assertThat(this.projectUtil.convertToCorrectBranch("19-6"))
				.isEqualTo("19.6");
	}

	@Test
	void testConvertBranchShouldConvertNameWithWrongPrefix() {
		assertThat(this.projectUtil.convertToCorrectBranch("v19.6"))
				.isEqualTo("19.6");
	}

	@Test
	void testConvertBranchShouldConvertNameMissingPrefix() {
		assertThat(this.projectUtil.convertToCorrectBranch("1220"))
				.isEqualTo("V12-20");
	}

	@Test
	void testConvertBranchNoNPEOnNull() {
		assertThat(this.projectUtil.convertToCorrectBranch(null))
				.isEqualTo("preview");
	}

	@Test
	void testGetProjectDefaultBranchShouldReturnNull() {
        this.projectUtil.shouldSearchDefaultBranch = false;

		assertThat(this.projectUtil.getProjectDefaultBranch())
				.isNull();
	}

	@Test
	void testGetProjectDefaultBranchShouldReturnPreviewWithoutSearching() {

        this.projectUtil.shouldSearchDefaultBranch = false;
        this.projectUtil.projectDefaultBranch = "preview";

		assertThat(this.projectUtil.getProjectDefaultBranch())
				.isEqualTo("preview");
	}

	@Test
	void testGetProjectDefaultBranchShouldFindBranch() {

		assertThat(this.projectUtil.getProjectDefaultBranch())
				.isEqualTo("19.6");
	}

	@Test
	void testGetBranchOfMajorAndMinorVersionShouldReturnNewStyle() {
		assertThat(this.projectUtil.getBranchOfMajorAndMinorVersion(19, 6))
				.isEqualTo("19.6");
	}

	@Test
	void testGetBranchOfMajorAndMinorVersionShouldReturnOldStyle() {
		assertThat(this.projectUtil.getBranchOfMajorAndMinorVersion(12, 10))
				.isEqualTo("V12-10");
	}
}