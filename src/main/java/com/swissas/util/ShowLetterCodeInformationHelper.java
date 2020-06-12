package com.swissas.util;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.swissas.beans.User;

/**
 * A helper class for the display User Information logic.
 *
 * @author Tavan Alain
 */

public class ShowLetterCodeInformationHelper {
	
	private ShowLetterCodeInformationHelper() throws IllegalAccessException {
		throw new IllegalAccessException("Helper class");
	}
	
	public static void displayInformation(String authorString, String errorText){
		String userInfos = null;
		String errorMessage = errorText;
		User user = null;
		JLabel lbl = new JLabel();
		if(authorString != null){
			if(authorString.startsWith("T_")) {
				userInfos = "A mighty Anonymous from Team " + authorString.substring(authorString.indexOf("_") + 1);
				user = new User();
			}else if(ProjectUtil.getInstance().isGitProject()) {
					Map<String, String> fullNameTo4LC = SwissAsStorage.getInstance().getFullNameTo4LcMap();
					authorString = fullNameTo4LC.getOrDefault(authorString, authorString);
			}
			
			authorString = authorString.toUpperCase();
			Map<String, User> userMap = SwissAsStorage.getInstance().getUserMap();
			if (userMap.containsKey(authorString)) {
				user = userMap.get(authorString);
				userInfos = user.getInfos();
			} else {
				errorMessage = "<html><b>Could not find \"" + authorString
				               + "\" in the internal phone book</b><br>Is this person still working at Swiss-as ?";
			}
			
		}
		JPanel pane = new JPanel(new BorderLayout());
		JLabel image = new JLabel(new ImageIcon(ShowLetterCodeInformationHelper.class.getResource("/images/loading.gif")));//add loading sign here, best option as big as pictures
		if(userInfos == null){
			lbl.setText(errorMessage);
			pane.add(lbl);
		}else {
			lbl.setText(userInfos);
			pane.add(lbl, BorderLayout.EAST);
			pane.add(image, BorderLayout.WEST);
		}
		
		ComponentPopupBuilder componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(pane, null);
		JBPopup popup = componentPopupBuilder.createPopup();
		popup.showInFocusCenter();
		new PictureLoader(image, user, popup).execute();
	}
	
	
	static class PictureLoader extends SwingWorker<ImageIcon, Void> {
		
		final         User    user;
		final         JLabel  label;
		private final JBPopup popup;
		
		public PictureLoader(JLabel label, User user, JBPopup popup) {
			this.user = user;
			this.label = label;
			this.popup = popup;
		}
		
		@Override
		protected ImageIcon doInBackground() {
			if(this.user == null || this.user.getLc() == null) {
				return null;
			}
			return this.user.getPicture();
		}
		
		@Override
		protected void done() {
			try {
				if(this.user == null) {
					return;
				}
				ImageIcon icon = this.user.getLc() == null ? new ImageIcon(PictureLoader.class.getResource("/images/anonymous.png")) : get(); 
				
				this.label.setIcon(icon);
				this.label.invalidate();
				this.popup.getContent().invalidate();
				this.popup.getContent().repaint();
				this.popup.pack(true, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
