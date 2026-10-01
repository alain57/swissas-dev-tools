package com.swissas.provider;

import java.util.regex.Matcher;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OpenCaseAnnotationProviderTest {
	
	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"SC 414294 add logic to prevent hiding a case|414294",
			"SC414294 fix|414294",
			"SC-12345 fix|12345",
			"#12345 fix|12345",
			"SC no. 12345 fix|12345",
			"Case ID 12345 fix|12345",
			"SUP: 12345 fix|12345",
			"support 12345 fix|12345"
	})
	void testCaseNumberIsFullyExtracted(String message, String expected) {
		Matcher matcher = OpenCaseAnnotationProvider.SUPPORT_FINDER.matcher(message);
		assertThat(matcher.find()).isTrue();
		assertThat(matcher.groupCount()).isEqualTo(3);
		assertThat(matcher.group(3)).isEqualTo(expected);
	}
}
