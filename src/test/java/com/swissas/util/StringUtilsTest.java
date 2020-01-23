package com.swissas.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author Tavan Alain
 */
class StringUtilsTest {
	
	@Test
	void isLetterCodeDoesNotAcceptLowerCase() {
		assertThat(StringUtils.getInstance().isLetterCode("azer")).isEqualTo(false);
	}
	
	@Test
	void isLetterCodeDoesNotAcceptLessThanThreeOrMoreThanFour() {
		assertThat(StringUtils.getInstance().isLetterCode("AZ")).isEqualTo(false);
		assertThat(StringUtils.getInstance().isLetterCode("AZERT")).isEqualTo(false);
	}
	
	@Test
	void isLetterCodeAcceptEmptyAndRefuseNull() {
		assertThat(StringUtils.getInstance().isLetterCode("")).isEqualTo(true);
		assertThatIllegalArgumentException().isThrownBy(() -> StringUtils.getInstance().isLetterCode(null));
	}
	
	@Test
	void isLetterCodeAcceptThreeOrFourUppercase() {
		assertThat(StringUtils.getInstance().isLetterCode("AZE")).isEqualTo(true);
		assertThat(StringUtils.getInstance().isLetterCode("AZER")).isEqualTo(true);
	}
	
	@Test
	void isLetterCodeWithNameRefuseLowerCaseLC() {
		assertThat(StringUtils.getInstance().isLetterCodeWithName("azer (bla)")).isEqualTo(false);
	}
	
	@Test
	void isLetterCodeWithNameAcceptEmptyAndRefuseNull() {
		assertThat(StringUtils.getInstance().isLetterCode("")).isEqualTo(true);
		assertThatIllegalArgumentException().isThrownBy(() -> StringUtils.getInstance().isLetterCode(null));
	}
	
	@Test
	void isLetterCodeWithNameTooLongOrTooShort() {
		assertThat(StringUtils.getInstance().isLetterCodeWithName("AZ (bla)")).isEqualTo(false);
		assertThat(StringUtils.getInstance().isLetterCodeWithName("AZERT (bla)")).isEqualTo(false);
	}
	
	@Test
	void isLetterCodeWithNameOk() {
		assertThat(StringUtils.getInstance().isLetterCodeWithName("AZE (bla)")).isEqualTo(true);
		assertThat(StringUtils.getInstance().isLetterCodeWithName("AZER (é'r)")).isEqualTo(true);
	}
	
	@Test
	void isPositiveNumber() {
		assertThat(StringUtils.getInstance().isPositiveNumber("500")).isEqualTo(true);
	}
	
	@Test
	void isNotPositiveNumber() {
		assertThat(StringUtils.getInstance().isPositiveNumber("")).isEqualTo(false);
		assertThatIllegalArgumentException().isThrownBy(() -> StringUtils.getInstance().isPositiveNumber(null));
		assertThat(StringUtils.getInstance().isPositiveNumber("-1")).isEqualTo(false);
		assertThat(StringUtils.getInstance().isPositiveNumber("1.6")).isEqualTo(false);
	}
	
	@Test
	void isValidGetter() {
		assertThat(StringUtils.getInstance().isGetter("isGetter")).isEqualTo(true);
		assertThat(StringUtils.getInstance().isGetter("hasMethod")).isEqualTo(true);
		assertThat(StringUtils.getInstance().isGetter("getValue")).isEqualTo(true);
		assertThat(StringUtils.getInstance().isGetter("areValuesGood")).isEqualTo(true);
	}
	
	@Test
	void isInvalidGetter() {
		assertThat(StringUtils.getInstance().isGetter("isntGetter")).isEqualTo(false);
		assertThat(StringUtils.getInstance().isGetter("hashCode")).isEqualTo(false);
		
	}
	
}