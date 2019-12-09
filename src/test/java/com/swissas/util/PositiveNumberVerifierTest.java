package com.swissas.util;


import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Tavan Alain
 */
public class PositiveNumberVerifierTest {
	
	private JTextField textField = new JTextField();
	private PositiveNumberVerifier verifier = new PositiveNumberVerifier();
	
	
	@Test
	public void testIntegerNumberShouldBePositive(){
		this.textField.setText("5");
		testShouldBe(true);
	}
	
	@Test
	public void testShouldNotBePositive(){
		this.textField.setText("-1");
		testShouldBe(false);
	}
	
	@Test
	public void testNotANumberShouldNotBePositive(){
		this.textField.setText("haha");
		testShouldBe(false);
	}
	
	
	private void testShouldBe(boolean result){
		boolean verify = this.verifier.verify(this.textField);
		assertThat(verify).isEqualTo(result);
	}
}
