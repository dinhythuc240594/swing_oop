package com.project01;

import java.sql.Connection;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Launcher {

	private static Connection connection = null;
	
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//// 2025-05-31 - initialize database ////
		initDatabase();
		
		// Run this program on the Event Dispatch Thread (EDT)
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					//// 2025-05-31 - show login window  -> login to open main window ////
					System.out.println("start run application");
					LoginWindow loginWindow = new LoginWindow(connection);
					loginWindow.show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private static void initDatabase() {
		connection = sqliteConnection.dbConnector();
	}

}
