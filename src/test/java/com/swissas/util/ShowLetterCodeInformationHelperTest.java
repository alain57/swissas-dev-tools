package com.swissas.util;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.intellij.openapi.ui.popup.JBPopup;
import com.swissas.beans.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowLetterCodeInformationHelperTest {
	
	@Test
	void testUserWithoutLetterCodeGetsAnonymousPicture() {
		JBPopup popup = mock(JBPopup.class);
		when(popup.getContent()).thenReturn(new JPanel());
		JLabel label = new JLabel();
		ShowLetterCodeInformationHelper.PictureLoader loader =
				new ShowLetterCodeInformationHelper.PictureLoader(label, new User(), popup);
		
		assertThat(loader.doInBackground()).isNull();
		loader.done();
		
		assertThat(label.getIcon()).isInstanceOf(ImageIcon.class);
		assertThat(((ImageIcon) label.getIcon()).getDescription())
				.endsWith("/images/anonymous.png");
	}
}
