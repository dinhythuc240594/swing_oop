package com.project01;
import java.sql.Connection;
import java.sql.DriverManager;


public class sqliteConnection {
	Connection conn = null;
	
	public static Connection dbConnector() {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager
					.getConnection("jdbc:sqlite:D:\\Work\\ws_java\\Swing-01\\src\\com\\project01\\database\\employee_management.db");

			System.out.println("Connection is successful");
			return conn;
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
