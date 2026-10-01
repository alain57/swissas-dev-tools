package com.swissas.provider;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationDocumentationProviderTest {
	
	private static final Map<Object, Object> SHARED = Map.of("WORKORDER", "Work Order", "AMOS", "Amos");
	
	@Test
	void referencesAreReplacedByTheSharedValue() {
		assertThat(TranslationDocumentationProvider.replaceReferences("Open the @WORKORDER@", SHARED))
				.isEqualTo("Open the Work Order");
	}
	
	@Test
	void severalReferencesAreReplaced() {
		assertThat(TranslationDocumentationProvider.replaceReferences("@AMOS@ - @WORKORDER@ list", SHARED))
				.isEqualTo("Amos - Work Order list");
	}
	
	@Test
	void unknownReferencesAreReported() {
		assertThat(TranslationDocumentationProvider.replaceReferences("a @UNKNOWN@ b", SHARED))
				.isEqualTo("a <i>unknown key: UNKNOWN</i> b");
	}
	
	@Test
	void specialReferencesAreReportedAsNotImplemented() {
		assertThat(TranslationDocumentationProvider.replaceReferences("a @some.key@ b", SHARED))
				.contains("special case for key: some.key not implemented yet");
	}
	
	@Test
	void textWithoutReferenceIsKept() {
		assertThat(TranslationDocumentationProvider.replaceReferences("Nothing to see", SHARED)).isEqualTo("Nothing to see");
		assertThat(TranslationDocumentationProvider.replaceReferences("mail me @ home", SHARED)).isEqualTo("mail me @ home");
	}
	
	@Test
	void nullTextIsKept() {
		assertThat(TranslationDocumentationProvider.replaceReferences(null, SHARED)).isNull();
	}
	
	@Test
	void noPsiElementIsNotAMultiLang() {
		assertThat(TranslationDocumentationProvider.isSasMultiLang(null)).isFalse();
	}
}
