package com.emretech.messagingsystem;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class Client extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private JTextField userText;
	private JTextArea chatWindow;
	private JButton settings;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	private String ServerUsername = "";
	private String ClientUsername = "";
	private String settingsOption = "";
	
	//Constructor
	public Client(String host) {
		super("Client on Emre's Messaging System");
		serverIP = host;
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
			new ActionListener() {
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
	
	//Connect to server
	public void startRunning() {
		try {
			setUsername();
			connectToServer();
			setupStreams();
			whileChatting();
		} catch (EOFException eof) {
			showMessage("\n Client terminated connection!");
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			closeClient();
		}
	}
	
	/*Commands
	private void checkForCommands(String message) {
		switch (message) {
		case "/ChangeUsernames":
			ClientUsername = JOptionPane.showInputDialog("What's your new username?");
			break;
		}
	} */
	
	//Connect to server
	private void connectToServer() throws IOException{
		showMessage("Connecting to server... \n");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("Connected to server. IP:" + connection.getInetAddress().getHostName());
		
	}
	
	//Get streams to send and collect data
	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are setup! \n");
	}
	
	//While chatting with server
	private void whileChatting() throws IOException {
		ableToType(true);
		sendUsername();
		System.out.println(ClientUsername);
		getUsername();
		try {
			ServerUsername = (String) input.readObject();
			showMessage("\n" + ServerUsername);
		} catch (ClassNotFoundException cnfe) {
			showMessage("\n ERROR: User sent info that cannot read!");
		}
		do {
			try {
				message = (String) input.readObject();
				showMessage("\n" + message);
				
			} catch (ClassNotFoundException cnfe) {
				showMessage("\n ERROR: User sent info that cannot read!");
			}
		} while (!message.equals(ServerUsername + " - /END"));
	}
	
	//Close the streams and sockets
	private void closeClient() {
		showMessage("\n Closing Client down...");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
	
	//Set username
	private void setUsername() throws IOException {
		ClientUsername = JOptionPane.showInputDialog("What's your username?");
	}
	//Find server's username
	private void getUsername() throws IOException {
		try {
			ServerUsername = (String) input.readObject();
			showMessage("\n" + ServerUsername);
		} catch (ClassNotFoundException cnfe) {
			showMessage("\n ERROR: User sent info that cannot read!");
		}
	}
	//Send messages to server
	private void sendMessage(String message) {
		try {
			output.writeObject(ClientUsername + " - " + message);
			output.flush();
			showMessage("\n" + ClientUsername + " - " + message);
		} catch (IOException io) {
			chatWindow.append("\n ERROR: Cannot send message");
		}
	}
	
	//Send your username to server
		private void sendUsername() {
			try {
				output.writeObject(ClientUsername);
				output.flush();
				showMessage("\n" + ClientUsername);
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
