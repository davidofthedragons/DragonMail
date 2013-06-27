package client;

import java.io.IOException;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;

public class DMessage {
	
	private String[] to, cc, bcc;
	private String from;
	private String subject;
	private String content;
	private Date date;
	private String id;
	
	public DMessage(String[] to, String from, String[] cc, String[] bcc, String subject, String content, Date date) {
		this.to=to; this.from=from; this.cc=cc; this.bcc=bcc; this.subject=subject; this.content=content;
	}
	public DMessage() {
		
	}
	public DMessage(Message message) throws MessagingException, IOException {
		from = message.getFrom()[0].toString();
		to = new String[message.getRecipients(RecipientType.TO).length];
		for(int i=0; i<message.getRecipients(RecipientType.TO).length; i++) {
			to[i] = message.getRecipients(RecipientType.TO)[i].toString();
		}
		cc = (message.getRecipients(RecipientType.CC)!=null)? new String[message.getRecipients(RecipientType.CC).length] : new String[0];
		for(int i=0; i<cc.length; i++) {
			cc[i] = message.getRecipients(RecipientType.CC)[i].toString();
		}
		bcc = (message.getRecipients(RecipientType.BCC)!=null)? new String[message.getRecipients(RecipientType.BCC).length] : new String[0];
		for(int i=0; i<bcc.length; i++) {
			bcc[i] = message.getRecipients(RecipientType.BCC)[i].toString();
		}
		id = Integer.toString(message.getMessageNumber());
		date = message.getSentDate();
		subject = message.getSubject();
		content = message.getContent().toString();
	}
	
	public void setTo(String[] to) {this.to=to;}
	public void setFrom(String from) {this.from=from;}
	public void setcc(String[] cc) {this.cc=cc;}
	public void setbcc(String[] bcc) {this.bcc=bcc;}
	public void setSubject(String subject) {this.subject=subject;}
	public void setContent(String content) {this.content=content;}
	
	public String[] getTo() {return to;}
	public String getFrom() {return from;}
	public String[] getcc() {return cc;}
	public String[] getbcc() {return bcc;}
	public String getSubject() {return subject;}
	public String getContent() {return content;}
	public Date getDate() {return date;}
	public String getid() {return id;}
	
	
}
