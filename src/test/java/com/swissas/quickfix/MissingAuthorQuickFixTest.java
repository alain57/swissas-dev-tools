package com.swissas.quickfix;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MissingAuthorQuickFixTest {
	
	@Test
	void authorIsAddedBeforeTheEndOfAMultiLinesComment() {
		String result = MissingAuthorQuickFix.addAuthorToDocComment("/**\n * Description\n */", "ABC");
		
		assertThat(result).isEqualTo("/**\n * Description\n * @author ABC\n */");
	}
	
	@Test
	void oneLineCommentIsConvertedToAMultiLinesComment() {
		String result = MissingAuthorQuickFix.addAuthorToDocComment("/** Description */", "ABC");
		
		assertThat(result).isEqualTo("/**\n * Description\n * @author ABC\n */");
	}
	
	@Test
	void emptyOneLineCommentOnlyContainsTheAuthor() {
		String result = MissingAuthorQuickFix.addAuthorToDocComment("/** */", "ABC");
		
		assertThat(result).isEqualTo("/**\n * @author ABC\n */");
	}
}
