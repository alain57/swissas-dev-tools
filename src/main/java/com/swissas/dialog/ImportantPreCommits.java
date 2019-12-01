package com.swissas.dialog;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.PreencodedMimeBodyPart;
import javax.mail.util.ByteArrayDataSource;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.swissas.ui.DragDropTextPane;
import com.swissas.util.ImageUtility;
import com.swissas.util.SwissAsStorage;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * The importantPreCommits dialog that appears if user configured 
 * his IntelliJ to ask for a reviewer or send an e-mail to other departments
 * @author Tavan Alain
 */
public class ImportantPreCommits extends JDialog {
	private static final Logger  LOGGER                    = Logger.getInstance("Swiss-as");
	private static final Pattern START_WITH_SUPPORT_STRING = Pattern
			.compile("^(#|sc|case|sup|support|story|request)\\s\\d+", CASE_INSENSITIVE);
	private static final String  SELECT_SOMEONE            = "Select someone !";
	
	private JPanel            contentPane;
	private JButton           buttonOK;
	private JButton           buttonCancel;
	private JCheckBox         informCheckbox;
	private JComboBox<String> reviewerComboBox;
	private DragDropTextPane  messageContent;
	private JLabel            reviewerLbl;
	private int               exitCode;
	
	public ImportantPreCommits(CheckinProjectPanel checkinProjectPanel) {
		setContentPane(this.contentPane);
		setModal(true);
		getRootPane().setDefaultButton(this.buttonOK);
		
		this.buttonOK.addActionListener(e -> onOK());
		
		this.buttonCancel.addActionListener(e -> onCancel());
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		
		this.contentPane.registerKeyboardAction(e -> onCancel(),
		                                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
		                                        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		this.informCheckbox.addActionListener(e -> {
			if(this.informCheckbox.isSelected()){
				this.messageContent.setText(checkinProjectPanel.getCommitMessage());
				this.messageContent.setEnabled(true);
			}else {
				this.messageContent.setText("");
				this.messageContent.setEnabled(false);
			}
			this.messageContent.setEnabled(this.informCheckbox.isSelected());
			
		});
		pack();
	}
	
	
	private void onOK() {
		boolean result = sendMail();
		this.exitCode = result ? DialogWrapper.OK_EXIT_CODE : DialogWrapper.CANCEL_EXIT_CODE;
		dispose();
	}
	
	private void onCancel() {
		this.exitCode = DialogWrapper.CANCEL_EXIT_CODE;
		dispose();
	}
	
	public int getExitCode() {
		return this.exitCode;
	}
	
	
	private boolean sendMail() {
		String message = this.messageContent.getText();
		Matcher matcher = START_WITH_SUPPORT_STRING.matcher(message);
		if (!matcher.find()) {
			Messages.showMessageDialog(
					"Your commit message needs to start with one of following options: #/SC/CASE/STORY/SUP/SUPPORT followed by case number",
					"Commit Message Invalid", Messages.getErrorIcon());
			return false;
		}
		Properties properties = System.getProperties();
		SwissAsStorage storage = SwissAsStorage.getInstance();
		properties.setProperty("mail.smtp.host", "sas-mail.swiss-as.com");
		Session session = Session.getDefaultInstance(properties, null);
		Multipart multipart = new MimeMultipart();
		BodyPart messageBodyPart = new MimeBodyPart();
		int imageCounter = 1;
		try {
			messageBodyPart.setText(message);
			multipart.addBodyPart(messageBodyPart);
			for (ImageIcon image : this.messageContent.getImages()) {
				String imageName = "image_" + imageCounter + ".jpg";
				String imageContent = ImageUtility.getInstance().imageToBase64Jpeg(image);
				MimeBodyPart  part = addJpegAttachment(imageName, imageContent);
				multipart.addBodyPart(part);
				imageCounter++;
			}
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(storage.getMyMail()));
			InternetAddress[] internetAddresses = Stream
					.of(storage.getQaMail(), storage.getDocuMail(), storage.getSupportMail())
					.map(this::generateAddress).filter(Objects::nonNull)
					.toArray(InternetAddress[]::new);
			
			msg.addRecipients(Message.RecipientType.TO, internetAddresses); //comment this for testing 
			//msg.addRecipient(Message.RecipientType.TO, new InternetAddress(storage.getMyMail())); //uncomment this for testing
			msg.setSubject("Automatic User Interface Change");
			msg.setContent(multipart);
			Transport.send(msg);
		} catch (Exception e) {
			Messages.showMessageDialog(e.getMessage(), "Mail Could not Be Sent",
			                           Messages.getErrorIcon());
			LOGGER.error(e);
			return false;
		}
		return true;
	}
	
	
	private MimeBodyPart addJpegAttachment(final String fileName, final String fileContent){
		if (fileName == null || fileContent == null) {
			return null;
		}
		MimeBodyPart filePart;
		try {
			DataSource ds = new ByteArrayDataSource(fileContent, "image/jpeg");
			filePart = new PreencodedMimeBodyPart("base64");
			filePart.setDataHandler(new DataHandler(ds));
			filePart.setFileName(fileName);
		} catch (MessagingException | IOException e) {
			LOGGER.error(e);
			filePart = null;
		}
		return filePart;
	}
	/**
	 * dummy method needed in order to generate an InternetAddress with a stream
	 */
	private InternetAddress generateAddress(String address) {
		InternetAddress result = null;
		if (address != null) {
			try {
				result = new InternetAddress(address);
			} catch (AddressException e) {
				LOGGER.error(e);
			}
		}
		return result;
	}
	
	
	public void refreshContent(boolean informOtherPeopleNeeded) {
		boolean reviewNeeded = SwissAsStorage.getInstance().isPreCommitCodeReview();
		this.informCheckbox.setEnabled(informOtherPeopleNeeded);
		if(reviewNeeded) {
			this.reviewerLbl.setVisible(true);
			this.reviewerComboBox.setVisible(true);
			this.reviewerComboBox.addItem(SELECT_SOMEONE);
			SwissAsStorage.getInstance().getMyTeamMembers().forEach(this.reviewerComboBox::addItem);
		} else {
			this.reviewerLbl.setVisible(false);
			this.reviewerComboBox.setVisible(false);
		}
	}
}
