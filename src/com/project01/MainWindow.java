package com.project01;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import java.sql.Statement;

import java.util.Objects;
import java.util.Vector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;


public class MainWindow {

	private JFrame window;
	private JTable employeeTable;
	private DefaultTableModel tableModel;
	private JButton newEmployeeButton;

	private Connection connection;
	private static final String DEFAULT_AVATAR_PATH = "img/avatar.png";
	private HandlerImage hImage = new HandlerImage();
	private SessionManager sessionManager;
	private LoginWindow loginWindow;
	private JMenuBar menuBar;
	private JTabbedPane tabPanel;
	
	private TaskManagementScreen taskManagementScreen;
	private CaculatorSalaryScreen  caculatorSalaryScreen;
	
	// Add language-related fields
	private static final String DEFAULT_LANGUAGE = "English";
	private String currentLanguage = DEFAULT_LANGUAGE;
	private ResourceBundle messages;
	
	private JComboBox<String> searchFilter;
	private JButton searchButton;
	private JTextField searchField;
	private JLabel searchLabel;
	private JLabel filterLabel;
	
	// Modern color scheme
	private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
	private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
	private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
	private static final Color WARNING_COLOR = new Color(241, 196, 15);
	private static final Color DANGER_COLOR = new Color(231, 76, 60);
	private static final Color LIGHT_GRAY = new Color(245, 245, 245);
	private static final Color MEDIUM_GRAY = new Color(189, 195, 199);
	private static final Color DARK_GRAY = new Color(52, 73, 94);
	private static final Color WHITE = Color.WHITE;
	private static final Color TABLE_HEADER_BG = new Color(236, 240, 241);
	private static final Color TABLE_ALTERNATE_ROW = new Color(248, 249, 250);
	
	public MainWindow() {
		
		System.out.println("run MainWindow");
		
		//// 2025-05-31 - initialize database and get instance login ////
		initDatabase();
		sessionManager = SessionManager.getInstance();
		
		// Load user's language preference
		loadUserLanguage();
		
		// Set modern look and feel
		setupModernLookAndFeel();
		
		// create a frame for application 
		window = new JFrame();
		window.setTitle(getMessage("app.title") + " - " + getMessage("logged.in.as") + ": " + sessionManager.getUsername());
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.setSize(1200, 800);
		window.setLocationRelativeTo(null);
		window.setBackground(WHITE);
		
		createMenuBar();
		
		// create a main panel for application
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(WHITE);
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		
		String[] columns = {
				getMessage("table.id"), 
				getMessage("table.photo"), 
				getMessage("table.firstname"), 
				getMessage("table.lastname"), 
				getMessage("table.email"), 
				getMessage("table.phone"), 
				getMessage("table.position"), 
				getMessage("table.hiredate"), 
				getMessage("table.actions")
		};

		System.out.println("run1");
		
		//// 2025-05-31 - customize table model for employees table view ////
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// 2025-05-31 - only allow editing of action column for admin users 
				return column == 8 && sessionManager.isAdmin();
			}
			
			@Override
			public Class<?> getColumnClass(int column){
				if(column == 1) {
					return ImageIcon.class;
				}	
				return String.class;
			}
		};
		employeeTable = new JTable(tableModel);
		setupModernTable();
		
		// create tab panel
		tabPanel = new JTabbedPane();
		tabPanel.setBackground(WHITE);
		tabPanel.setForeground(DARK_GRAY);
		tabPanel.setFont(new Font("Segoe UI", Font.BOLD, 14));

		System.out.println("run2");
		//// 2025-05-31 - display if user is admin ////
		newEmployeeButton = createModernButton(getMessage("button.new.employee"), PRIMARY_COLOR, 150, 30);
		newEmployeeButton.setVisible(sessionManager.isAdmin());
		newEmployeeButton.addActionListener(e -> openNewEmployeeDialog());

		JPanel employeeButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		employeeButtonPanel.setBackground(WHITE);
		employeeButtonPanel.setBorder(new EmptyBorder(15, 0, 10, 0));
		employeeButtonPanel.add(newEmployeeButton);
		
		// Add search panel
		JPanel searchPanel = createModernSearchPanel();
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(WHITE);
		topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
		topPanel.add(employeeButtonPanel, BorderLayout.WEST);
		topPanel.add(searchPanel, BorderLayout.CENTER);
		
		JScrollPane scrollPanel = new JScrollPane(employeeTable);
		scrollPanel.setBorder(BorderFactory.createCompoundBorder(
				new LineBorder(MEDIUM_GRAY, 1, true),
				new EmptyBorder(10, 10, 10, 10)));
		scrollPanel.getViewport().setBackground(WHITE);
		
		// add table to scroll pane
		JPanel employeePanel = new JPanel(new BorderLayout());
		employeePanel.setBackground(WHITE);
		employeePanel.add(topPanel, BorderLayout.NORTH);
		employeePanel.add(scrollPanel, BorderLayout.CENTER);
		
		taskManagementScreen = new TaskManagementScreen(connection);
		
		if(sessionManager.isAdmin()) {
			tabPanel.addTab(getMessage("tab.employees"), createTabIcon("👥"), employeePanel, getMessage("tab.employees.tooltip"));
		}
		
		if(sessionManager.isManager()) {
			caculatorSalaryScreen = new CaculatorSalaryScreen(connection);
			tabPanel.addTab(getMessage("tab.salary"), createTabIcon("💰"), caculatorSalaryScreen, getMessage("tab.salary.tooltip"));
		}
		
		tabPanel.addTab(getMessage("tab.tasks"), createTabIcon("📋"), taskManagementScreen, getMessage("tab.tasks.tooltip"));
		
		mainPanel.add(tabPanel, BorderLayout.CENTER);
		
		System.out.println("run3");
		// // load employee data
		loadEmployeeData();
		
		// add main panel to window
		window.add(mainPanel);
		
		// // Update all UI text after initialization
		updateUIText();
		System.out.println("run4");
	}
	
	private void setupModernLookAndFeel() {
		// Set modern UI properties
		UIManager.put("Button.arc", 8);
		UIManager.put("Component.arc", 8);
		UIManager.put("TextComponent.arc", 8);
		UIManager.put("ComboBox.arc", 8);
		UIManager.put("Table.arc", 8);
		UIManager.put("TableHeader.arc", 8);
	}
	
	private void setupModernTable() {
		employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		employeeTable.setRowHeight(60);
		employeeTable.setShowGrid(false);
		employeeTable.setIntercellSpacing(new Dimension(0, 0));
		employeeTable.setBackground(WHITE);
		employeeTable.setSelectionBackground(new Color(52, 152, 219, 30));
		employeeTable.setSelectionForeground(DARK_GRAY);
		employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		
		// Modern table header
		JTableHeader header = employeeTable.getTableHeader();
		header.setBackground(TABLE_HEADER_BG);
		header.setForeground(DARK_GRAY);
		header.setFont(new Font("Segoe UI", Font.BOLD, 13));
		header.setBorder(new MatteBorder(0, 0, 2, 0, MEDIUM_GRAY));
	}
	
	private JPanel createModernSearchPanel() {
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		searchPanel.setBackground(WHITE);
//		searchPanel.setBorder(BorderFactory.createCompoundBorder(
//			new LineBorder(MEDIUM_GRAY, 1, true),
//			new EmptyBorder(15, 15, 15, 15)
//		));
		searchPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		
		// Search field
		searchField = createModernTextField(20);
		searchField.setToolTipText("Enter search term...");
		
		// Search filter
		searchFilter = createModernComboBox(new String[]{
			getMessage("search.all"),
			getMessage("search.name"),
			getMessage("search.email"),
			getMessage("search.position"),
		});
		
		// Search button
		searchButton = createModernButton(getMessage("search.button"), PRIMARY_COLOR, 100, 30);
		searchButton.setIcon(new ImageIcon("🔍"));
		
		// Labels
		searchLabel = new JLabel(getMessage("search.label"));
		searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		searchLabel.setForeground(DARK_GRAY);
		
		filterLabel = new JLabel(getMessage("search.filter"));
		filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		filterLabel.setForeground(DARK_GRAY);
		
		searchPanel.add(searchLabel);
		searchPanel.add(searchField);
		searchPanel.add(filterLabel);
		searchPanel.add(searchFilter);
		searchPanel.add(searchButton);
		
		// Add event listeners
		searchButton.addActionListener(e -> searchEmployees(searchField.getText(), (String)searchFilter.getSelectedItem()));
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchEmployees(searchField.getText(), (String)searchFilter.getSelectedItem());
				}
			}
		});
		
		return searchPanel;
	}
	
	private JTextField createModernTextField(int columns) {
		JTextField textField = new JTextField(columns);
		textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		textField.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(8, 12, 8, 12)
		));
		textField.setBackground(WHITE);
		textField.setForeground(DARK_GRAY);
		
		// Add focus listener for modern effect
		textField.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent e) {
				textField.setBorder(BorderFactory.createCompoundBorder(
					new LineBorder(PRIMARY_COLOR, 2, true),
					new EmptyBorder(7, 11, 7, 11)
				));
			}
			
			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
				textField.setBorder(BorderFactory.createCompoundBorder(
					new LineBorder(MEDIUM_GRAY, 1, true),
					new EmptyBorder(8, 12, 8, 12)
				));
			}
		});
		
		return textField;
	}
	
	private JComboBox<String> createModernComboBox(String[] items) {
		JComboBox<String> comboBox = new JComboBox<>(items);
		comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		comboBox.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(8, 12, 8, 12)
		));
		comboBox.setBackground(WHITE);
		comboBox.setForeground(DARK_GRAY);
		
		return comboBox;
	}
	
	private JButton createModernButton(String text, Color backgroundColor, int Width, int Height) {
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
				
				g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				g2d.dispose();
				
				super.paintComponent(g);
			}
		};
		
		button.setFont(new Font("Segoe UI", Font.BOLD, 14));
		button.setForeground(WHITE);
		button.setBackground(backgroundColor);
		button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setPreferredSize(new Dimension(Width, Height));
		
		return button;
	}
	
	private ImageIcon createTabIcon(String emoji) {
		// Create a simple icon with emoji
		JLabel label = new JLabel(emoji);
		label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
		label.setPreferredSize(new Dimension(20, 20));
		
		// Convert to ImageIcon
		BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		label.paint(g2d);
		g2d.dispose();
		
		return new ImageIcon(image);
	}
	
	private void loadEmployeeData() {
		try {
			// clear existing data
			tableModel.setRowCount(0);
			
			//// 2025-05-30 - load employees with users are admin and manage ////
			// query to join employees and users table
			String query = "SELECT e.id, e.photo, e.first_name, e.last_name, e.email, e.phone, e.position, e.hire_date " +
					"FROM employees e "
					+ "JOIN users u ON e.user_id = u.id";

			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			
			while(rs.next()) {
				Vector<Object> row = new Vector<>();

				row.add(rs.getInt("id"));
				// load avatar if exists
				ImageIcon icon;
				byte[] bytePhoto;
				bytePhoto = rs.getBytes("photo"); 
				if(bytePhoto != null && bytePhoto.length > 0) {
					icon = new ImageIcon(bytePhoto);
				} else {
					
					bytePhoto = hImage.fileImage(DEFAULT_AVATAR_PATH, 40, 40);
					if(bytePhoto != null) {
						icon = new ImageIcon(bytePhoto);
					} else {
						icon = new ImageIcon(Objects.requireNonNull(this.getClass().getResource(DEFAULT_AVATAR_PATH)));
					}
					
				}
				row.add(icon);
				
				row.add(rs.getString("first_name"));
				row.add(rs.getString("last_name"));
				row.add(rs.getString("email"));
				row.add(rs.getString("phone"));
				row.add(rs.getString("position"));
				row.add(rs.getString("hire_date"));
				row.add("Delete");
				
				tableModel.addRow(row);
			}
			
			rs.close();
			st.close();
		}catch (SQLException e) {
			JOptionPane.showMessageDialog(window, "Error loading employee data: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void openNewEmployeeDialog() {
		EmployeeDialog dialog = new EmployeeDialog(window, connection);
		dialog.setVisible(true);
		if(sessionManager.isAdmin()) {
			if(dialog.isEmployeeAdded()) {
				// refresh the table
				loadEmployeeData();
			}	
		} else if(sessionManager.isLoggedIn()) {
			loadEmployeeData();
		} else {
			JOptionPane.showMessageDialog(window, 
					"You don't have permission to add new employees", 
					"Pemission Denied",
					JOptionPane.WARNING_MESSAGE);
		}
		
	}
	
	private void openEmployeeDetails(int row) {
		int employeeId = (int) tableModel.getValueAt(row, 0);
		EmployeeDialog dialog = new EmployeeDialog(window, connection, employeeId);
		dialog.setVisible(true);
		if(dialog.isEmployeeUpdated()) {
			// refresh the table
			loadEmployeeData();
		}
	}
	
	private void deleteEmployee(int row) {
		try {
			int employeeId = (int) tableModel.getValueAt(row, 0);
			
			// first delete the user record
			String userQuery = "DELETE FROM users WHERE id = (SELECT user_id FROM employees WHERE id = ?) ";
			PreparedStatement userStmt = connection.prepareStatement(userQuery);
			userStmt.setInt(1, employeeId);
			userStmt.executeUpdate();
			
			// then delete the employee record
			String employeeQuery = "DELETE FROM employees WHERE id = ? ";
			PreparedStatement employeeStmt = connection.prepareStatement(employeeQuery);
			employeeStmt.setInt(1, employeeId);
			employeeStmt.executeUpdate();
			
			// refresh the table
			loadEmployeeData();
			
			JOptionPane.showMessageDialog(window, "Delete employee data successfully");
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(window, "Error delete employee data: " + e.getMessage());
			System.out.println("Error delete employee data: " + e.getMessage());
		}
	}
	
	//// 2025-06-01 - create menu bar ////
	private void createMenuBar() {
		menuBar = new JMenuBar();
		menuBar.setBackground(TABLE_HEADER_BG);
		menuBar.setBorder(new MatteBorder(0, 0, 1, 0, MEDIUM_GRAY));
		
		// profile menu
		JMenu profileMenu = new JMenu("👤 Profile");
		profileMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));
		profileMenu.setForeground(DARK_GRAY);
		
		JMenuItem accountItem = new JMenuItem("👤 Account");
		JMenuItem changePasswordItem = new JMenuItem("🔒 Change Password");
		JMenuItem logoutItem = new JMenuItem("🚪 Log out");
		
		// Style menu items
		styleMenuItem(accountItem);
		styleMenuItem(changePasswordItem);
		styleMenuItem(logoutItem);
		
		if(sessionManager.isEmployee()) {
			accountItem.addActionListener(e -> accountDetail());
			changePasswordItem.addActionListener(e -> changePassword());		
			profileMenu.add(accountItem);
			profileMenu.add(changePasswordItem);
		}
		
		logoutItem.addActionListener(e -> logout());
		profileMenu.add(logoutItem);
		
		// setting menu
		JMenu settingsMenu = new JMenu("⚙️ Settings");
		settingsMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));
		settingsMenu.setForeground(DARK_GRAY);
		
		JMenuItem appearanceItem = new JMenuItem("🎨 Appearance");
		JMenuItem notificationItem = new JMenuItem("🔔 Notification");
		JMenuItem languageItem = new JMenuItem("🌐 Language");
		
		styleMenuItem(appearanceItem);
		styleMenuItem(notificationItem);
		styleMenuItem(languageItem);
		
		appearanceItem.addActionListener(e -> openAppearanceSettings());
		notificationItem.addActionListener(e -> openNotificationSettings());
		languageItem.addActionListener(e -> openLanguageSettings());

		settingsMenu.add(appearanceItem);
		settingsMenu.add(notificationItem);
		settingsMenu.add(languageItem);

		// help menu
		JMenu helpMenu = new JMenu("❓ Help");
		helpMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));
		helpMenu.setForeground(DARK_GRAY);
		
		JMenuItem aboutItem = new JMenuItem("ℹ️ About");
		JMenuItem contactItem = new JMenuItem("📞 Contact Support");
		JMenuItem helpItem = new JMenuItem("📖 Help Contents");
		
		styleMenuItem(aboutItem);
		styleMenuItem(contactItem);
		styleMenuItem(helpItem);
		
		aboutItem.addActionListener(e -> showAboutDialog());
		contactItem.addActionListener(e -> showContactSupports());
		helpItem.addActionListener(e -> showHelpContents());
		
		helpMenu.add(aboutItem);
		helpMenu.add(contactItem);
		helpMenu.add(helpItem);
		
		menuBar.add(profileMenu);
		menuBar.add(settingsMenu);
		menuBar.add(helpMenu);
		
		// set the menu bar
		window.setJMenuBar(menuBar);
		
	}
	
	private void styleMenuItem(JMenuItem menuItem) {
		menuItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		menuItem.setForeground(DARK_GRAY);
		menuItem.setBackground(WHITE);
		menuItem.setBorder(new EmptyBorder(5, 10, 5, 10));
	}
	
	private int loadEmployeeId(int user_id) {
		try {
			int id;
			String query = "SELECT id " +
					"FROM employees e " +
					"WHERE e.user_id = ?";
			
			PreparedStatement prstmt = connection.prepareStatement(query);
			prstmt.setInt(1, user_id);
			ResultSet rs = prstmt.executeQuery();
			if(rs.next()) {
				id = rs.getInt("id");
				return id;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private void accountDetail() {
		int userId = sessionManager.getUserId();
		int employeeId = loadEmployeeId(userId);
		try {
			EmployeeDialog dialog = new EmployeeDialog(window, connection, employeeId);
			dialog.setVisible(true);
			if(dialog.isEmployeeUpdated()) {
				loadEmployeeData();
			}	
		} catch (Exception e) {
			JOptionPane.showMessageDialog(window, "Error loading profile: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	//// 2025-06-04 - add function for menu bar ////
	private void changePassword() {
		
		JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
		JPasswordField currentPass = new JPasswordField();
		JPasswordField newPass = new JPasswordField();
		JPasswordField confirmPass = new JPasswordField();
		
		panel.add(new JLabel(getMessage("menu.change.password.current")));
		panel.add(currentPass);
		panel.add(new JLabel(getMessage("menu.change.password.new")));
		panel.add(newPass);
		panel.add(new JLabel(getMessage("menu.change.password.confirm")));
		panel.add(confirmPass);
		
		int result = JOptionPane.showConfirmDialog(window, panel, getMessage("menu.change.password"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if(result == JOptionPane.OK_OPTION) {
			String newPassord = new String(newPass.getPassword());
			String cnfPassword = new String(confirmPass.getPassword());
			System.out.println(newPassord + " " + cnfPassword);

			try {

				if(!newPassord.equals(cnfPassword)) {
					JOptionPane.showMessageDialog(window, "Password and confirm password not matched.");
					changePassword();
					return;
				}
				
				if(newPassord.length() <= 6) {
					JOptionPane.showMessageDialog(window, "Password more than 6 character.");
					changePassword();
					return;
				}
				
				PreparedStatement stmt;
				String usersQuery;
				
				// update user name, password and role if changed
				usersQuery = "UPDATE users SET password = ? WHERE id = ?";
				stmt = connection.prepareStatement(usersQuery);
				stmt.setString(1, newPassord);
				stmt.setInt(2, sessionManager.getUserId());
				stmt.executeUpdate();
				JOptionPane.showMessageDialog(window, "Change password success.");
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void openAppearanceSettings() {
		JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
		JComboBox<String> themeCombo = new JComboBox<>(new String[] {"Light", "Dark", "System"});
		JComboBox<String> fontSizeCombo = new JComboBox<>(new String[] {"Small", "Medium", "Large"});
		
		// Set current values
		themeCombo.setSelectedItem("Light"); // Default theme
		fontSizeCombo.setSelectedItem("Medium"); // Default font size
		
		panel.add(new JLabel(getMessage("menu.appearance.theme")));
		panel.add(themeCombo);
		panel.add(new JLabel(getMessage("menu.appearance.font_size")));
		panel.add(fontSizeCombo);
		
		int result = JOptionPane.showConfirmDialog(window, panel, getMessage("menu.appearance"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if(result == JOptionPane.OK_OPTION) {
			// Apply theme
			String selectedTheme = (String) themeCombo.getSelectedItem();
			applyTheme(selectedTheme);
			
			// Apply font size
			String selectedFontSize = (String) fontSizeCombo.getSelectedItem();
			applyFontSize(selectedFontSize);
			
			// Save settings
			saveAppearanceSettings(selectedTheme, selectedFontSize);
		}
	}
	
	private void applyTheme(String theme) {
		try {
			switch(theme) {
				case "Dark":
					UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
					UIManager.put("Panel.background", new Color(43, 43, 43));
					UIManager.put("Panel.foreground", Color.WHITE);
					UIManager.put("Table.background", new Color(43, 43, 43));
					UIManager.put("Table.foreground", Color.WHITE);
					UIManager.put("TableHeader.background", new Color(60, 60, 60));
					UIManager.put("TableHeader.foreground", Color.WHITE);
					break;
				case "Light":
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UIManager.put("Panel.background", Color.WHITE);
					UIManager.put("Panel.foreground", Color.BLACK);
					UIManager.put("Table.background", Color.WHITE);
					UIManager.put("Table.foreground", Color.BLACK);
					UIManager.put("TableHeader.background", new Color(240, 240, 240));
					UIManager.put("TableHeader.foreground", Color.BLACK);
					break;
				case "System":
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					break;
			}
			
			// Update all components
			SwingUtilities.updateComponentTreeUI(window);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(window, "Error applying theme: " + e.getMessage());
		}
	}
	
	private void applyFontSize(String size) {
		int fontSize;
		switch(size) {
			case "Small":
				fontSize = 12;
				break;
			case "Large":
				fontSize = 16;
				break;
			default: // Medium
				fontSize = 14;
		}
		
		// Set font for all components
		Font newFont = new Font("Dialog", Font.PLAIN, fontSize);
		UIManager.put("Button.font", newFont);
		UIManager.put("Label.font", newFont);
		UIManager.put("TextField.font", newFont);
		UIManager.put("Table.font", newFont);
		UIManager.put("TableHeader.font", newFont);
		UIManager.put("Menu.font", newFont);
		UIManager.put("MenuItem.font", newFont);
		
		// Update all components
		SwingUtilities.updateComponentTreeUI(window);
	}
	
	private void saveAppearanceSettings(String theme, String fontSize) {
		try {
			String query = "UPDATE users SET theme = ?, font_size = ? WHERE id = ?";
			PreparedStatement pstmt = connection.prepareStatement(query);
			pstmt.setString(1, theme);
			pstmt.setString(2, fontSize);
			pstmt.setInt(3, sessionManager.getUserId());
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void openNotificationSettings() {

		JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
		JCheckBox emailNotif = new JCheckBox(getMessage("menu.notification.email"));
		JCheckBox systemNoti = new JCheckBox(getMessage("menu.notification.system"));
		JCheckBox updateNotif = new JCheckBox(getMessage("menu.notification.update"));
		
		panel.add(emailNotif);
		panel.add(systemNoti);
		panel.add(updateNotif);
		
		int result = JOptionPane.showConfirmDialog(window, panel, getMessage("menu.notification"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if(result == JOptionPane.OK_OPTION) {
			JOptionPane.showMessageDialog(window, "Notification settings to be implemented");
		}		
	}
	
	private void openLanguageSettings() {
		JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
		JComboBox<String> langCombo = new JComboBox<>(new String[] {"English", "Vietnamese"});
		
		// Set current language
		langCombo.setSelectedItem(currentLanguage);
		
		panel.add(new JLabel(getMessage("menu.help.language.lable")));
		panel.add(langCombo);
		
		int result = JOptionPane.showConfirmDialog(window, panel, getMessage("menu.help.language.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if(result == JOptionPane.OK_OPTION) {
			String selectedLanguage = (String) langCombo.getSelectedItem();
			if (!selectedLanguage.equals(currentLanguage)) {
				changeLanguage(selectedLanguage);
			}
		}		
	}
	
	private void changeLanguage(String language) {
		try {
			// Update language in database
			String query = "UPDATE users SET language = ? WHERE id = ?";
			PreparedStatement pstmt = connection.prepareStatement(query);
			pstmt.setString(1, language);
			pstmt.setInt(2, sessionManager.getUserId());
			pstmt.executeUpdate();
			pstmt.close();
			
			// Update current language
			currentLanguage = language;
			
			// Load language resource bundle
			Locale locale = language.equals("Vietnamese") ? Locale.of("vi") : Locale.of("en");
			messages = ResourceBundle.getBundle("messages", locale);
			
			// Update UI text
			updateUIText();
			
			// Update task management screen language
			if (taskManagementScreen != null) {
				taskManagementScreen.setLanguage(locale);
			}
			
			// Update calculator salary screen language
			if (caculatorSalaryScreen != null) {
				caculatorSalaryScreen.setLanguage(locale);
			}
			
			JOptionPane.showMessageDialog(window, 
				language.equals("Vietnamese") ? "Ngôn ngữ đã được thay đổi thành công" : "Language changed successfully");
			
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(window, 
				language.equals("Vietnamese") ? "Lỗi khi thay đổi ngôn ngữ" : "Error changing language");
			e.printStackTrace();
		}
	}
	
	private void updateUIText() {
		// Update window title
		window.setTitle(getMessage("app.title") + " - " + getMessage("logged.in.as") + ": " + sessionManager.getUsername());
		
		// Update menu items
		updateMenuText();
		
		if(!sessionManager.isEmployee()) {
			// Update table headers
			updateTableHeaders();
			
			// Update search components
			updateSearchFilter();
			
			// Update other UI components
			updateOtherComponents();	
		}

		// Refresh the UI
		SwingUtilities.updateComponentTreeUI(window);
	}
	
	private void updateMenuText() {
		// Update Profile menu
		JMenu profileMenu = menuBar.getMenu(0);
		profileMenu.setText(getMessage("menu.profile"));
		
		// Update Profile menu items
		for (int i = 0; i < profileMenu.getItemCount(); i++) {
			JMenuItem item = profileMenu.getItem(i);
			if (item != null) {
				if (item.getText().equals("Account") || item.getText().equals("Tài khoản")) {
					item.setText(getMessage("menu.account"));
				} else if (item.getText().equals("Change Password") || item.getText().equals("Đổi mật khẩu")) {
					item.setText(getMessage("menu.change.password"));
				} else if (item.getText().equals("Log out") || item.getText().equals("Đăng xuất")) {
					item.setText(getMessage("menu.logout"));
				}
			}
		}

		// Update Settings menu
		JMenu settingsMenu = menuBar.getMenu(1);
		settingsMenu.setText(getMessage("menu.settings"));
		settingsMenu.getItem(0).setText(getMessage("menu.appearance"));
		settingsMenu.getItem(1).setText(getMessage("menu.notification"));
		settingsMenu.getItem(2).setText(getMessage("menu.language"));
		
		// Update Help menu
		JMenu helpMenu = menuBar.getMenu(2);
		helpMenu.setText(getMessage("menu.help"));
		helpMenu.getItem(0).setText(getMessage("menu.about"));
		helpMenu.getItem(1).setText(getMessage("menu.contact"));
		helpMenu.getItem(2).setText(getMessage("menu.help.contents"));
	}
	
	private void updateTableHeaders() {
		String[] columns = {
			getMessage("table.id"),
			getMessage("table.photo"),
			getMessage("table.firstname"),
			getMessage("table.lastname"),
			getMessage("table.email"),
			getMessage("table.phone"),
			getMessage("table.position"),
			getMessage("table.hiredate"),
			getMessage("table.actions")
		};
		
		tableModel.setColumnIdentifiers(columns);
		
		//// 2025-05-29 - customize center the photo column ////
		employeeTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		employeeTable.getColumnModel().getColumn(1).setPreferredWidth(60);
		employeeTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = new JLabel();
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setOpaque(true);
				label.setBorder(new EmptyBorder(5, 5, 5, 5));
				
				if(value instanceof ImageIcon) {
					ImageIcon icon = (ImageIcon) value;
					Image img = icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
					label.setIcon(new ImageIcon(img));
				}
				
				if(isSelected) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				}else {
					label.setBackground(row % 2 == 0 ? WHITE : TABLE_ALTERNATE_ROW);
					label.setForeground(table.getForeground());
				}
				
				return label;
			}
		});
		
		if(sessionManager.isAdmin()) {
			//// 2025-05-31 - customize the delete button column ////
			employeeTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
					panel.setOpaque(true);		
					
					JButton deleteButton = createModernButton(getMessage("table.actions.button.delete"), DANGER_COLOR, 100, 40);
					deleteButton.setPreferredSize(new Dimension(80, 30));
					panel.add(deleteButton);
					
					if(isSelected) {
						panel.setBackground(table.getSelectionBackground());
					}else {
						panel.setBackground(row % 2 == 0 ? WHITE : TABLE_ALTERNATE_ROW);
					}
					
					return panel;
				}
			});	
		} else {
			//// 2025-05-31 - customize the table hide id and delete button column ////
			employeeTable.getColumnModel().getColumn(0).setWidth(0);
			employeeTable.getColumnModel().getColumn(0).setMinWidth(0);
			employeeTable.getColumnModel().getColumn(0).setMaxWidth(0);
			
			employeeTable.getColumnModel().getColumn(8).setWidth(0);
			employeeTable.getColumnModel().getColumn(8).setMinWidth(0);
			employeeTable.getColumnModel().getColumn(8).setMaxWidth(0);
		}
	
		//// 2025-05-30 - add mouse listener for row selection and delete button ////
		employeeTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = employeeTable.getColumnModel().getColumnIndexAtX(e.getX());
				int row = e.getY() / employeeTable.getRowHeight();
				
				if (row < employeeTable.getRowCount() && row >= 0 && column < employeeTable.getColumnCount() && column >= 0) {
					if(column == 8 && sessionManager.isAdmin()) {
						int confirm = JOptionPane.showConfirmDialog(window, 
							"<html><div style='text-align: center;'>" +
							"<p style='font-size: 16px; color: #e74c3c;'>⚠️ Delete Employee</p>" +
							"<p style='font-size: 14px; color: #555;'>Are you sure you want to delete this employee?</p>" +
							"<p style='font-size: 12px; color: #777;'>This action cannot be undone.</p>" +
							"</div></html>",
							"Confirm Delete",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
						if(confirm == JOptionPane.YES_OPTION) {
							deleteEmployee(row);
						}	
					} else if(e.getClickCount() == 2) {
						if(row != -1) {
							openEmployeeDetails(row);
						}
					}
				}
			
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				employeeTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}
	
	private void updateOtherComponents() {
		// Update new employee button
		newEmployeeButton.setText(getMessage("button.new.employee"));
		
		// Update tab names
		tabPanel.setTitleAt(0, getMessage("tab.employees"));
		tabPanel.setTitleAt(1, getMessage("tab.tasks"));
	}

	private String getMessage(String key) {
		try {
			return messages.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	
	private void showAboutDialog() {
		String aboutMessage = "Employee Management System\n" +
								"Version 1.0.0 \n\n" +
								"A comprehensive solution for managing employee information.\n" + 
								"@ 2025 TI Name\n\n" + 
								"Developed with Java Swing";
		JOptionPane.showMessageDialog(window, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void showHelpContents() {
		String helpMessage = "Help contents\n\n" +
								"1. Managing Employees\n" +
								"   - View employee list\n" + 
								"   - Add employee list\n" + 
								"   - Edit employee list\n" + 
								"   - Delete employee list\n\n" + 
								"2. Profile Management\n" +
								"   - View your profile\n" + 
								"   - Edit your profile\n" + 
								"   - Change Password\n\n" + 
								"3. Settings\n" +
								"   - Customize appearance\n" + 
								"   - Configure notifications\n" + 
								"   - Change langauge\n\n" + 
								"For more help, please contact support.";
		JOptionPane.showMessageDialog(window, helpMessage, "Help Contents", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void showContactSupports() {
		String contactMessage = "Content Support\n\n" +
								"Email: admin@company.com\n\n" +
								"Phone: 123456789\n" +  
								"For urgent issues, please call our support line.";
		JOptionPane.showMessageDialog(window, contactMessage, "Contact Support", JOptionPane.INFORMATION_MESSAGE);
	}
	
	//// 2025-05-31 - logout -> clear session and open new login window ////
	private void logout() {
		sessionManager.logout();
		window.dispose();
		loginWindow = new LoginWindow(connection);
		loginWindow.show();
	}
	
	public void show() {
		window.setVisible(true);
	}
	
	public void hide() {
		window.setVisible(false);
	}
	
	private void initDatabase() {
		connection = SqlServerConnection.dbConnector();
	}
	
	private void searchEmployees(String searchText, String filter) {
		try {
			tableModel.setRowCount(0);
			String query = "SELECT e.id, e.photo, e.first_name, e.last_name, e.email, e.phone, e.position, e.hire_date " +
					"FROM employees e " +
					"JOIN users u ON e.user_id = u.id WHERE 1=1";
			
			if (!searchText.isEmpty()) {
				switch (filter) {
					case "Name":
						query += " AND (e.first_name LIKE ? OR e.last_name LIKE ?)";
						break;
					case "Email":
						query += " AND e.email LIKE ?";
						break;
					case "Position":
						query += " AND e.position LIKE ?";
						break;
					case "Department":
						query += " AND e.department LIKE ?";
						break;
					default:
						query += " AND (e.first_name LIKE ? OR e.last_name LIKE ? OR e.email LIKE ? OR e.position LIKE ? OR e.department LIKE ?)";
				}
			}
			
			PreparedStatement stmt = connection.prepareStatement(query);
			
			if (!searchText.isEmpty()) {
				String searchPattern = "%" + searchText + "%";
				switch (filter) {
					case "Name":
						stmt.setString(1, searchPattern);
						stmt.setString(2, searchPattern);
						break;
					case "Email":
						stmt.setString(1, searchPattern);
						break;
					case "Position":
						stmt.setString(1, searchPattern);
						break;
					case "Department":
						stmt.setString(1, searchPattern);
						break;
					default:
						stmt.setString(1, searchPattern);
						stmt.setString(2, searchPattern);
						stmt.setString(3, searchPattern);
						stmt.setString(4, searchPattern);
						stmt.setString(5, searchPattern);
				}
			}
			
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				Vector<Object> row = new Vector<>();
				row.add(rs.getInt("id"));
				
				ImageIcon icon;
				byte[] bytePhoto = rs.getBytes("photo");
				if(bytePhoto != null && bytePhoto.length > 0) {
					icon = new ImageIcon(bytePhoto);
				} else {
					bytePhoto = hImage.fileImage(DEFAULT_AVATAR_PATH, 40, 40);
					if(bytePhoto != null) {
						icon = new ImageIcon(bytePhoto);
					} else {
						icon = new ImageIcon(Objects.requireNonNull(this.getClass().getResource(DEFAULT_AVATAR_PATH)));
					}
				}
				row.add(icon);
				
				row.add(rs.getString("first_name"));
				row.add(rs.getString("last_name"));
				row.add(rs.getString("email"));
				row.add(rs.getString("phone"));
				row.add(rs.getString("position"));
				row.add(rs.getString("hire_date"));
				row.add("Delete");
				
				tableModel.addRow(row);
			}
			
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(window, "Error searching employees: " + e.getMessage());
		}
	}
	
	private void loadUserLanguage() {
		try {
			String query = "SELECT language FROM users WHERE id = ?";
			PreparedStatement pstmt = connection.prepareStatement(query);
			pstmt.setInt(1, sessionManager.getUserId());
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				currentLanguage = rs.getString("language");
				Locale locale = currentLanguage.equals("Vietnamese") ? Locale.of("vi") : Locale.of("en");
				messages = ResourceBundle.getBundle("messages", locale);
			} else {
				// Default to English if no language preference is set
				currentLanguage = DEFAULT_LANGUAGE;
				messages = ResourceBundle.getBundle("messages", Locale.of("en"));
			}
			
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			// Default to English if there's an error
			currentLanguage = DEFAULT_LANGUAGE;
			messages = ResourceBundle.getBundle("messages", Locale.of("en"));
		}
	}
	
	private void updateSearchFilter() {
		// Store the currently selected index
		int selectedIndex = searchFilter.getSelectedIndex();
		
		// Update the items
		searchFilter.removeAllItems();
		searchFilter.addItem(getMessage("search.all"));
		searchFilter.addItem(getMessage("search.name"));
		searchFilter.addItem(getMessage("search.email"));
		searchFilter.addItem(getMessage("search.position"));
//		searchFilter.addItem(getMessage("search.department"));
		
		// Restore the selected index
		if (selectedIndex >= 0 && selectedIndex < searchFilter.getItemCount()) {
			searchFilter.setSelectedIndex(selectedIndex);
		}
		
		// Update other search components
		searchLabel.setText(getMessage("search.label"));
		filterLabel.setText(getMessage("search.filter"));
		searchButton.setText(getMessage("search.button"));
	}
	
}
