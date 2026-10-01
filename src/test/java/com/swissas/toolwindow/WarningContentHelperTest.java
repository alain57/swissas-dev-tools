package com.swissas.toolwindow;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.swissas.TestApplication;
import com.swissas.beans.Directory;
import com.swissas.beans.Type;
import com.swissas.beans.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarningContentHelperTest {
	
	private static final String SONAR_ONE = """
			<type name="Sonar">
			  <group>
			    <file path="amos/client/One.java" responsible="ABC">
			      <message severity="critical" line="10" description="Remove unused import" priority="Warning"/>
			      <message severity="minor" line="20" description="Complete the task associated to this TODO comment." priority="Warning"/>
			    </file>
			    <file path="amos/client/Two.java" responsible="XYZ">
			      <message severity="minor" line="3" description="Remove unused imports" priority="Warning"/>
			    </file>
			  </group>
			</type>""";
	
	private TestApplication application;
	
	private static Element xml(String xml) {
		return Jsoup.parse(xml, "", Parser.xmlParser()).child(0);
	}
	
	private static Type readType(String... typeXmls) {
		Set<Type> types = new TreeSet<>();
		Map<String, Directory> directories = new TreeMap<>();
		for (String typeXml : typeXmls) {
			WarningContentHelper.generateTypeFromElementTypeAndAddItToTypeSet(xml(typeXml), types, directories);
		}
		assertThat(types).hasSize(1);
		return types.iterator().next();
	}
	
	private static WarningContentTreeNode buildTree(Type type, String filter, boolean onlyCritical, String similar) {
		WarningContentTreeNode root = new WarningContentTreeNode("Root");
		type.getChildren().forEach(child -> WarningContentHelper.fillTreeWithChildren(root, child, filter, onlyCritical, similar));
		return root;
	}
	
	private static String dump(WarningContentTreeNode node) {
		StringBuilder sb = new StringBuilder();
		Enumeration<?> e = node.preorderEnumeration();
		while (e.hasMoreElements()) {
			WarningContentTreeNode current = (WarningContentTreeNode) e.nextElement();
			if (current != node) {
				sb.repeat("  ", current.getLevel() - 1).append(current.getUserObject()).append('\n');
			}
		}
		return sb.toString();
	}
	
	@BeforeEach
	void setUp() {
		this.application = TestApplication.install();
		this.application.getStorage().setFourLetterCode("ABC");
	}
	
	@AfterEach
	void tearDown() {
		this.application.close();
	}
	
	@Test
	void typeIsReadWithItsDirectoriesAndFiles() {
		Type type = readType(SONAR_ONE);
		
		assertThat(type.getMainAttribute()).isEqualTo("Sonar");
		assertThat(type.getChildren()).hasSize(1);
		Directory amos = (Directory) type.getChildren().iterator().next();
		assertThat(amos.getMainAttribute()).isEqualTo("amos");
	}
	
	@Test
	void sameTypeReadSeveralTimesIsMerged() {
		Type type = readType(SONAR_ONE,
		                     """
				                     <type name="Sonar"><group>
				                       <file path="other/Three.java" responsible="DEF">
				                         <message severity="minor" line="1" description="x" priority="Warning"/>
				                       </file>
				                     </group></type>""");
		
		assertThat(type.getChildren()).extracting(c -> c.getMainAttribute()).containsExactlyInAnyOrder("amos", "other");
	}
	
	@Test
	void fileWithoutFolderDoesNotFail() {
		Type type = readType("""
				                     <type name="Sonar"><group>
				                       <file path="Top.java" responsible="DEF">
				                         <message severity="minor" line="1" description="x" priority="Warning"/>
				                       </file>
				                     </group></type>""");
		
		assertThat(type.getChildren()).hasSize(1);
	}
	
	@Test
	void directoriesWithASingleSubDirectoryAreDisplayedOnOneLine() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), null, false, null);
		
		assertThat(root.getChildCount()).isEqualTo(1);
		assertThat(root.getChildAt(0).toString()).isEqualTo("amos/client");
	}
	
	@Test
	void treeContainsDirectoryThenFilesThenMessages() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), null, false, null);
		
		assertThat(dump(root)).isEqualTo("""
				amos/client
				  One.java
				    Remove unused import:10
				    Complete the task associated to this TODO comment.:20
				  Two.java
				    Remove unused imports:3
				""");
	}
	
	@Test
	void nodesHaveTheCorrectType() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), null, false, null);
		
		WarningContentTreeNode directory = (WarningContentTreeNode) root.getChildAt(0);
		WarningContentTreeNode file = (WarningContentTreeNode) directory.getChildAt(0);
		WarningContentTreeNode message = (WarningContentTreeNode) file.getChildAt(0);
		assertThat(directory.getTreeType()).isEqualTo(WarningContentTreeNode.TreeType.DIRECTORY);
		assertThat(file.getTreeType()).isEqualTo(WarningContentTreeNode.TreeType.FILE);
		assertThat(message.getTreeType()).isEqualTo(WarningContentTreeNode.TreeType.MESSAGE);
	}
	
	@Test
	void criticalMessagesAreFlaggedAsCritical() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), null, false, null);
		
		WarningContentTreeNode file = (WarningContentTreeNode) root.getChildAt(0).getChildAt(0);
		assertThat(((WarningContentTreeNode) file.getChildAt(0)).isCritical()).isTrue();
		assertThat(((WarningContentTreeNode) file.getChildAt(1)).isCritical()).isFalse();
	}
	
	@Test
	void onlyCriticalMessagesCanBeDisplayed() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), null, true, null);
		
		assertThat(dump(root)).isEqualTo("""
				amos/client
				  One.java
				    Remove unused import:10
				""");
	}
	
	@Test
	void filesOfOtherPeopleCanBeFilteredOut() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), "XYZ", false, null);
		
		assertThat(dump(root)).isEqualTo("""
				amos/client
				  Two.java
				    Remove unused imports:3
				""");
	}
	
	@Test
	void teamFilterDisplaysTheFilesOfEveryone() {
		this.application.getStorage().setUserMap(Map.of("ABC", new User("ABC", "DEV", "Alice A", "<html><body>Alice A<br/>")));
		this.application.getStorage().setFourLetterCode("ABC");
		
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), "T_DEV", false, null);
		
		assertThat(dump(root)).contains("One.java").contains("Two.java");
	}
	
	@Test
	void teamFilterKeepsTheSingleLineDirectory() {
		this.application.getStorage().setUserMap(Map.of("ABC", new User("ABC", "DEV", "Alice A", "<html><body>Alice A<br/>")));
		this.application.getStorage().setFourLetterCode("ABC");
		
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), "T_DEV", false, null);
		
		assertThat(root.getChildAt(0).toString()).isEqualTo("amos/client");
	}
	
	@Test
	void filterWithoutAnyFileDisplaysNothing() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), "NOBODY", false, null);
		
		assertThat(root.getChildCount()).isZero();
	}
	
	@Test
	void similarMessagesCanBeSearched() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), null, false, "Remove unused import");
		
		assertThat(dump(root)).contains("Remove unused import:10").contains("Remove unused imports:3")
		                      .doesNotContain("TODO");
	}
	
	@Test
	void todoMessagesCanBeSearched() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), null, false,
		                                        "Complete the task associated to this TODO comment.");
		
		assertThat(dump(root)).isEqualTo("""
				amos/client
				  One.java
				    Complete the task associated to this TODO comment.:20
				""");
	}
	
	@Test
	void myFilesAreMarkedAsMine() {
		WarningContentTreeNode root = buildTree(readType(SONAR_ONE), null, false, null);
		
		WarningContentTreeNode directory = (WarningContentTreeNode) root.getChildAt(0);
		assertThat(((WarningContentTreeNode) directory.getChildAt(0)).isMine()).isTrue();
		assertThat(((WarningContentTreeNode) directory.getChildAt(1)).isMine()).isFalse();
		assertThat(directory.isMine()).isTrue();
	}
}
