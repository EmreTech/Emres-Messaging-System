package com.emretech.messagingsystem;

import javax.swing.JFrame;

public class ServerTest {
	public static void main(String[] args) {
		Server emre = new Server();
		emre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		emre.startRunning();
	}
}
