package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.*;
import javax.mail.*;
import javax.mail.internet.*;
import com.sun.mail.*;

public class Main {
	
	String version = "v0.1";
	File settingsFile = new File(".mailsettings");
	String username, password, host, port;
	Properties props;
	static JTextArea console = new JTextArea(5, 50);
	
	public Main() {
		if(!loadSettings()) {
			loadSettingsGUI();
		}
	}
	
	public boolean loadSettings() {
		if(!settingsFile.exists()) return false;
		try {
			Scanner scanner = new Scanner(settingsFile);
			username = scanner.nextLine();
			password = scanner.nextLine();
			host = scanner.nextLine();
			port = scanner.nextLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			printMessage(e.getMessage());
		}
		
		return true;
	}
	
	public void writeSettings() {
		printMessage("host: " + host + ":" + port);
	}
	
	public static void printMessage(String message) {
		console.setText(message);
		System.out.println(message);
	}
	
	public void loadGUI() {
		JFrame frame = new JFrame("DragonMail " + version);
		frame.setSize(1000, 600);
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
		final JComboBox<String> hostList = new JComboBox<String>(hosts);
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
			}
		});
		main.add(saveButton);
		frame.add(main, BorderLayout.CENTER);
		frame.add(new JScrollPane(console), BorderLayout.SOUTH);
		frame.setVisible(true);
		printMessage("Testing console");
	}

	public static void main(String[] args) {
		new Main();
	}

}
