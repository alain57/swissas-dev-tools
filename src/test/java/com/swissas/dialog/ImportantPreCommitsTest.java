package com.swissas.dialog;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImportantPreCommitsTest {
	
	@Test
	void reviewerIsReadFromTheCommitMessage() {
		Matcher matcher = ImportantPreCommits.REVIEWER.matcher("#1234 fix the thing reviewed by ABCD");
		
		assertThat(matcher.find()).isTrue();
		assertThat(matcher.group(1)).isEqualTo("ABCD");
	}
	
	@Test
	void reviewerWithTwoLettersIsAccepted() {
		Matcher matcher = ImportantPreCommits.REVIEWER.matcher("fix reviewed by AB");
		
		assertThat(matcher.find()).isTrue();
		assertThat(matcher.group(1)).isEqualTo("AB");
	}
	
	@Test
	void reviewerIsReadIgnoringTheCase() {
		Matcher matcher = ImportantPreCommits.REVIEWER.matcher("Reviewed By abc.");
		
		assertThat(matcher.find()).isTrue();
		assertThat(matcher.group(1)).isEqualTo("abc");
	}
	
	@Test
	void wordsLongerThanALetterCodeAreNotReviewers() {
		assertThat(ImportantPreCommits.REVIEWER.matcher("reviewed by somebody").find()).isFalse();
	}
	
	@Test
	void messageWithoutReviewerHasNoReviewer() {
		assertThat(ImportantPreCommits.REVIEWER.matcher("#1234 fix the thing NO REVIEW").find()).isFalse();
	}
}
