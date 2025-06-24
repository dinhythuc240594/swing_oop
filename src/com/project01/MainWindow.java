package com.project01;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.Statement;

import java.util.Objects;
import java.util.Vector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import javax.swing.*;

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
	
	public MainWindow() {
		
		System.out.println("run MainWindow");
		
		//// 2025-05-31 - initialize database and get instance login ////
		initDatabase();
		sessionManager = SessionManager.getInstance();
		
		// Load user's language preference
		loadUserLanguage();
		
		// create a frame for application 
		window = new JFrame();
		window.setTitle(getMessage("app.title") + " - " + getMessage("logged.in.as") + ": " + sessionManager.getUsername());
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.setSize(1000, 750);
		window.setLocationRelativeTo(null);
		
		createMenuBar();
		
		// create a main panel for application
		JPanel mainPanel = new JPanel(new BorderLayout());
		
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
		employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		employeeTable.setRowHeight(50);
		
		// create tab panel
		tabPanel = new JTabbedPane();

		System.out.println("run2");
		//// 2025-05-31 - display if user is admin ////
		newEmployeeButton = new JButton(getMessage("button.new.employee"));
		newEmployeeButton.setVisible(sessionManager.isAdmin());
		newEmployeeButton.addActionListener(e -> openNewEmployeeDialog());

		JPanel employeeButotnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		employeeButotnPanel.add(newEmployeeButton);
		employeeButotnPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		// Add search panel
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchField = new JTextField(20);
		searchFilter = new JComboBox<>(new String[]{
			getMessage("search.all"),
			getMessage("search.name"),
			getMessage("search.email"),
			getMessage("search.position"),
//			getMessage("search.department")
		});
		searchButton = new JButton(getMessage("search.button"));
		
		searchLabel = new JLabel(getMessage("search.label"));
		filterLabel = new JLabel(getMessage("search.filter"));
		
		searchPanel.add(searchLabel);
		searchPanel.add(searchField);
		searchPanel.add(filterLabel);
		searchPanel.add(searchFilter);
		searchPanel.add(searchButton);
		searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		searchButton.addActionListener(e -> searchEmployees(searchField.getText(), (String)searchFilter.getSelectedItem()));
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchEmployees(searchField.getText(), (String)searchFilter.getSelectedItem());
				}
			}
		});
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(employeeButotnPanel, BorderLayout.WEST);
		topPanel.add(searchPanel, BorderLayout.CENTER);
		
		JScrollPane scrollPanel = new JScrollPane(employeeTable);
		scrollPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 5, 5, 5), 
				BorderFactory.createLineBorder(Color.GRAY)));
		
		// add table to scroll pane
		JPanel employeePanel = new JPanel(new BorderLayout());
		employeePanel.add(topPanel, BorderLayout.NORTH);
		employeePanel.add(scrollPanel, BorderLayout.CENTER);
		
		taskManagementScreen = new TaskManagementScreen(connection);
		
		if(sessionManager.isAdmin()) {
			tabPanel.addTab(getMessage("tab.employees"), new ImageIcon(), employeePanel, getMessage("tab.employees.tooltip"));
		}
		
		caculatorSalaryScreen = new CaculatorSalaryScreen(connection);
		if(sessionManager.isManager()) {
			tabPanel.addTab(getMessage("tab.salary"), new ImageIcon(), caculatorSalaryScreen, getMessage("tab.salary.tooltip"));
		}
		
		tabPanel.addTab(getMessage("tab.tasks"), new ImageIcon(), taskManagementScreen, getMessage("tab.tasks.tooltip"));
		
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
		}
	}
	
	//// 2025-06-01 - create menu bar ////
	private void createMenuBar() {
		menuBar = new JMenuBar();
		
		// profile menu
		JMenu profileMenu = new JMenu("Profile");
		JMenuItem accountItem = new JMenuItem("Account");
		JMenuItem changePasswordItem = new JMenuItem("Change Password");
		JMenuItem logoutItem = new JMenuItem("Log out");
		
		if(sessionManager.isEmployee()) {
			accountItem.addActionListener(e -> accountDetail());
			changePasswordItem.addActionListener(e -> changePassword());		
			profileMenu.add(accountItem);
			profileMenu.add(changePasswordItem);
		}
		
		logoutItem.addActionListener(e -> logout());
		profileMenu.add(logoutItem);
		
		// setting menu
		JMenu settingsMenu = new JMenu("Settings");
		JMenuItem appearanceItem = new JMenuItem("Appearance");
		JMenuItem notificationItem = new JMenuItem("Notification");
		JMenuItem languageItem = new JMenuItem("Language");
		
		appearanceItem.addActionListener(e -> openAppearanceSettings());
		notificationItem.addActionListener(e -> openNotificationSettings());
		languageItem.addActionListener(e -> openLanguageSettings());

		settingsMenu.add(appearanceItem);
		settingsMenu.add(notificationItem);
		settingsMenu.add(languageItem);

		// help menu
		JMenu helpMenu = new JMenu("Help");
		JMenuItem aboutItem = new JMenuItem("About");
		JMenuItem contactItem = new JMenuItem("Contact Support");
		JMenuItem helpItem = new JMenuItem("Help Contents");
		
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
		// Update column headers
//		for (int i = 0; i < columns.length; i++) {
//			tableModel.setColumnIdentifiers(columns);
//		}
		
		//// 2025-05-29 - customize center the photo column ////
		employeeTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		employeeTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = new JLabel();
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setOpaque(true);
				if(value instanceof ImageIcon) {
					ImageIcon icon = (ImageIcon) value;
					Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
					label.setIcon(new ImageIcon(img));
				}
				
				if(isSelected) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				}else {
					label.setBackground(table.getBackground());
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
					
					JButton deleteButton = new JButton(getMessage("table.actions.button.delete"));
					deleteButton.setFocusPainted(false);
					deleteButton.setBorderPainted(true);
					deleteButton.setContentAreaFilled(true);
					deleteButton.setForeground(Color.RED);
					deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
					deleteButton.setHorizontalAlignment(SwingConstants.CENTER);
					panel.add(deleteButton);
					
					if(isSelected) {
						panel.setBackground(table.getSelectionBackground());
					}else {
						panel.setBackground(table.getBackground());
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
							getMessage("msg.delete.confirm"),
							getMessage("msg.delete.title"),
							JOptionPane.YES_NO_OPTION);
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
		connection = sqliteConnection.dbConnector();
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
