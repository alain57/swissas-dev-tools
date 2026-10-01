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
		assertThat(StringUtils.getInstance().isLetterCode("azer")).isFalse();
	}
	
	@Test
	void isLetterCodeDoesNotAcceptLessThanThreeOrMoreThanFour() {
		assertThat(StringUtils.getInstance().isLetterCode("A")).isFalse();
		assertThat(StringUtils.getInstance().isLetterCode("AZERT")).isFalse();
	}
	
	@Test
	void isLetterCodeAcceptEmptyAndRefuseNull() {
		assertThat(StringUtils.getInstance().isLetterCode("")).isTrue();
		assertThatIllegalArgumentException().isThrownBy(() -> StringUtils.getInstance().isLetterCode(null));
	}
	
	@Test
	void isLetterCodeAcceptTwoThreeOrFourUppercase() {
		assertThat(StringUtils.getInstance().isLetterCode("AZ")).isTrue();
		assertThat(StringUtils.getInstance().isLetterCode("AZE")).isTrue();
		assertThat(StringUtils.getInstance().isLetterCode("AZER")).isTrue();
	}
	
	@Test
	void isLetterCodeWithNameRefuseLowerCaseLC() {
		assertThat(StringUtils.getInstance().isLetterCodeWithName("azer (bla)")).isFalse();
	}
	
	@Test
	void isLetterCodeWithNameAcceptEmptyAndRefuseNull() {
		assertThat(StringUtils.getInstance().isLetterCodeWithName("")).isTrue();
		assertThatIllegalArgumentException().isThrownBy(() -> StringUtils.getInstance().isLetterCodeWithName(null));
	}
	
	@Test
	void isLetterCodeWithNameTooLongOrTooShort() {
		assertThat(StringUtils.getInstance().isLetterCodeWithName("A (bla)")).isFalse();
		assertThat(StringUtils.getInstance().isLetterCodeWithName("AZERT (bla)")).isFalse();
	}
	
	@Test
	void isLetterCodeWithNameOk() {
		assertThat(StringUtils.getInstance().isLetterCodeWithName("AZ (bla)")).isTrue();
		assertThat(StringUtils.getInstance().isLetterCodeWithName("AZE (bla)")).isTrue();
		assertThat(StringUtils.getInstance().isLetterCodeWithName("AZER (é'r)")).isTrue();
	}
	
	@Test
	void isPositiveNumber() {
		assertThat(StringUtils.getInstance().isPositiveNumber("500")).isTrue();
	}
	
	@Test
	void isNotPositiveNumber() {
		assertThat(StringUtils.getInstance().isPositiveNumber("")).isFalse();
		assertThatIllegalArgumentException().isThrownBy(() -> StringUtils.getInstance().isPositiveNumber(null));
		assertThat(StringUtils.getInstance().isPositiveNumber("-1")).isFalse();
		assertThat(StringUtils.getInstance().isPositiveNumber("1.6")).isFalse();
	}
	
	@Test
	void isValidGetter() {
		assertThat(StringUtils.getInstance().isGetter("isGetter")).isTrue();
		assertThat(StringUtils.getInstance().isGetter("hasMethod")).isTrue();
		assertThat(StringUtils.getInstance().isGetter("getValue")).isTrue();
		assertThat(StringUtils.getInstance().isGetter("areValuesGood")).isTrue();
	}
	
	@Test
	void isInvalidGetter() {
		assertThat(StringUtils.getInstance().isGetter("isntGetter")).isFalse();
		assertThat(StringUtils.getInstance().isGetter("hashCode")).isFalse();
		
	}
	
	@Test
	void removeJavaEndingOnlyRemovesTheJavaExtension() {
		assertThat(StringUtils.getInstance().removeJavaEnding("MyBO.java")).isEqualTo("MyBO");
		assertThat(StringUtils.getInstance().removeJavaEnding("MyBO")).isEqualTo("MyBO");
	}
	
	@Test
	void removeJavaEndingDoesNotFailOnOtherExtensions() {
		assertThat(StringUtils.getInstance().removeJavaEnding("Standard.properties")).isEqualTo("Standard.properties");
		assertThat(StringUtils.getInstance().removeJavaEnding("My.java.bak")).isEqualTo("My.java.bak");
	}
	
	@Test
	void removeGetterPrefixLowersTheFirstLetterByDefault() {
		assertThat(StringUtils.getInstance().removeGetterPrefix("getValue")).isEqualTo("value");
		assertThat(StringUtils.getInstance().removeGetterPrefix("isActive")).isEqualTo("active");
		assertThat(StringUtils.getInstance().removeGetterPrefix("hasChildren")).isEqualTo("children");
		assertThat(StringUtils.getInstance().removeGetterPrefix("areValuesGood")).isEqualTo("valuesGood");
	}
	
	@Test
	void removeGetterPrefixCanKeepTheFirstLetter() {
		assertThat(StringUtils.getInstance().removeGetterPrefix("getValue", false)).isEqualTo("Value");
	}
	
	@Test
	void removeGetterPrefixReturnsNullWhenItIsNotAGetter() {
		assertThat(StringUtils.getInstance().removeGetterPrefix("hashCode")).isNull();
	}
	
	@Test
	void addSetOfGetterGeneratesTheDtoSetter() {
		StringBuilder sb = new StringBuilder();
		
		StringUtils.getInstance().addSetOfGetter(sb, "getName", null, false);
		
		assertThat(sb).hasToString("\tdto.setName(bo.getName());\n");
	}
	
	@Test
	void addSetOfGetterGeneratesTheBoSetter() {
		StringBuilder sb = new StringBuilder();
		
		StringUtils.getInstance().addSetOfGetter(sb, "getName", "getId", true);
		
		assertThat(sb).hasToString("\tbo.setName(dto.getName());\n");
	}
	
	@Test
	void addSetOfGetterProtectsThePrimaryKeyAgainstNull() {
		StringBuilder sb = new StringBuilder();
		
		StringUtils.getInstance().addSetOfGetter(sb, "getId", "getId", false);
		
		assertThat(sb.toString()).startsWith("\tif(bo.getId() != null ) {\n")
		                         .contains("dto.setId(bo.getId());")
		                         .endsWith("\t}\n");
	}
	
	@Test
	void addSetOfGetterRefusesAnythingThatIsNotAGetter() {
		assertThatIllegalArgumentException().isThrownBy(
				() -> StringUtils.getInstance().addSetOfGetter(new StringBuilder(), "hashCode", null, false));
	}
	
}