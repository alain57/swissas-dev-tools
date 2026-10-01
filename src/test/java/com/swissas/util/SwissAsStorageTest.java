package com.swissas.util;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.swissas.beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwissAsStorageTest {
	
	private SwissAsStorage storage;
	
	private static User user(String lc, String team, String name) {
		return new User(lc, team, name, "<html><body>" + name + "<br/>LC: " + lc + "<br/>Team: " + team + "</body></html>");
	}
	
	private static Map<String, User> users() {
		Map<String, User> users = new TreeMap<>();
		users.put("ABC", user("ABC", "DEV", "Alice A"));
		users.put("DEF", user("DEF", "DEV", "Bob B"));
		users.put("XYZ", user("XYZ", "QC", "Carol C"));
		return users;
	}
	
	@BeforeEach
	void setUp() {
		this.storage = new SwissAsStorage();
	}
	
	@Test
	void teamIsFoundFromTheLetterCode() {
		this.storage.setUserMap(users());
		this.storage.setFourLetterCode("ABC");
		
		assertThat(this.storage.hasTeam()).isTrue();
		assertThat(this.storage.getMyTeam()).isEqualTo("T_DEV");
	}
	
	@Test
	void teamIsFoundWhenTheUsersAreLoadedAfterTheLetterCode() {
		this.storage.setFourLetterCode("XYZ");
		this.storage.setUserMap(users());
		
		assertThat(this.storage.getMyTeam()).isEqualTo("T_QC");
	}
	
	@Test
	void unknownLetterCodeHasNoTeam() {
		this.storage.setUserMap(users());
		this.storage.setFourLetterCode("ZZZ");
		
		assertThat(this.storage.hasTeam()).isFalse();
		assertThat(this.storage.getMyTeamMembers()).isEmpty();
	}
	
	@Test
	void nullLetterCodeIsConsideredAsEmpty() {
		this.storage.setUserMap(users());
		this.storage.setFourLetterCode(null);
		
		assertThat(this.storage.getFourLetterCode()).isEmpty();
		assertThat(this.storage.hasTeam()).isFalse();
	}
	
	@Test
	void teamMembersContainTheTeamAccountButNotMyself() {
		this.storage.setUserMap(users());
		this.storage.setFourLetterCode("ABC");
		
		assertThat(this.storage.getMyTeamMembers()).containsExactly("DEF", "T_DEV");
	}
	
	@Test
	void teamMembersCanIncludeMyself() {
		this.storage.setUserMap(users());
		this.storage.setFourLetterCode("ABC");
		
		assertThat(this.storage.getMyTeamMembers(true)).containsExactly("ABC", "DEF", "T_DEV");
	}
	
	@Test
	void teamMembersForReviewHaveNoTeamAccountAndNoMyself() {
		this.storage.setUserMap(users());
		this.storage.setFourLetterCode("ABC");
		
		assertThat(this.storage.getMyTeamMembersForReview()).containsExactly("DEF");
	}
	
	@Test
	void myMailIsBuiltFromTheLetterCode() {
		this.storage.setFourLetterCode("ABC");
		
		assertThat(this.storage.getMyMail()).isEqualTo("ABC@swiss-as.com");
	}
	
	@Test
	void mailIsBuiltFromTheLetterCodeAndName() {
		assertThat(SwissAsStorage.toMail("ABC (Alice A)")).isEqualTo("ABC@swiss-as.com");
		assertThat(SwissAsStorage.toMail("ABC")).isEqualTo("ABC@swiss-as.com");
		assertThat(SwissAsStorage.toMail("  ABC (Alice A)  ")).isEqualTo("ABC@swiss-as.com");
	}
	
	@Test
	void noMailWithoutLetterCode() {
		assertThat(SwissAsStorage.toMail("")).isNull();
		assertThat(SwissAsStorage.toMail("   ")).isNull();
		assertThat(SwissAsStorage.toMail(null)).isNull();
	}
	
	@Test
	void qaDocuAndSupportMailsAreUpdatedWithTheirLetterCode() {
		this.storage.setQaLetterCode("QAA (Quality A)");
		this.storage.setDocuLetterCode("DOC (Docu D)");
		this.storage.setSupportLetterCode("SUP");
		
		assertThat(this.storage.getQaMail()).isEqualTo("QAA@swiss-as.com");
		assertThat(this.storage.getDocuMail()).isEqualTo("DOC@swiss-as.com");
		assertThat(this.storage.getSupportMail()).isEqualTo("SUP@swiss-as.com");
	}
	
	@Test
	void clearingALetterCodeClearsItsMail() {
		this.storage.setQaLetterCode("QAA (Quality A)");
		
		this.storage.setQaLetterCode("");
		
		assertThat(this.storage.getQaLetterCode()).isEmpty();
		assertThat(this.storage.getQaMail()).isNull();
	}
	
	@Test
	void letterCodeWithoutNameDoesNotFail() {
		this.storage.setSupportLetterCode("SUP");
		this.storage.setSupportLetterCode(null);
		
		assertThat(this.storage.getSupportLetterCode()).isEmpty();
		assertThat(this.storage.getSupportMail()).isNull();
	}
	
	@Test
	void fullNamesAreMappedToTheirLetterCode() {
		this.storage.setUserMap(users());
		
		assertThat(this.storage.getFullNameTo4LcMap()).containsEntry("Alice A", "ABC")
		                                              .containsEntry("Bob B", "DEF");
	}
	
	@Test
	void twoUsersWithTheSameFullNameDoNotFail() {
		Map<String, User> users = users();
		users.put("AAA", user("AAA", "DEV", "Alice A"));
		
		this.storage.setUserMap(users);
		
		assertThat(this.storage.getFullNameTo4LcMap()).containsKey("Alice A");
		assertThat(this.storage.getUserMap()).hasSize(4);
	}
	
	@Test
	void replacingTheUsersReplacesThePreviousOnes() {
		this.storage.setUserMap(users());
		Map<String, User> others = new TreeMap<>();
		others.put("NEW", user("NEW", "DEV", "New N"));
		
		this.storage.setUserMap(others);
		
		assertThat(this.storage.getUserMap()).containsOnlyKeys("NEW");
		assertThat(this.storage.getFullNameTo4LcMap()).containsOnlyKeys("New N");
	}
	
	@Test
	void userMapCanNotBeModifiedFromTheOutside() {
		this.storage.setUserMap(users());
		
		assertThatThrownBy(() -> this.storage.getUserMap().clear()).isInstanceOf(UnsupportedOperationException.class);
	}
	
	@Test
	void ignoredValuesAreCopied() {
		this.storage.setIgnoredValues(List.of("a", "b"));
		
		assertThat(this.storage.getIgnoredValues()).containsExactly("a", "b");
		assertThatThrownBy(() -> this.storage.getIgnoredValues().add("c")).isInstanceOf(UnsupportedOperationException.class);
	}
	
	@Test
	void defaultValues() {
		assertThat(this.storage.isHorizontalOrientation()).isTrue();
		assertThat(this.storage.getMinWarningSize()).isEqualTo("5");
		assertThat(this.storage.isFixMissingAuthor()).isTrue();
		assertThat(this.storage.isFixMissingOverride()).isTrue();
		assertThat(this.storage.isFixMissingThis()).isTrue();
		assertThat(this.storage.isFixUnusedSuppressWarning()).isFalse();
		assertThat(this.storage.isConvertToTeam()).isFalse();
		assertThat(this.storage.getSimilarValue()).isEqualTo(0.8d);
		assertThat(this.storage.getShareProperties()).isEmpty();
	}
}
