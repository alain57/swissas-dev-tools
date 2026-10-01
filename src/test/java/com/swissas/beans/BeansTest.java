package com.swissas.beans;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;

import com.swissas.TestApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeansTest {
	
	private static Element xml(String xml) {
		Document document = Jsoup.parse(xml, "", Parser.xmlParser());
		return document.child(0);
	}
	
	@Test
	void messageReadsItsAttributes() {
		Message message = new Message(xml("<message severity=\"critical\" line=\"12\" description=\"Do it\" priority=\"Warning\"/>"));
		
		assertThat(message.getLine()).isEqualTo(12);
		assertThat(message.getDescription()).isEqualTo("Do it");
		assertThat(message.getText()).isEqualTo("Do it:12");
		assertThat(message.isCritical()).isTrue();
		assertThat(message.isWarning()).isTrue();
	}
	
	@Test
	void messageWithoutValidLineDoesNotThrow() {
		Message message = new Message(xml("<message severity=\"minor\" line=\"\" description=\"Do it\" priority=\"Other\"/>"));
		
		assertThat(message.getLine()).isZero();
		assertThat(message.isCritical()).isFalse();
		assertThat(message.isWarning()).isFalse();
	}
	
	@Test
	void messagesAreSortedBySeverityThenLineThenDescription() {
		Message critical = new Message(xml("<message severity=\"critical\" line=\"20\" description=\"a\" priority=\"p\"/>"));
		Message minorEarly = new Message(xml("<message severity=\"minor\" line=\"1\" description=\"a\" priority=\"p\"/>"));
		Message minorLate = new Message(xml("<message severity=\"minor\" line=\"5\" description=\"a\" priority=\"p\"/>"));
		
		assertThat(critical.compareTo(minorEarly)).isNegative();
		assertThat(minorEarly.compareTo(minorLate)).isNegative();
	}
	
	@Test
	void sameMessagesAreEquals() {
		Message one = new Message(xml("<message severity=\"minor\" line=\"5\" description=\"a\" priority=\"p\"/>"));
		Message two = new Message(xml("<message severity=\"minor\" line=\"5\" description=\"a\" priority=\"p\"/>"));
		Message other = new Message(xml("<message severity=\"minor\" line=\"6\" description=\"a\" priority=\"p\"/>"));
		
		assertThat(one).isEqualTo(two).hasSameHashCodeAs(two).isNotEqualTo(other);
	}
	
	@Test
	void fileIgnoresTheWhiteSpacesBetweenTheMessages() {
		Element fileElement = xml("<file path=\"a/b/My.java\" responsible=\"ABC\">\n"
		                          + "  <message severity=\"minor\" line=\"1\" description=\"first\" priority=\"p\"/>\n"
		                          + "  <message severity=\"critical\" line=\"2\" description=\"second\" priority=\"p\"/>\n"
		                          + "</file>");
		
		File file = new File("My.java", fileElement);
		
		assertThat(file.getPath()).isEqualTo("a/b/My.java");
		assertThat(file.getResponsible()).isEqualTo("ABC");
		assertThat(file.getChildren()).hasSize(2);
	}
	
	@Test
	void nonCriticalMessagesAreTheMessagesThatAreNotCritical() {
		Element fileElement = xml("<file path=\"a/My.java\" responsible=\"ABC\">"
		                          + "<message severity=\"minor\" line=\"1\" description=\"first\" priority=\"p\"/>"
		                          + "<message severity=\"critical\" line=\"2\" description=\"second\" priority=\"p\"/>"
		                          + "</file>");
		
		File file = new File("My.java", fileElement);
		
		assertThat(file.getNonCriticalMessages()).extracting(Message::getDescription).containsExactly("first");
	}
	
	@Test
	void fileIsMineIgnoringTheCase() {
		try (TestApplication application = TestApplication.install()) {
			application.getStorage().setFourLetterCode("abc");
			File file = new File("My.java", xml("<file path=\"a/My.java\" responsible=\"ABC\"/>"));
			File other = new File("Other.java", xml("<file path=\"a/Other.java\" responsible=\"XYZ\"/>"));
			
			assertThat(file.isMine()).isTrue();
			assertThat(other.isMine()).isFalse();
		}
	}
	
	@Test
	void directoriesAreSortedBeforeFiles() {
		Directory directory = new Directory("dir", "path");
		File file = new File("My.java", xml("<file path=\"a/My.java\" responsible=\"ABC\"/>"));
		
		assertThat(directory.compareTo(file)).isNegative();
		assertThat(file.compareTo(directory)).isPositive();
	}
	
	@Test
	void directoriesAreSortedByPathThenName() {
		Directory aInA = new Directory("a", "a");
		Directory aInB = new Directory("a", "b");
		Directory bInA = new Directory("b", "a");
		
		assertThat(aInA.compareTo(aInB)).isNegative();
		assertThat(aInA.compareTo(bInA)).isNegative();
		assertThat(aInA).isEqualTo(new Directory("a", "a")).hasSameHashCodeAs(new Directory("a", "a"));
		assertThat(aInA).isNotEqualTo(aInB);
	}
	
	@Test
	void childrenCanNotBeModifiedFromTheOutside() {
		Directory directory = new Directory("dir", "path");
		directory.addChildren(new Directory("child", "path/dir"));
		
		assertThat(directory.getChildren()).hasSize(1);
		assertThatThrownBy(() -> directory.getChildren().clear()).isInstanceOf(UnsupportedOperationException.class);
	}
	
	@Test
	void typeIsReadFromTheNameAttribute() {
		Type type = new Type(xml("<type name=\"Sonar\"/>"));
		
		assertThat(type.getMainAttribute()).isEqualTo("Sonar");
		assertThat(type.getText()).isEqualTo("Sonar");
	}
}
