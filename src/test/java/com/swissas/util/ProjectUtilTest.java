package com.swissas.util;

import com.intellij.openapi.module.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ProjectUtilTest {
	@Mock
	Module shared;
	@InjectMocks
	ProjectUtil projectUtil;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	
	@Test
	void testConvertBranchShouldNotConvertName() {
		String branch = "19.6";
		String result = this.projectUtil.convertToCorrectBranch(branch);
		assertThat(result).isEqualTo(branch);
	}

	@Test
	void testConvertBranchShouldConvertNameToPreview() {
		String branch = "trunk";
		String result = this.projectUtil.convertToCorrectBranch(branch);
		assertThat(result).isNotEqualTo(branch);
		assertThat(result).isEqualTo("preview");
	}
	
	@Test
	void testConvertBranchShouldConvertNameWithNoSeparator() {
		String branch = "196";
		String result = this.projectUtil.convertToCorrectBranch(branch);
		assertThat(result).isNotEqualTo(branch);
		assertThat(result).isEqualTo("19.6");
	}
	
	@Test
	void testConvertBranchShouldConvertNameWithWrongSeparator() {
		String branch = "19-6";
		String result = this.projectUtil.convertToCorrectBranch(branch);
		assertThat(result).isNotEqualTo(branch);
		assertThat(result).isEqualTo("19.6");
	}
	
	@Test
	void testConvertBranchShouldConvertNameWithWrongPrefix() {
		String branch = "v19.6";
		String result = this.projectUtil.convertToCorrectBranch(branch);
		assertThat(result).isNotEqualTo(branch);
		assertThat(result).isEqualTo("19.6");
	}
	
	@Test
	void testConvertBranchShouldConvertNameMissingPrefix() {
		String branch = "1220";
		String result = this.projectUtil.convertToCorrectBranch(branch);
		assertThat(result).isNotEqualTo(branch);
		assertThat(result).isEqualTo("V12-20");
	}
	
	@Test
	void testConvertBranchNoNPEOnNull() {
		String result = this.projectUtil.convertToCorrectBranch(null);
		assertThat(result).isEqualTo("preview");
	}
	

	@Test
	void testGetProjectDefaultBranchShouldReturnNull() {
		this.projectUtil.shouldSearchDefaultBranch = false;
		String result = this.projectUtil.getProjectDefaultBranch();
		assertThat(result).isNull();
	}

	@Test
	void testGetProjectDefaultBranchShouldReturnPreviewWithoutSearching() {
		String preview = "preview";
		this.projectUtil.shouldSearchDefaultBranch = false;
		this.projectUtil.projectDefaultBranch = preview;
		String result = this.projectUtil.getProjectDefaultBranch();
		assertThat(result).isEqualTo(preview);
	}

	@Test
	void testGetProjectDefaultBranchShouldFindBranch(){
		String preview = "19.6";
		String pathWithPreview = "x:/somestuff/19.6/amos_shared/";
		this.projectUtil.shouldSearchDefaultBranch = true;
		when(this.shared.getModuleFilePath()).thenReturn(pathWithPreview);
		String result = this.projectUtil.getProjectDefaultBranch();
		assertThat(result).isEqualTo(preview);
	}
	

	@Test
	void testGetBranchOfMajorAndMinorVersionShouldReturnNewStyle(){
		int majorVersion = 19;
		int minorVersion = 6;
		String result = this.projectUtil.getBranchOfMajorAndMinorVersion(majorVersion
		                                                                , minorVersion);
		assertThat(result).isEqualTo("19.6");
	}
	
	@Test
	void testGetBranchOfMajorAndMinorVersionShouldReturnOldStyle(){
		int majorVersion = 12;
		int minorVersion = 10;
		String result = this.projectUtil.getBranchOfMajorAndMinorVersion(majorVersion
				, minorVersion);
		assertThat(result).isEqualTo("V12-10");
	}
}
