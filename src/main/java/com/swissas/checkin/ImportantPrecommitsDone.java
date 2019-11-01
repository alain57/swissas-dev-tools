package com.swissas.checkin;

import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JComponent;



import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.swissas.util.SwissAsStorage;
import org.jetbrains.annotations.Nullable;

/**
 * The Dialog for the actions that may be mandatory before each commits.
 *
 * @author Tavan Alain
 */

public class ImportantPrecommitsDone extends DialogWrapper {
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("texts");
	private final CheckinProjectPanel checkinProjectPanel;
	private final boolean informQASelected;
	private final boolean reviewSelected;
	
	public ImportantPrecommitsDone(CheckinProjectPanel checkinProjectPanel){
		super(true);
		this.checkinProjectPanel = checkinProjectPanel;
		this.informQASelected = SwissAsStorage.getInstance().isPreCommitInformQA();
		this.reviewSelected = SwissAsStorage.getInstance().isPreCommitCodeReview();
		
		setTitle("Precommit Important Tasks");
		setOKButtonText(RESOURCE_BUNDLE.getString("yes"));
		
		init();
		
		
	}
	
	private boolean informQANeeded(){
		boolean informQA = SwissAsStorage.getInstance().isPreCommitInformQA();
		this.checkinProjectPanel.getFiles();//TODO
		return informQA;
	}
	
	private void sendMail(){
		Properties properties = new Properties();
		Session session = Session.getDefaultInstance(properties, null);
		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(SwissAsStorage.getInstance().getFourLetterCode() + "@swiss-as.com"));
			msg.addRecipient(RecipientType.TO, new InternetAddress(SwissAsStorage.getInstance().getFourLetterCode() + "@swiss-as.com"));
			msg.setSubject("QA test");
			msg.setText("my nice text goes here");
			Transport.send(msg);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return null;
	}
}
