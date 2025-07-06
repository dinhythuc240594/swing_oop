package com.project01;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

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
	
	// Modern color scheme
	private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
	private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
	private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
	private static final Color DANGER_COLOR = new Color(231, 76, 60);
	private static final Color LIGHT_GRAY = new Color(245, 245, 245);
	private static final Color MEDIUM_GRAY = new Color(189, 195, 199);
	private static final Color DARK_GRAY = new Color(52, 73, 94);
	private static final Color WHITE = Color.WHITE;
	
	public LoginWindow(Connection connection) {
		//// 2025-05-31 - inhered connection login ////
		this.connection = connection;
		initialize();
	}
	
	private void initialize() {
		loginWindow = new JFrame();
		loginWindow.setTitle("Employee Management System - Login");
		loginWindow.setBackground(WHITE);
		loginWindow.setFont(new Font("Segoe UI", Font.BOLD, 14));
		loginWindow.setBounds(100, 100, 800, 500);
		loginWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		loginWindow.setResizable(false);
		loginWindow.getContentPane().setLayout(null);
		
		// Create main panel with gradient background
		JPanel mainPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				
				// Create gradient background
				GradientPaint gradient = new GradientPaint(
					0, 0, new Color(52, 152, 219, 50),
					getWidth(), getHeight(), new Color(41, 128, 185, 30)
				);
				g2d.setPaint(gradient);
				g2d.fillRect(0, 0, getWidth(), getHeight());
				g2d.dispose();
			}
		};
		mainPanel.setLayout(null);
		mainPanel.setBounds(0, 0, 800, 500);
		loginWindow.getContentPane().add(mainPanel);
		
		// Create login card panel
		JPanel loginCard = new JPanel();
		loginCard.setLayout(null);
		loginCard.setBounds(200, 50, 400, 400);
		loginCard.setBackground(WHITE);
		loginCard.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(30, 30, 30, 30)
		));
		
		// Add shadow effect
		loginCard.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5),
			BorderFactory.createCompoundBorder(
				new LineBorder(MEDIUM_GRAY, 1, true),
				new EmptyBorder(25, 25, 25, 25)
			)
		));
		
		mainPanel.add(loginCard);
		
		// Title
		JLabel titleLabel = new JLabel("Welcome Back!");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
		titleLabel.setForeground(DARK_GRAY);
		titleLabel.setBounds(0, 20, 350, 40);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loginCard.add(titleLabel);
		
		JLabel subtitleLabel = new JLabel("Sign in to your account");
		subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		subtitleLabel.setForeground(MEDIUM_GRAY);
		subtitleLabel.setBounds(0, 60, 350, 20);
		subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loginCard.add(subtitleLabel);
		
		// Username
		JLabel lblUserName = new JLabel("👤 Username");
		lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblUserName.setForeground(DARK_GRAY);		
		lblUserName.setBounds(25, 100, 350, 25);
		loginCard.add(lblUserName);
		
		usernameField = createModernTextField(25);
		usernameField.setBounds(25, 125, 350, 40);
		loginCard.add(usernameField);
		
		// Password
		JLabel lblPassword = new JLabel("🔒 Password");
		lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 14));
		lblPassword.setForeground(DARK_GRAY);
		lblPassword.setBounds(25, 175, 350, 25);
		loginCard.add(lblPassword);
		
		passwordField = createModernPasswordField(25);
		passwordField.setBounds(25, 200, 350, 40);
		loginCard.add(passwordField);
		
		// Error label
		errorLabel = new JLabel("");
		errorLabel.setForeground(DANGER_COLOR);
		errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		errorLabel.setBounds(25, 250, 350, 20);
		errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loginCard.add(errorLabel);
		
		// Login button
		JButton btnLogin = createModernButton("🚀 Sign In", SUCCESS_COLOR);
		btnLogin.setBounds(25, 280, 350, 45);
		btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
		loginCard.add(btnLogin);
		
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
		
		// Add decorative elements
		addDecorativeElements(mainPanel);
	}
	
	private JTextField createModernTextField(int columns) {
		JTextField textField = new JTextField(columns);
		textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		textField.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(10, 15, 10, 15)
		));
		textField.setBackground(WHITE);
		textField.setForeground(DARK_GRAY);
		
		// Add focus listener for modern effect
		textField.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent e) {
				textField.setBorder(BorderFactory.createCompoundBorder(
					new LineBorder(PRIMARY_COLOR, 2, true),
					new EmptyBorder(9, 14, 9, 14)
				));
			}
			
			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
				textField.setBorder(BorderFactory.createCompoundBorder(
					new LineBorder(MEDIUM_GRAY, 1, true),
					new EmptyBorder(10, 15, 10, 15)
				));
			}
		});
		
		return textField;
	}
	
	private JPasswordField createModernPasswordField(int columns) {
		JPasswordField passwordField = new JPasswordField(columns);
		passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		passwordField.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(10, 15, 10, 15)
		));
		passwordField.setBackground(WHITE);
		passwordField.setForeground(DARK_GRAY);
		
		// Add focus listener for modern effect
		passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent e) {
				passwordField.setBorder(BorderFactory.createCompoundBorder(
					new LineBorder(PRIMARY_COLOR, 2, true),
					new EmptyBorder(9, 14, 9, 14)
				));
			}
			
			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
				passwordField.setBorder(BorderFactory.createCompoundBorder(
					new LineBorder(MEDIUM_GRAY, 1, true),
					new EmptyBorder(10, 15, 10, 15)
				));
			}
		});
		
		return passwordField;
	}
	
	private JButton createModernButton(String text, Color backgroundColor) {
		JButton button = new JButton(text) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				if (getModel().isPressed()) {
					g2d.setColor(backgroundColor.darker());
				} else if (getModel().isRollover()) {
					g2d.setColor(backgroundColor.brighter());
				} else {
					g2d.setColor(backgroundColor);
				}
				
				g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2d.dispose();
				
				super.paintComponent(g);
			}
		};
		
		button.setFont(new Font("Segoe UI", Font.BOLD, 14));
		button.setForeground(WHITE);
		button.setBackground(backgroundColor);
		button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		
		return button;
	}
	
	private void addDecorativeElements(JPanel mainPanel) {
		// Add some decorative circles
		JPanel decorativePanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				// Draw decorative circles
				g2d.setColor(new Color(52, 152, 219, 20));
				g2d.fillOval(50, 50, 100, 100);
				g2d.fillOval(650, 350, 80, 80);
				g2d.fillOval(100, 400, 60, 60);
				
				g2d.dispose();
			}
		};
		decorativePanel.setOpaque(false);
		decorativePanel.setBounds(0, 0, 800, 500);
		mainPanel.add(decorativePanel);
	}

	private void performLogin() {
		try {
			String user_name = usernameField.getText();
			String pwd = new String(passwordField.getPassword());
			
			if(user_name.isEmpty() || pwd.isEmpty()) {
				errorLabel.setForeground(DANGER_COLOR);
				errorLabel.setText("⚠️ Please enter both username and password");
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
				// Show success message
				errorLabel.setForeground(SUCCESS_COLOR);
				errorLabel.setText("✅ Login successful! Redirecting...");
				
				// Close login window after a short delay
				Timer timer = new Timer(1000, e -> {
					loginWindow.dispose();
					
					// Set session login for user login
					sessionManager = SessionManager.getInstance();
					sessionManager.login(getUserId(), getUserName(), getUserRole());
					
					// Show main window
					mainWindow = new MainWindow();
					mainWindow.show();
				});
				timer.setRepeats(false);
				timer.start();
			} else if(count > 1) {
				errorLabel.setForeground(DANGER_COLOR);
				errorLabel.setText("❌ Duplicate username and password found");
			} else {
				errorLabel.setForeground(DANGER_COLOR);
				errorLabel.setText("❌ Invalid username or password");
			}
			rs.close();
			pst.close();
		} catch (Exception e1) {
			errorLabel.setForeground(DANGER_COLOR);
			errorLabel.setText("❌ Error: " + e1.getMessage());
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
