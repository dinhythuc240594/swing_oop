package com.project01;
import java.sql.Connection;
import java.sql.DriverManager;
import java.io.File;

public class SqlServerConnection {
	Connection conn = null;
	
	public static Connection dbConnector() {
		try {
			Connection conn = null;
				boolean isOptionDB = false;
				if(isOptionDB) {
					Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
					String connectionUrl = "jdbc:sqlserver://localhost:1433;"
							+ "databaseName=EmployeeManagement;"
							+ "user=sa;"
							+ "password=123456;"
							+ "encrypt=true;"
							+ "trustServerCertificate=true;";
					conn = DriverManager.getConnection(connectionUrl);
				}else{
					Class.forName("org.sqlite.JDBC");
					File dbFile = new File("."); 
					conn = DriverManager
							.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath() + "\\employee_management.db");
				}

			System.out.println("Connection is successful");
			return conn;
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
