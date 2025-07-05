package com.project01;
import java.sql.Connection;
import java.sql.DriverManager;

public class SqlServerConnection {
	Connection conn = null;
	
	public static Connection dbConnector() {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://localhost:1433;"
					+ "databaseName=EmployeeManagement;"
					+ "user=sa;"
					+ "password=123456;"
					+ "encrypt=true;"
					+ "trustServerCertificate=true;";
			
			Connection conn = DriverManager.getConnection(connectionUrl);

			System.out.println("Connection is successful");
			return conn;
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
