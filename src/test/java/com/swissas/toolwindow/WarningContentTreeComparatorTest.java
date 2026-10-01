package com.swissas.toolwindow;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarningContentTreeComparatorTest {
	
	@Test
	void leadingSymbolsAreIgnored() {
		assertThat(WarningContentTreeComparator.getDisplayTextToSort("  - Hello")).isEqualTo("Hello");
		assertThat(WarningContentTreeComparator.getDisplayTextToSort("Hello")).isEqualTo("Hello");
		assertThat(WarningContentTreeComparator.getDisplayTextToSort("1st")).isEqualTo("1st");
	}
	
	@Test
	void emptyOrSymbolOnlyTextsDoNotFail() {
		assertThat(WarningContentTreeComparator.getDisplayTextToSort("")).isEmpty();
		assertThat(WarningContentTreeComparator.getDisplayTextToSort("!!!")).isEmpty();
	}
	
	@Test
	void nodesAreSortedNaturally() {
		List<String> sorted = Stream.of("file10", "file2", "File1")
		                            .map(WarningContentTreeNode::new)
		                            .sorted(WarningContentTreeComparator.INSTANCE)
		                            .map(node -> node.getUserObject().toString())
		                            .toList();
		
		assertThat(sorted).containsExactly("File1", "file2", "file10");
	}
}
