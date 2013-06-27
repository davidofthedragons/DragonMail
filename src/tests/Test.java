package tests;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;


public class Test {

	private static int port = 465;
	private static String host = "smtp.gmail.com";
	private static String from = "davidofthedragons@gmail.com";
	private static String username = "davidofthedragons@gmail.com";
	private static String password = "<password>";
	private static Properties props;
	private static Authenticator auth = new Authenticator() {
		private PasswordAuthentication pa = new PasswordAuthentication(username, password);
		public PasswordAuthentication getPasswordAuthentication() {
			return pa;
		}
	};
	private static Session session;
		
	public static void init() {
		props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.ssl.enable", true);
		props.put("mail.smtp.auth", true);
		props.put("mail.store.protocol", "imaps");
		session = Session.getInstance(props, auth);
		//session.setDebug(true);
	}
	
	
	public static void sendMail(String to, String subject, String body) {
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(from));
			InternetAddress[] recip = {new InternetAddress(to)};
			message.setRecipients(Message.RecipientType.TO, recip);
			message.setSubject(subject);
			message.setSentDate(new Date());
			message.setText(body);
			System.out.println("Sending message titled \"" + subject + "\" to <" + to + ">");
			Transport.send(message);
		} catch(MessagingException e) {
			e.printStackTrace();
		}
	}
	
	public static void checkMail() {
		try {
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", username, password);
			Folder inbox = store.getFolder("Inbox");
			System.out.println(inbox.getUnreadMessageCount() + " unread message(s)");
			inbox.open(Folder.READ_WRITE);
			Message messages[] = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
			FetchProfile fp = new FetchProfile();
			fp.add(FetchProfile.Item.ENVELOPE);
			fp.add(FetchProfile.Item.CONTENT_INFO);
			inbox.fetch(messages, fp);
			for(int i=0; i< messages.length; i++) {
				System.out.println("MESSAGE #" + (i+1) + ":");
				System.out.println("	FROM: " + messages[i].getFrom()[0].toString());
				System.out.println("	SUBJECT: " + messages[i].getSubject());
				System.out.println("	CONTENTS:");
				System.out.println("	 " + messages[i].getContent().toString());
			}
			inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		init();
		checkMail();
		sendMail("davidofthedragons@gmail.com", "testish thing2", "testytestytesseract");
	}

}
