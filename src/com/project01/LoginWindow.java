package com.project01;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginWindow {
	
	private JFrame loginWindow;
	
	// call the connection
	private Connection connection = null;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JLabel errorLabel;
	private int userId = -1;
	private String userRole = null;
	private String userName;
	private SessionManager sessionManager;
	private MainWindow mainWindow;
	
	public LoginWindow(Connection connection) {
		//// 2025-05-31 - inhered connection login ////
		this.connection = connection;
		initialize();
	}
	
	private void initialize() {
		loginWindow = new JFrame();
		loginWindow.setTitle("Login Page");
		loginWindow.setBackground(Color.WHITE);
		loginWindow.setFont(new Font("Dialog", Font.BOLD, 14));
		loginWindow.setBounds(100, 100, 600, 400); // Increased window size
		loginWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		loginWindow.getContentPane().setLayout(null);
		
		// Create main panel with absolute layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		mainPanel.setBounds(0, 0, 600, 400);
		mainPanel.setBackground(Color.WHITE);
		loginWindow.getContentPane().add(mainPanel);
		
		// Add Login Image
		JLabel lblLoginImage = new JLabel("");
		Image imgLogin = new ImageIcon(this.getClass().getResource("img/Login.png")).getImage();
		lblLoginImage.setIcon(new ImageIcon(imgLogin));
		lblLoginImage.setBounds(50, 50, 200, 200); // Adjusted image position and size
		mainPanel.add(lblLoginImage);
		
		// Username
		JLabel lblUserName = new JLabel("Username");
		lblUserName.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblUserName.setBounds(300, 80, 100, 30);
		mainPanel.add(lblUserName);
		
		usernameField = new JTextField(20);
		usernameField.setBounds(300, 110, 200, 35); // Increased field size
		usernameField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainPanel.add(usernameField);
		
		// Password
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblPassword.setBounds(300, 160, 100, 30);
		mainPanel.add(lblPassword);
		
		passwordField = new JPasswordField(20);
		passwordField.setBounds(300, 190, 200, 35); // Increased field size
		passwordField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainPanel.add(passwordField);
		
		// Error label
		errorLabel = new JLabel("");
		errorLabel.setForeground(Color.RED);
		errorLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		errorLabel.setBounds(300, 230, 200, 20);
		errorLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mainPanel.add(errorLabel);
		
		// Login button
		JButton btnLogin = new JButton("Login");
		Image imgBtn = new ImageIcon(this.getClass().getResource("img/Ok-icon.png")).getImage();
		btnLogin.setIcon(new ImageIcon(imgBtn));
		btnLogin.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnLogin.setBounds(300, 260, 200, 40); // Increased button size
		mainPanel.add(btnLogin);
		
		// Add Enter key functionality
		KeyAdapter enterKeyAdapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					performLogin();
				}
			}
		};
		
		usernameField.addKeyListener(enterKeyAdapter);
		passwordField.addKeyListener(enterKeyAdapter);
		
		btnLogin.addActionListener(e -> performLogin());
	}
	
	private void performLogin() {
		try {
			String user_name = usernameField.getText();
			String pwd = new String(passwordField.getPassword());
			
			if(user_name.isEmpty() || pwd.isEmpty()) {
				errorLabel.setText("Please enter both username and password");
				return;
			}
			
			// Query to select user_name and password
			String query = "SELECT * FROM users WHERE username=? and password=?";
			PreparedStatement pst = connection.prepareStatement(query);
			pst.setString(1, user_name);
			pst.setString(2, pwd);
			
			ResultSet rs = pst.executeQuery();
			
			int count = 0;
			while (rs.next()) {
				setUserId(rs.getInt("id"));
				userName = rs.getString("username");
				setUserRole(rs.getString("role"));
				count++;
			}
			
			if(count == 1) {
				// Close login window
				loginWindow.dispose();
				
				// Set session login for user login
				sessionManager = SessionManager.getInstance();
				sessionManager.login(getUserId(), getUserName(), getUserRole());
				
				// Show main window
				mainWindow = new MainWindow();
				mainWindow.show();
			} else if(count > 1) {
				errorLabel.setText("Duplicate username and password found");
			} else {
				errorLabel.setText("Invalid username or password");
			}
			rs.close();
			pst.close();
		} catch (Exception e1) {
			errorLabel.setText("Error: " + e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	public void show() {
		loginWindow.setVisible(true);
	}
	
	public void hide() {
		loginWindow.setVisible(false);
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
	
}
