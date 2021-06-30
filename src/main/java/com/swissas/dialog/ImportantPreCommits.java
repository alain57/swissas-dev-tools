package com.swissas.dialog;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import javax.swing.*;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.uiDesigner.core.*;
import com.swissas.ui.DragDropTextPane;
import com.swissas.util.ImageUtility;
import com.swissas.util.StringUtils;
import com.swissas.util.SwissAsStorage;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * The importantPreCommits dialog that appears if user configured
 * his IntelliJ to ask for a reviewer or send an e-mail to other departments
 *
 * @author Tavan Alain
 */
public class ImportantPreCommits extends JDialog {
	private static final Logger  LOGGER                    = Logger.getInstance("Swiss-as");
	private static final Pattern START_WITH_SUPPORT_STRING = Pattern
			.compile("^(#|sc|case|sup|support|story|request) ?(id|no)?.(\\d+[`']?\\d+)", CASE_INSENSITIVE);
	public static final Pattern REVIEWER                  = Pattern
			.compile("reviewed by ([a-z]{3,4})", CASE_INSENSITIVE);
	private static final String  SELECT_SOMEONE            = "Select a reviewer !";
	private static final String NO_REVIEW                 = "NO REVIEW";
	
	private final CheckinProjectPanel checkinProjectPanel;
	private       int                 exitCode;
	private       boolean             shouldDispose;
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel contentPane;
	private JCheckBox informCheckbox;
	private JComboBox<String> reviewerComboBox;
	private JLabel reviewerLbl;
	private DragDropTextPane messageContent;
	private JButton buttonOK;
	private JButton buttonCancel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public ImportantPreCommits(CheckinProjectPanel checkinProjectPanel) {
		initComponents();
		setContentPane(this.contentPane);
		setModal(true);
		getRootPane().setDefaultButton(this.buttonOK);
		this.checkinProjectPanel = checkinProjectPanel;
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
			if (this.informCheckbox.isSelected()) {
				this.messageContent.setText(checkinProjectPanel.getCommitMessage());
				this.messageContent.setEnabled(true);
			} else {
				this.messageContent.setText("");
				this.messageContent.setEnabled(false);
			}
			this.messageContent.setEnabled(this.informCheckbox.isSelected());
			
		});
		pack();
	}
	
	private void onOK() {
		this.shouldDispose = true;
		validateReviewerCombobox();
		validateInformMesssage();
		if (this.shouldDispose) {
			setVisible(false);
		}
	}
	
	private void validateInformMesssage() {
		if (this.exitCode == DialogWrapper.OK_EXIT_CODE && this.informCheckbox.isSelected()) {
			if (this.messageContent.getText().isEmpty()) {
				Messages.showErrorDialog("Please fill the graphical change text", "Error");
				this.exitCode = DialogWrapper.CANCEL_EXIT_CODE;
				this.shouldDispose = false;
			} else {
				String message = this.messageContent.getText();
				Matcher matcher = START_WITH_SUPPORT_STRING.matcher(message);
				if (!matcher.find()) {
					Messages.showMessageDialog(
							"Your commit message needs to start with one of following options: #/SC/CASE/STORY/SUP/SUPPORT followed by case number",
							"Commit Message Invalid", Messages.getErrorIcon());
					this.shouldDispose = false;
					this.exitCode = DialogWrapper.CANCEL_EXIT_CODE;
				}else {
					this.exitCode = DialogWrapper.OK_EXIT_CODE;
				}
			}
		}
	}
	
	private boolean hasLetterCode(){
		return Optional.ofNullable(this.reviewerComboBox.getSelectedItem())
				.map(String.class::cast).map(StringUtils.getInstance()::isLetterCode).orElse(false);
	}
	
	private boolean hasNoReview(){
		return NO_REVIEW.equalsIgnoreCase((String)this.reviewerComboBox.getSelectedItem());
	}
	
	private void validateReviewerCombobox() {
		this.exitCode = DialogWrapper.OK_EXIT_CODE;
		if (this.reviewerComboBox.isVisible()) {
			if (!hasLetterCode() && !hasNoReview()) {
				Messages.showErrorDialog("Please select a reviewer, type a valid 4LC or type 'NO REVIEW'", "Error");
				this.shouldDispose = false;
				this.exitCode = DialogWrapper.CANCEL_EXIT_CODE;
			} else {
				String reviewText = Optional.ofNullable(this.reviewerComboBox.getSelectedItem()).map(String.class::cast).orElse("");
				String commitMessage = this.checkinProjectPanel.getCommitMessage();
				if(hasLetterCode() && !commitMessage.toUpperCase().contains(reviewText)) {
					this.checkinProjectPanel
							.setCommitMessage(commitMessage + " reviewed by " + reviewText);
				}
			}
		}
	}
	
	private void onCancel() {
		this.exitCode = DialogWrapper.CANCEL_EXIT_CODE;
		dispose();
	}
	
	public int getExitCode() {
		return this.exitCode;
	}
	
	public void sendMail() {
		if(this.informCheckbox.isSelected()) {
			Properties properties = System.getProperties();
			SwissAsStorage storage = SwissAsStorage.getInstance();
			properties.setProperty("mail.smtp.host", "sas-mail.swiss-as.com");
			List<String> destinationMails = Stream.of(storage.getQaMail(), storage.getDocuMail(), storage.getSupportMail())
			                                      .filter(Objects::nonNull).collect(Collectors.toList());
			try {
				Message msg = generateMessage(properties, storage.getMyMail(), destinationMails);
				Transport.send(msg);
			} catch (Exception e) {
				Messages.showMessageDialog(e.getMessage(), "Mail Could not Be Sent",
				                           Messages.getErrorIcon());
				LOGGER.error(e);
			}
		}
		dispose();
	}
	
	private Message generateMessage(Properties properties, String sender, List<String> destination) throws MessagingException {
		Session session = Session.getDefaultInstance(properties, null);
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(sender));
		InternetAddress[] internetAddresses = destination.stream()
				.map(this::generateAddress).filter(Objects::nonNull)
				.toArray(InternetAddress[]::new);
		
		msg.addRecipients(Message.RecipientType.TO,
		                  internetAddresses);
		msg.setSubject("Automatic User Interface Change");
		msg.setContent(generateMultipartWithMessageContent());
		return msg;
	}
	
	private Multipart generateMultipartWithMessageContent() throws MessagingException {
		Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(this.messageContent.getText());
			multipart.addBodyPart(messageBodyPart);
			int imageCounter = 1;
			for (ImageIcon image : this.messageContent.getImages()) {
				String imageName = "image_" + imageCounter + ".jpg";
				String imageContent = ImageUtility.getInstance().imageToBase64Jpeg(image);
				MimeBodyPart part = addJpegAttachment(imageName, imageContent);
				multipart.addBodyPart(part);
				imageCounter++;
			}
		return multipart;
	}
	
	private MimeBodyPart addJpegAttachment(final String fileName, final String fileContent) {
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
		this.reviewerComboBox.removeAllItems();
		this.reviewerComboBox.addItem(SELECT_SOMEONE);
		this.reviewerComboBox.getEditor().selectAll();
		SwissAsStorage.getInstance().getMyTeamMembersForReview().forEach(this.reviewerComboBox::addItem);
		if (reviewNeeded) {
			Matcher matcher = REVIEWER.matcher(this.checkinProjectPanel.getCommitMessage());
			if (matcher.find()) {
				String reviewerInCommitMessage = matcher.group(1).toUpperCase();
				boolean hasReviewer = SwissAsStorage.getInstance().getMyTeamMembers().contains(reviewerInCommitMessage);
				this.reviewerComboBox.setSelectedItem(hasReviewer ? reviewerInCommitMessage : SELECT_SOMEONE);
			}else if(this.checkinProjectPanel.getCommitMessage().toUpperCase().contains(NO_REVIEW)){
				this.reviewerComboBox.setSelectedItem(NO_REVIEW);
			}
			this.reviewerLbl.setVisible(true);
			this.reviewerComboBox.setVisible(true);
		} else {
			this.reviewerLbl.setVisible(false);
			this.reviewerComboBox.setVisible(false);
		}
	}
	

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		ResourceBundle bundle = ResourceBundle.getBundle("texts");
		this.contentPane = new JPanel();
		var panel1 = new JPanel();
		this.informCheckbox = new JCheckBox();
		this.reviewerComboBox = new JComboBox<>();
		this.reviewerLbl = new JLabel();
		var scrollPane1 = new JScrollPane();
		this.messageContent = new DragDropTextPane();
		var panel2 = new JPanel();
		var hSpacer1 = new Spacer();
		var panel3 = new JPanel();
		this.buttonOK = new JButton();
		this.buttonCancel = new JButton();

		{
			this.contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));

			{
				panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));

				this.informCheckbox.setText(bundle.getString("precommit.checkbox.inform"));
				panel1.add(this.informCheckbox, new GridConstraints(1, 0, 1, 1,
					GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					GridConstraints.SIZEPOLICY_WANT_GROW,
					null, new Dimension(149, 303), null));

				this.reviewerComboBox.setModel(new DefaultComboBoxModel<>(new String[] {

				}));
				this.reviewerComboBox.setEditable(true);
				panel1.add(this.reviewerComboBox, new GridConstraints(0, 1, 1, 1,
					GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
					GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
					GridConstraints.SIZEPOLICY_FIXED,
					null, new Dimension(509, 30), null));

				this.reviewerLbl.setText(bundle.getString("precommit.label.reviewer"));
				panel1.add(this.reviewerLbl, new GridConstraints(0, 0, 1, 1,
					GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
					GridConstraints.SIZEPOLICY_FIXED,
					GridConstraints.SIZEPOLICY_FIXED,
					null, null, null));

				{
					scrollPane1.setViewportView(this.messageContent);
				}
				panel1.add(scrollPane1, new GridConstraints(1, 1, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
					null, null, null));
			}
			this.contentPane.add(panel1, new GridConstraints(0, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
				null, null, null));

			{
				panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
				panel2.add(hSpacer1, new GridConstraints(0, 0, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
					GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK,
					null, null, null));

				{
					panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));

					this.buttonOK.setText("OK");
					panel3.add(this.buttonOK, new GridConstraints(0, 0, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_FIXED,
						null, null, null));

					this.buttonCancel.setText("Cancel");
					panel3.add(this.buttonCancel, new GridConstraints(0, 1, 1, 1,
						GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
						GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
						GridConstraints.SIZEPOLICY_FIXED,
						null, null, null));
				}
				panel2.add(panel3, new GridConstraints(0, 1, 1, 1,
					GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
					null, null, null));
			}
			this.contentPane.add(panel2, new GridConstraints(1, 0, 1, 1,
				GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK,
				null, null, null));
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

}
