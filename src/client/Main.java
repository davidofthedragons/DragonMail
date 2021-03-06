/********************************
 * DragonMail Email Client      *
 * (C) 2013 David Gardner       *
 *                              *
 * Source released under the    *
 * 	GNU General Public License  *
 *                              *
 ********************************/


package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.*;
import javax.mail.search.FlagTerm;

public class Main {
	String version = "Beta v1.1";
	File settingsFile = new File(".mailsettings");
	String username, password, host, port;
	Properties props = new Properties();
	Session session;
	Authenticator auth;
	static JTextArea console = new JTextArea(5, 50); //Unimplemented
	DefaultListModel listModel = new DefaultListModel();
	ArrayList<DMessage> messages = new ArrayList<DMessage>();
	JList messageList = new JList();
	
	public Main() {
		loadGUI();
		if(!loadSettings()) {
			loadSettingsGUI();
			return;
		}
		init();
	}
	
	public void init() {
		auth = new Authenticator() {
			private PasswordAuthentication pa = new PasswordAuthentication(username, password);
			public PasswordAuthentication getPasswordAuthentication() {
				return pa;
			}
		};
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.ssl.enable", true);
		props.put("mail.smtp.auth", true);
		props.put("mail.store.protocol", "imaps");
		session = Session.getInstance(props, auth);
		addToList(checkMail());
	}
	
	public boolean loadSettings() {
		if(!settingsFile.exists()) return false;
		try {
			Scanner scanner = new Scanner(settingsFile);
			username = scanner.nextLine();
			password = scanner.nextLine();
			host = scanner.nextLine();
			port = scanner.nextLine();
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			printMessage(e.getMessage());
		}
		return true;
	}
	
	public void writeSettings() {
		printMessage("host: " + host + ":" + port);
		try {
			PrintWriter writer = new PrintWriter(settingsFile);
			writer.write(username + "\n");
			writer.write(password + "\n");
			writer.write(host + "\n");
			writer.write(port + "\n");
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void printMessage(String message) {
		console.setText(message);
		System.out.println(message);
	}
	
	public void loadGUI() {
		JFrame frame = new JFrame("DragonMail " + version);
		frame.setSize(1000, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		//frame.add(console, BorderLayout.SOUTH);
		JSplitPane messagePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		final JTextArea messageArea = new JTextArea(10, 10);
		messagePanel.add(messageArea, BorderLayout.CENTER);
		messagePane.setRightComponent(messagePanel);
		messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		messageList.setLayoutOrientation(JList.VERTICAL);
		messageList.setVisibleRowCount(5);
		messagePane.setLeftComponent(new JScrollPane(messageList));
		
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridBagLayout());
		GridBagConstraints label = new GridBagConstraints();
		GridBagConstraints field = new GridBagConstraints();
		
		label.gridx = 0;
		label.gridy = 0;
		field.gridx = 1;
		field.gridy = 0;
		infoPanel.add(new JLabel("To"), label);
		final JTextField toField = new JTextField(30);
		toField.setEditable(false);
		infoPanel.add(toField, field);
		label.gridy = 1;
		field.gridy = 1;
		infoPanel.add(new JLabel("From "), label);
		final JTextField fromField = new JTextField(30);
		fromField.setEditable(false);
		infoPanel.add(fromField, field);
		label.gridx = 2;
		label.gridy = 0;
		field.gridx = 3;
		field.gridy = 0;
		infoPanel.add(new JLabel("CC"), label);
		final JTextField ccField = new JTextField(30);
		ccField.setEditable(false);
		infoPanel.add(ccField, field);
		label.gridy = 1;
		field.gridy = 1;
		infoPanel.add(new JLabel("BCC"), label);
		final JTextField bccField = new JTextField(30);
		bccField.setEditable(false);
		infoPanel.add(bccField, field);
		label.gridy = 2;
		field.gridy = 2;
		label.gridx = 0;
		field.gridx = 1;
		field.gridwidth = 4;
		infoPanel.add(new JLabel("Subject "), label);
		final JTextField subjectField = new JTextField(63);
		subjectField.setEditable(false);
		infoPanel.add(subjectField, field);
		messagePanel.add(infoPanel, BorderLayout.NORTH);
		field.gridx = 4;
		field.gridy = 0;
		final JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DMessage m = new DMessage();
				StringTokenizer t = new StringTokenizer(toField.getText(), "; ");
				String[] array = new String[t.countTokens()];
				if(array.length == 0) {
					printMessage("Message must have at least one recipient");
					return;
				}
				for(int i=0; i<array.length; i++) {
					array[i] = t.nextToken();
				}
				m.setTo(array);
				m.setFrom(username);
				t = new StringTokenizer(ccField.getText(), "; ");
				array = new String[t.countTokens()];
				for(int i=0; i<array.length; i++) {
					array[i] = t.nextToken();
				}
				m.setcc(array);
				t = new StringTokenizer(bccField.getText(), "; ");
				array = new String[t.countTokens()];
				for(int i=0; i<array.length; i++) {
					array[i] = t.nextToken();
				}
				m.setbcc(array);
				m.setSubject(subjectField.getText());
				m.setContent(messageArea.getText());
				sendMail(m);
			}
		});
		sendButton.setEnabled(false);
		JButton checkButton = new JButton("Check Mail");
		checkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageArea.setEditable(false);
				addToList(checkMail());
				sendButton.setEnabled(false);
				fromField.setEnabled(true);
				toField.setEditable(false);
				ccField.setEditable(false);
				bccField.setEditable(false);
				subjectField.setEditable(false);
			}
		});
		infoPanel.add(checkButton, field);
		
		JButton composeButton = new JButton("Compose");
		composeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendButton.setEnabled(true);
				fromField.setEnabled(false);
				toField.setEditable(true);
				ccField.setEditable(true);
				bccField.setEditable(true);
				subjectField.setEditable(true);
				toField.setText("");
				fromField.setText("");
				ccField.setText("");
				bccField.setText("");
				subjectField.setText("");
				messageArea.setText("");
				messageArea.setEditable(true);
			}
		});
		field.gridy = 1;
		infoPanel.add(composeButton, field);
		
		field.gridy = 2;
		infoPanel.add(sendButton, field);
		messagePanel.add(infoPanel, BorderLayout.NORTH);
		
		messageList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				toField.setEditable(false);
				fromField.setEditable(false);
				fromField.setEnabled(true);
				ccField.setEditable(false);
				bccField.setEditable(false);
				subjectField.setEditable(false);
				messageArea.setEditable(false);
				
				DMessage m = messages.get(messageList.getSelectedIndex());
				toField.setText(m.getToString());
				fromField.setText(m.getFrom());
				ccField.setText(m.getccString());
				bccField.setText(m.getbccString());
				subjectField.setText(m.getSubject());
				messageArea.setText(m.getContent());
			}
		});
		
		frame.add(messagePane, BorderLayout.CENTER);
		
		frame.setVisible(true);
	}
	
	public DMessage[] checkMail() {
		printMessage("Checking Mail...");
		try {
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", username, password);
			Folder inbox = store.getFolder("Inbox");
			printMessage(inbox.getUnreadMessageCount() + " unread message(s)");
			inbox.open(Folder.READ_WRITE);
			Message messages[] = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
			FetchProfile fp = new FetchProfile();
			fp.add(FetchProfile.Item.ENVELOPE);
			fp.add(FetchProfile.Item.CONTENT_INFO);
			inbox.fetch(messages, fp);
			inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
			DMessage[] dm = new DMessage[messages.length];
			for(int i=0; i<messages.length; i++) {
				dm[i] = new DMessage(messages[i]);
			}
			return dm;
		} catch (MessagingException e) {
			printMessage(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void addToList(DMessage[] m) {
		for(int i=0; i<m.length; i++) {
			messages.add(m[i]);
			listModel.addElement(m[i].getFrom() + " - " + m[i].getSubject()
					+ "     " + DateFormat.getInstance().format(m[i].getDate()));
			System.out.println("Message " + i + ": " + m[i].getFrom() + " - " 
					+ m[i].getSubject() + "\t" + DateFormat.getInstance().format(m[i].getDate()));
		}
		messageList.setModel(listModel);
	}
	
	public void sendMail(DMessage message) {
		try {
			MimeMessage m = message.getMimeMessage(session);
			printMessage("Sending message titled \"" + message.getSubject() + "\"");
			Transport.send(m);
			printMessage("Done");
		} catch(MessagingException e) {
			e.printStackTrace();
			printMessage("Message send failure");
		}
	}
	
	public void loadSettingsGUI() {
		final JFrame frame = new JFrame("DragonMail " + version + " - Settings");
		frame.setSize(600, 600);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		JPanel main = new JPanel();
		main.add(new JLabel("Username:"));
		final JTextField uField = new JTextField(20);
		main.add(uField);
		main.add(new JLabel("Password"));
		final JPasswordField pField = new JPasswordField(20);
		main.add(pField);
		main.add(new JLabel("Host"));
		String[] hosts = {"Gmail"};
		final JComboBox hostList = new JComboBox(hosts);
		hostList.setEditable(false);
		main.add(hostList);
		JButton saveButton = new JButton("Save Settings");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				username = uField.getText();
				password = new String(pField.getPassword());
				if(hostList.getSelectedItem().toString().equals("Gmail")) {
					host = "smtp.gmail.com";
					port = "465";
				}
				writeSettings();
				frame.setVisible(false);
				init();
			}
		});
		main.add(saveButton);
		frame.add(main, BorderLayout.CENTER);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new Main();
	}

}
