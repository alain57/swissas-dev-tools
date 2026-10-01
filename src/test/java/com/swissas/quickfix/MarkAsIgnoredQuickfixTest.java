package com.swissas.quickfix;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkAsIgnoredQuickfixTest {
	
	@Test
	void noExtFixIsNamedNoExt() {
		assertThat(new MarkAsIgnoredQuickfix("/*NO_EXT*/").getName()).isEqualTo("Mark as NO-EXT");
	}
	
	@Test
	void noSqlFixIsNamedNoSql() {
		assertThat(new MarkAsIgnoredQuickfix("/*NOSQL*/").getName()).isEqualTo("Mark as NOSQL");
	}
}
