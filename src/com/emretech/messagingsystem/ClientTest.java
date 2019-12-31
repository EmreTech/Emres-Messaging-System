package com.emretech.messagingsystem;

import javax.swing.*;

public class ClientTest {
	public static void main(String[] args) {
		Client berke = new Client(JOptionPane.showInputDialog("Type in server's IP:"));
		berke.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		berke.startRunning();
	}
}
// My Public IP: 72.199.138.163