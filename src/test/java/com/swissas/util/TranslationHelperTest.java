package com.swissas.util;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationHelperTest {
	
	@Test
	void keyIsUpperCaseWithUnderscores() {
		assertThat(TranslationHelper.convertPropertyStringToKey("Hello world")).isEqualTo("HELLO_WORLD");
	}
	
	@Test
	void keyDoesNotContainPunctuation() {
		assertThat(TranslationHelper.convertPropertyStringToKey("Hello, world!")).isEqualTo("HELLO_WORLD");
	}
	
	@Test
	void keyDoesNotStartOrEndWithAPlaceholder() {
		assertThat(TranslationHelper.convertPropertyStringToKey("%s items")).isEqualTo("ITEMS");
		assertThat(TranslationHelper.convertPropertyStringToKey("items %s")).isEqualTo("ITEMS");
	}
	
	@Test
	void keyIsLimitedTo36Characters() {
		String key = TranslationHelper.convertPropertyStringToKey(
				"This is a really long sentence that should be cut at some point");
		
		assertThat(key).hasSize(36).startsWith("THIS_IS_A_REALLY_LONG_SENTENCE");
	}
	
	@Test
	void fullKeyContainsTheEnding() {
		assertThat(TranslationHelper.generateFullKey(Set.of(), "Hello", "_TXT")).isEqualTo("HELLO_TXT");
		assertThat(TranslationHelper.generateFullKey(Set.of(), "Hello", "_TT")).isEqualTo("HELLO_TT");
	}
	
	@Test
	void fullKeyIsNumberedWhenItAlreadyExists() {
		Set<String> existing = Set.of("HELLO_TXT", "HELLO_1_TXT");
		
		assertThat(TranslationHelper.generateFullKey(existing, "Hello", "_TXT")).isEqualTo("HELLO_2_TXT");
	}
	
	@Test
	void commonMistakesAreCorrected() {
		assertThat(TranslationHelper.autoCorrectCommonMistakes("Open the work order")).isEqualTo("Open the @WORKORDER@");
		assertThat(TranslationHelper.autoCorrectCommonMistakes("Work-Order")).isEqualTo("@WORKORDER@");
		assertThat(TranslationHelper.autoCorrectCommonMistakes("Part number")).isEqualTo("@PART_NUMBER@");
		assertThat(TranslationHelper.autoCorrectCommonMistakes("PN")).isEqualTo("P/N");
		assertThat(TranslationHelper.autoCorrectCommonMistakes("Serial Number")).isEqualTo("@SERIAL_NUMBER@");
		assertThat(TranslationHelper.autoCorrectCommonMistakes("Amos")).isEqualTo("@AMOS@");
		assertThat(TranslationHelper.autoCorrectCommonMistakes("Analyze the Center")).isEqualTo("Analyse the Centre");
	}
	
	@Test
	void commonMistakesKeepNull() {
		assertThat(TranslationHelper.autoCorrectCommonMistakes(null)).isNull();
	}
	
	@Test
	void knownWordsAreReplacedByTheirKey() {
		Map<String, String> shared = Map.of("AIRCRAFT", "Aircraft");
		
		assertThat(TranslationHelper.replaceWithKnownKeys("Select an Aircraft", shared)).isEqualTo("Select an @AIRCRAFT@");
	}
	
	@Test
	void sharedValuesAreNotRegularExpressions() {
		Map<String, String> shared = Map.of("PN", "P.N");
		
		assertThat(TranslationHelper.replaceWithKnownKeys("Enter PxN here", shared)).isEqualTo("Enter PxN here");
		assertThat(TranslationHelper.replaceWithKnownKeys("Enter P.N here", shared)).isEqualTo("Enter @PN@ here");
	}
	
	@Test
	void sharedValuesWithRegexSpecialCharactersDoNotFail() {
		Map<String, String> shared = Map.of("KEY", "(unbalanced");
		
		assertThat(TranslationHelper.replaceWithKnownKeys("a (unbalanced b", shared)).isNotNull();
	}
	
	@Test
	void keysWithReplacementSpecialCharactersDoNotFail() {
		Map<String, String> shared = Map.of("A$1", "word");
		
		assertThat(TranslationHelper.replaceWithKnownKeys("a word", shared)).isEqualTo("a @A$1@");
	}
	
	@Test
	void emptySharedValuesAreIgnored() {
		Map<String, String> shared = Map.of("EMPTY", "");
		
		assertThat(TranslationHelper.replaceWithKnownKeys("untouched", shared)).isEqualTo("untouched");
	}
}
