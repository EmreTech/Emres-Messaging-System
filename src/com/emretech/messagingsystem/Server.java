package com.emretech.messagingsystem;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class Server extends JFrame{
	
	private static final long serialVersionUID = 1L;
	
	private JTextField userText;
	private JTextArea chatWindow;
	private JButton settings;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	private FileManagement fileManage = new FileManagement();
	private String message = "";
	private String ServerUsername = "";
	private String ClientUsername = "";
	private String settingsOption = "";
	private List<String> ClientIPs = new ArrayList<String>();
	
	//Constructor
	public Server() {
		super("Emre's Messaging System");
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					sendMessage(event.getActionCommand());
					userText.setText("");
				}
			}
		);
		super.add(userText, BorderLayout.SOUTH);
		
		chatWindow = new JTextArea();
		DefaultCaret caret = (DefaultCaret) chatWindow.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		chatWindow.setLineWrap(true);
        chatWindow.setWrapStyleWord(true);
		chatWindow.setEditable(false);
		super.add(new JScrollPane(chatWindow));
		
		settings = new JButton("Settings");
		settings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settingsOption = JOptionPane.showInputDialog("Settings Menu. Don't type anything to exit.");
				if (settingsOption != "null") {
					settingsOption = settingsOption.toLowerCase();
					switch (settingsOption) {
					case "change username":
						try {
							setUsername();
							sendUsername();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						break;
					}
				}
			}
		});
		super.add(settings, BorderLayout.NORTH);
		
		super.setSize(300, 150);
		super.setVisible(true);
	}
	
	//Setup and run server
	public void startRunning() {
		try {
			server = new ServerSocket(6789, 10);
			setUsername();
			while (true) {
				try {
					waitForConnection();
					setupStreams();
					whileChatting();
				} catch (EOFException ex) {
					showMessage("\n Server ended the connection!");
				} finally {
					closeServer();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/*Commands
	private void checkForCommands(String message) {
		switch (message) {
		case "/ChangeUsernames":
			ClientUsername = JOptionPane.showInputDialog("What's your new username?");
			break;
		}
	}*/
	
	//Wait for connection, then display connection info
	private void waitForConnection() throws IOException {
		showMessage("Waiting for someone to connect to you... \n");
		connection = server.accept();
		showMessage(" Now connected to " + connection.getInetAddress().getHostName());
		ClientIPs.add(connection.getInetAddress().getHostName());
	}
	
	//Get streams to send and collect data
	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are setup! \n");
	}
	
	//During the chat conversation
	private void whileChatting() throws IOException {
		message = " You are now connected to other client! ";
		sendUsername();
		getUsername();
		sendMessage(message);
		ableToType(true);
		do {
			try {
				message = (String) input.readObject();
				showMessage("\n" + message);
				//checkForCommands(message);
			} catch (ClassNotFoundException ex) {
				showMessage("\n ERROR: User sent info that cannot read!");
			}
		} while (!message.equals(ClientUsername + " - /END"));
	}
	
	//Close streams and sockets after you are done chatting
	private void closeServer() {
		int answer = JOptionPane.showConfirmDialog(null, "Would you like to save the chat history on this computer?", "Question", JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			File log = new File(System.getProperty("user.dir") + "/res/" + "log.txt");
			if (!log.exists()) {
				fileManage.createFile(log);
				fileManage.writeToFile(log, chatWindow.getText());
			} else {
				fileManage.writeToFile(log, chatWindow.getText());
			}
		}
		
		showMessage("\n Closing Server down... \n");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	//Set username
	private void setUsername() throws IOException{
		ServerUsername = JOptionPane.showInputDialog("What's your username?");
	}
	//Get Client's username
	private void getUsername() throws IOException {
		try {
			ClientUsername = (String) input.readObject();
			final int mid = ClientUsername.length() / 2; 
			String[] parts = {ClientUsername.substring(0, mid),ClientUsername.substring(mid)};
			System.out.println(parts[0]); 
			System.out.println(parts[1]); 
			ClientUsername = parts[0];
			showMessage("\n" + ClientUsername);
		} catch (ClassNotFoundException cnfe) {
			showMessage("\n ERROR: User sent info that cannot read!");
		}
	}
	
	//Send a message to client
	private void sendMessage(String message) {
		try {
			output.writeObject(ServerUsername + " - " + message);
			output.flush();
			showMessage("\n" + ServerUsername + " - " + message);
		} catch (IOException ex) {
			chatWindow.append("\n ERROR: Cannot send message");
		}
	}
	
	//Send your username to clients
	private void sendUsername() {
		try {
			output.writeObject(ServerUsername);
			output.flush();
			showMessage("\n" + ServerUsername);
		} catch (IOException ex) {
			chatWindow.append("\n ERROR: Cannot send username");
		}
	}
	
	//Updates chatWindow
	private void showMessage(final String text) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					chatWindow.append(text);
				}
			}
		);
	}
	
	//Let user type text into box
	private void ableToType(final boolean tof) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					userText.setEditable(tof);	
				}
			}
		);
	}
}
