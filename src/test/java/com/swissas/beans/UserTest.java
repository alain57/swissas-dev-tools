package com.swissas.beans;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {
	
	private static final String INFOS = "<html><body>John Doe<br/>LC: ABC<br/>Team: DEV</body></html>";
	
	@Test
	void letterCodeIsAlwaysUpperCase() {
		assertThat(new User("abc", "DEV", "John Doe", INFOS).getLc()).isEqualTo("ABC");
	}
	
	@Test
	void letterCodeAndNameAreReadFromTheInfos() {
		assertThat(new User("ABC", "DEV", "John Doe", INFOS).getLCAndName()).isEqualTo("ABC (John Doe)");
	}
	
	@Test
	void letterCodeAndNameFallBackOnTheFullNameWhenTheInfosAreUnexpected() {
		assertThat(new User("ABC", "DEV", "John Doe", "no line break").getLCAndName()).isEqualTo("ABC (John Doe)");
		assertThat(new User("ABC", "DEV", "John Doe", null).getLCAndName()).isEqualTo("ABC (John Doe)");
		assertThat(new User("ABC", "DEV", null, null).getLCAndName()).isEqualTo("ABC");
	}
	
	@Test
	void userIsInItsTeamOnly() {
		User user = new User("ABC", "DEV", "John Doe", INFOS);
		
		assertThat(user.isInTeam("DEV")).isTrue();
		assertThat(user.isInTeam("QC")).isFalse();
		assertThat(new User().isInTeam("DEV")).isFalse();
	}
	
	@Test
	void usersWithTheSameInformationAreEquals() {
		User user = new User("ABC", "DEV", "John Doe", INFOS);
		
		assertThat(user).isEqualTo(new User("abc", "DEV", "John Doe", INFOS))
		                .hasSameHashCodeAs(new User("abc", "DEV", "John Doe", INFOS))
		                .isNotEqualTo(new User("ABC", "QC", "John Doe", INFOS));
	}
}
