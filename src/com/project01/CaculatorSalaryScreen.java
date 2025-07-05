package com.project01;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class CaculatorSalaryScreen extends JPanel {
	private ResourceBundle messages;
	private static final String DEFAULT_LANGUAGE = "English";
	private String currentLanguage = DEFAULT_LANGUAGE;
	private Connection connection;
	private SessionManager sessionManager;
	private DefaultTableModel tableModel;
	private JTable salaryTable;
	private static final String DEFAULT_AVATAR_PATH = "img/avatar.png";
	private HandlerImage hImage = new HandlerImage();
	private JButton viewDetailsButton;
	private JButton refreshButton;
	private JComboBox<String> searchFilter;
	private JButton searchButton;
	private JTextField searchField;
	private JLabel searchLabel;
	private JLabel filterLabel;
	private Locale localeInfo;
	
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
	
	public CaculatorSalaryScreen(Connection connection) {
		this.connection = connection;
		this.sessionManager = SessionManager.getInstance();
		this.messages = ResourceBundle.getBundle("messages", Locale.of("en"));
		
		// Set modern look and feel
		setupModernLookAndFeel();
		
		// Check if user has permission to view salary information
		if (!sessionManager.isManager() && !sessionManager.isAdmin()) {
			initNoPermissionComponents();
		} else {
			initComponents();
			loadSalaryEmployees();
		}
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
	
	public void setLanguage(Locale locale) {
		this.messages = ResourceBundle.getBundle("messages", locale);
		this.localeInfo = locale;
		updateUIText();
	}

	private void updateUIText() {
		updateTableHeaders();
		updateSearchFilter();
	}

	private void updateSearchFilter() {
		// Store the currently selected index
		int selectedIndex = searchFilter.getSelectedIndex();
		
		// Update the items
		searchFilter.removeAllItems();
		searchFilter.addItem(messages.getString("search.all"));
		searchFilter.addItem(messages.getString("search.name"));
		searchFilter.addItem(messages.getString("search.email"));
		searchFilter.addItem(messages.getString("search.position"));
		
		// Restore the selected index
		if (selectedIndex >= 0 && selectedIndex < searchFilter.getItemCount()) {
			searchFilter.setSelectedIndex(selectedIndex);
		}
		
		// Update other search components
		searchLabel.setText(messages.getString("search.label"));
		filterLabel.setText(messages.getString("search.filter"));
		searchButton.setText(messages.getString("search.button"));
		viewDetailsButton.setText(messages.getString("salary.button.view_detail"));
		refreshButton.setText(messages.getString("salary.button.refresh"));
	}
	
	private void initNoPermissionComponents() {
		setLayout(new BorderLayout());
		setBackground(WHITE);
		setBorder(new EmptyBorder(40, 40, 40, 40));
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(WHITE);
		centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// Modern access denied icon/emoji
		JLabel iconLabel = new JLabel("🚫");
		iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
		iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		iconLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
		
		JLabel titleLabel = new JLabel("Access Denied");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
		titleLabel.setForeground(DANGER_COLOR);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
		
		JLabel messageLabel = new JLabel(
			"<html><div style='text-align: center; max-width: 400px;'>" +
			"<p style='font-size: 16px; color: #555; margin: 10px 0;'>" +
			"Only managers can view salary information.</p>" +
			"<p style='font-size: 14px; color: #777; margin: 10px 0;'>" +
			"Please contact your manager if you need access to this feature.</p>" +
			"</div></html>"
		);
		messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		centerPanel.add(iconLabel);
		centerPanel.add(titleLabel);
		centerPanel.add(messageLabel);
		
		add(centerPanel, BorderLayout.CENTER);
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		setBackground(WHITE);
		setBorder(new EmptyBorder(20, 20, 20, 20));
		
		loadUserLanguage();

		String[] columns = {
				messages.getString("salary.column.id"),
				messages.getString("salary.column.photo"),
				messages.getString("salary.column.name"),
				messages.getString("salary.column.position"),
				messages.getString("salary.column.level"),
				messages.getString("salary.column.email"),
				messages.getString("salary.column.phone"),
				messages.getString("salary.column.identitynumber"),
				messages.getString("salary.column.hire_date"),
				messages.getString("salary.column.salary"),
				messages.getString("salary.column.salarycoefficient"),
				messages.getString("salary.column.experience_level"),
				messages.getString("salary.column.grosssalary"),
		};
		
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
			
			@Override
			public Class<?> getColumnClass(int column){
				if(column == 1) {
					return ImageIcon.class;
				}	
				return String.class;
			}
		};
		
		salaryTable = new JTable(tableModel);
		setupModernTable();
		
		// Create modern search panel
		JPanel searchPanel = createModernSearchPanel();
		
		// Create modern table panel
		JPanel tablePanel = createModernTablePanel();
		
		// Create modern control panel
		JPanel controlPanel = createModernControlPanel();
		
		// Layout components
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(WHITE);
		topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
		topPanel.add(searchPanel, BorderLayout.CENTER);
		
		add(topPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);

		// Add table selection listener
		salaryTable.getSelectionModel().addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting()) {
				int selectedRow = salaryTable.getSelectedRow();
				viewDetailsButton.setEnabled(selectedRow >= 0);
			}
		});
		updateUIText();
	}
	
	private void setupModernTable() {
		salaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		salaryTable.setRowHeight(60);
		salaryTable.setShowGrid(false);
		salaryTable.setIntercellSpacing(new Dimension(0, 0));
		salaryTable.setBackground(WHITE);
		salaryTable.setSelectionBackground(new Color(52, 152, 219, 30));
		salaryTable.setSelectionForeground(DARK_GRAY);
		salaryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		
		// Modern table header
		JTableHeader header = salaryTable.getTableHeader();
		header.setBackground(TABLE_HEADER_BG);
		header.setForeground(DARK_GRAY);
		header.setFont(new Font("Segoe UI", Font.BOLD, 13));
		header.setBorder(new MatteBorder(0, 0, 2, 0, MEDIUM_GRAY));
		
		// Set column widths
		salaryTable.getColumnModel().getColumn(0).setPreferredWidth(50); //ID
		salaryTable.getColumnModel().getColumn(1).setPreferredWidth(60); //PHOTO
		salaryTable.getColumnModel().getColumn(2).setPreferredWidth(180); // NAME
		salaryTable.getColumnModel().getColumn(3).setPreferredWidth(120); // POSITION
		salaryTable.getColumnModel().getColumn(4).setPreferredWidth(100); // LEVEL
		salaryTable.getColumnModel().getColumn(5).setPreferredWidth(200); // EMAIL
		salaryTable.getColumnModel().getColumn(6).setPreferredWidth(120); // PHONE
		salaryTable.getColumnModel().getColumn(7).setPreferredWidth(120); // IDENTITY
		salaryTable.getColumnModel().getColumn(8).setPreferredWidth(100); // HIRE DATE
		salaryTable.getColumnModel().getColumn(9).setPreferredWidth(100); // SALARY
		salaryTable.getColumnModel().getColumn(10).setPreferredWidth(120); // SALARY COEFFICIENT
		salaryTable.getColumnModel().getColumn(11).setPreferredWidth(120); // EXPERIENCE LEVEL
		salaryTable.getColumnModel().getColumn(12).setPreferredWidth(120); // GROSS SALARY
	}
	
	private JPanel createModernSearchPanel() {
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
		searchPanel.setBackground(WHITE);
		searchPanel.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(20, 20, 20, 20)
		));
		
		// Title
		JLabel titleLabel = new JLabel("🔍 Employee Search");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
		titleLabel.setForeground(DARK_GRAY);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
		
		// Search controls panel
		JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		controlsPanel.setBackground(WHITE);
		
		// Search field
		searchField = createModernTextField(25);
		searchField.setToolTipText("Enter search term...");
		
		// Search filter
		searchFilter = createModernComboBox(new String[]{
				messages.getString("search.all"),
				messages.getString("search.name"),
				messages.getString("search.email"),
				messages.getString("search.position"),
		});
		
		// Search button
		searchButton = createModernButton(messages.getString("search.button"), PRIMARY_COLOR);
		searchButton.setIcon(new ImageIcon("🔍"));
		
		// Labels
		searchLabel = new JLabel(messages.getString("search.label"));
		searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		searchLabel.setForeground(DARK_GRAY);
		
		filterLabel = new JLabel(messages.getString("search.filter"));
		filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		filterLabel.setForeground(DARK_GRAY);
		
		controlsPanel.add(searchLabel);
		controlsPanel.add(searchField);
		controlsPanel.add(filterLabel);
		controlsPanel.add(searchFilter);
		controlsPanel.add(searchButton);
		
		searchPanel.add(titleLabel);
		searchPanel.add(controlsPanel);
		
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
	
	private JPanel createModernTablePanel() {
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBackground(WHITE);
		tablePanel.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(10, 10, 10, 10)
		));
		
		// Table title
		JLabel tableTitle = new JLabel("💰 Salary Information");
		tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
		tableTitle.setForeground(DARK_GRAY);
		tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
		
		JScrollPane scrollPane = new JScrollPane(salaryTable);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setBackground(WHITE);
		
		tablePanel.add(tableTitle, BorderLayout.NORTH);
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		
		return tablePanel;
	}
	
	private JPanel createModernControlPanel() {
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		controlPanel.setBackground(WHITE);
		controlPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
		
		viewDetailsButton = createModernButton(messages.getString("salary.button.view_detail"), SUCCESS_COLOR);
		viewDetailsButton.setEnabled(false);
		viewDetailsButton.setIcon(new ImageIcon("👁️"));
		viewDetailsButton.addActionListener(e -> viewSalaryDetails());
		
		refreshButton = createModernButton(messages.getString("salary.button.refresh"), WARNING_COLOR);
		refreshButton.setIcon(new ImageIcon("🔄"));
		refreshButton.addActionListener(e -> loadSalaryEmployees());
		
		controlPanel.add(viewDetailsButton);
		controlPanel.add(refreshButton);
		
		return controlPanel;
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
		
		return button;
	}
	
	private void openEmployeeDetails(int row) {
		int employeeId = (int) tableModel.getValueAt(row, 0);
		JFrame window = new JFrame();
		EmployeeDialog dialog = new EmployeeDialog(window, connection, employeeId);
		dialog.setVisible(true);
		if(dialog.isEmployeeUpdated()) {
			// refresh the table
			loadSalaryEmployees();
		}
	}
	
	private void viewSalaryDetails() {
		int rowSelected = salaryTable.getSelectedRow();
		if (rowSelected < 0) {
			return;
		}
		
		try {
			String nameEmployee = (String) tableModel.getValueAt(rowSelected, 2);
			String position = (String) tableModel.getValueAt(rowSelected, 3);
			
			String sql = "SELECT e.id FROM employees e " +
						"WHERE CONCAT(e.first_name, ' ', e.last_name) = ? AND e.position = ?";
			PreparedStatement pstmt = connection.prepareStatement(sql);
			pstmt.setString(1, nameEmployee);
			pstmt.setString(2, position);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				int idEmployee = rs.getInt("id");
				
				// Use SalaryInfo class
				SalaryInfo salary = SalaryCalculator.calculateSalary(idEmployee, connection);
				salary.setLanguage(localeInfo);
				
				// Modern dialog with better styling
				JOptionPane.showMessageDialog(
					this,
					"<html><div style='font-family: Segoe UI; max-width: 500px;'>" +
					"<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px; margin: -15px -15px 15px -15px; border-radius: 8px 8px 0 0;'>" +
					"<h2 style='margin: 0; font-size: 18px;'>💰 Salary Details</h2>" +
					"<p style='margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;'>" + nameEmployee + "</p>" +
					"</div>" +
					"<div style='padding: 10px;'>" +
					salary.viewDetails().replaceAll("<br>", "</p><p style='margin: 8px 0;'>") +
					"</div>" +
					"</div></html>",
					"💼 " + messages.getString("salary.dialog.detail") + " - " + nameEmployee,
					JOptionPane.INFORMATION_MESSAGE
				);
			}
			
			rs.close();
			pstmt.close();
		} catch (SQLException loi) {
			System.out.println("❌ Error viewing salary details: " + loi.getMessage());
			
			// Modern error dialog
			JOptionPane.showMessageDialog(
				this,
				"<html><div style='text-align: center; font-family: Segoe UI;'>" +
				"<p style='font-size: 16px; color: #e74c3c; margin-bottom: 10px;'>⚠️ Error</p>" +
				"<p style='font-size: 14px; color: #555; margin-bottom: 5px;'>Unable to load salary details.</p>" +
				"<p style='font-size: 12px; color: #777;'>Please try again or contact support.</p>" +
				"</div></html>",
				"Error",
				JOptionPane.ERROR_MESSAGE
			);
		}
	}
	
	private void loadSalaryEmployees() {
		if (!sessionManager.isManager() && !sessionManager.isAdmin()) {
			return;
		}
		
		try {
			tableModel.setRowCount(0);
			
			String sql = "SELECT e.id, e.first_name, e.last_name, e.email, e.position, e.phone, " +
					"e.identity_number, e.photo, e.salary, u.role, e.hire_date " +
					"FROM employees e " +
					"INNER JOIN users u ON e.user_id = u.id " +
					"ORDER BY e.first_name, e.last_name";
			
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(sql);
			
			while(rs.next()) {
				int idEmployee = rs.getInt("id");
				
				// Use SalaryInfo class
				SalaryInfo salaryInfo = SalaryCalculator.calculateSalary(idEmployee, connection);
				
				Vector<Object> row = new Vector<>();

				// Handle photo with modern styling
				ImageIcon icon;
				byte[] anhBytes = rs.getBytes("photo"); 
				if(anhBytes != null && anhBytes.length > 0) {
					icon = new ImageIcon(anhBytes);
				} else {
					anhBytes = hImage.fileImage(DEFAULT_AVATAR_PATH, 45, 45);
					if(anhBytes != null) {
						icon = new ImageIcon(anhBytes);
					} else {
						icon = new ImageIcon(Objects.requireNonNull(this.getClass().getResource(DEFAULT_AVATAR_PATH)));
					}
				}
				row.add(idEmployee);
				row.add(icon);
				
				// Format employee name with modern styling
				String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
				row.add(fullName);
				
				// Format position with badge-like appearance
				String position = rs.getString("position");
				row.add(position);
				
				// Format level with color coding
				String level = salaryInfo.level.toUpperCase();
				row.add(level);
				
				// Format email
				row.add(rs.getString("email"));
				
				// Format phone number
				String phone = rs.getString("phone");
				row.add(phone != null ? phone : "-");
				
				// Format identity number
				String identity = rs.getString("identity_number");
				row.add(identity != null ? identity : "-");
				
				// Format hire date
				String hireDate = rs.getString("hire_date");
				row.add(hireDate != null ? hireDate : "-");
				
				// Format salary with currency symbol
				double salary = rs.getDouble("salary");
				row.add(String.format("$%,.0f", salary));
				
				// Format salary coefficient
				row.add(String.format("%.1f", salaryInfo.levelCoefficient));
				
				// Format experience level
				row.add(salaryInfo.experienceLevel);
				
				// Format gross salary with currency symbol and color coding
				double grossSalary = salaryInfo.finalSalary;
				row.add(String.format("$%,.0f", grossSalary));
				
				tableModel.addRow(row);
			}
			rs.close();
			st.close();
			
			// Show success message with modern styling
			if (tableModel.getRowCount() > 0) {
				System.out.println("✅ Successfully loaded " + tableModel.getRowCount() + " employee records");
			} else {
				System.out.println("ℹ️ No employee records found");
			}
			
		} catch (Exception loi) {
			System.out.println("❌ Error loading data: " + loi.getMessage());
			loi.printStackTrace();
			
			// Show user-friendly error message
			JOptionPane.showMessageDialog(
				this,
				"<html><div style='text-align: center;'>" +
				"<p style='font-size: 16px; color: #e74c3c;'>Error Loading Data</p>" +
				"<p style='font-size: 14px; color: #555;'>Unable to load employee salary information.</p>" +
				"<p style='font-size: 12px; color: #777;'>Please check your database connection and try again.</p>" +
				"</div></html>",
				"Database Error",
				JOptionPane.ERROR_MESSAGE
			);
		}
	}
	
	private void searchEmployees(String searchText, String filter) {
		try {
			tableModel.setRowCount(0);
			String query = "SELECT e.id, e.photo, e.first_name, e.last_name, e.email, e.phone, e.position, e.hire_date," +
							" e.identity_number, e.photo, e.salary, u.role, e.hire_date " +
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
			
			query += " ORDER BY e.first_name, e.last_name";
			
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
				
				int idEmployee = rs.getInt("id");
				
				// Use SalaryInfo class
				SalaryInfo salaryInfo = SalaryCalculator.calculateSalary(idEmployee, connection);
				
				row.add(rs.getInt("id"));
				
				// Modern photo handling
				ImageIcon icon;
				byte[] bytePhoto = rs.getBytes("photo");
				if(bytePhoto != null && bytePhoto.length > 0) {
					icon = new ImageIcon(bytePhoto);
				} else {
					bytePhoto = hImage.fileImage(DEFAULT_AVATAR_PATH, 45, 45);
					if(bytePhoto != null) {
						icon = new ImageIcon(bytePhoto);
					} else {
						icon = new ImageIcon(Objects.requireNonNull(this.getClass().getResource(DEFAULT_AVATAR_PATH)));
					}
				}
				row.add(icon);
				
				// Format data consistently with loadSalaryEmployees
				String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
				row.add(fullName);
				
				String position = rs.getString("position");
				row.add(position);
				
				String level = salaryInfo.level.toUpperCase();
				row.add(level);
				
				row.add(rs.getString("email"));
				
				String phone = rs.getString("phone");
				row.add(phone != null ? phone : "-");
				
				String identity = rs.getString("identity_number");
				row.add(identity != null ? identity : "-");
				
				String hireDate = rs.getString("hire_date");
				row.add(hireDate != null ? hireDate : "-");
				
				double salary = rs.getDouble("salary");
				row.add(String.format("$%,.0f", salary));
				
				row.add(String.format("%.1f", salaryInfo.positionCoefficient));
				row.add(salaryInfo.level);
				
				double grossSalary = salaryInfo.finalSalary;
				row.add(String.format("$%,.0f", grossSalary));
				
				tableModel.addRow(row);
			}
			
			rs.close();
			stmt.close();
			
			// Show search results with modern styling
			if (tableModel.getRowCount() > 0) {
				System.out.println("🔍 Found " + tableModel.getRowCount() + " employee(s) matching your search");
			} else {
				System.out.println("ℹ️ No employees found matching your search criteria");
				
				// Show user-friendly no results message
				JOptionPane.showMessageDialog(
					this,
					"<html><div style='text-align: center;'>" +
					"<p style='font-size: 16px; color: #3498db;'>No Results Found</p>" +
					"<p style='font-size: 14px; color: #555;'>No employees match your search criteria.</p>" +
					"<p style='font-size: 12px; color: #777;'>Try adjusting your search terms or filters.</p>" +
					"</div></html>",
					"Search Results",
					JOptionPane.INFORMATION_MESSAGE
				);
			}
			
		} catch (SQLException e) {
			System.out.println("❌ Error searching employees: " + e.getMessage());
			
			// Show user-friendly error message
			JOptionPane.showMessageDialog(
				this,
				"<html><div style='text-align: center;'>" +
				"<p style='font-size: 16px; color: #e74c3c;'>Search Error</p>" +
				"<p style='font-size: 14px; color: #555;'>Unable to perform search operation.</p>" +
				"<p style='font-size: 12px; color: #777;'>Please try again or contact support.</p>" +
				"</div></html>",
				"Search Error",
				JOptionPane.ERROR_MESSAGE
			);
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
				this.localeInfo = locale;
			} else {
				// Default to English if no language preference is set
				currentLanguage = DEFAULT_LANGUAGE;
				messages = ResourceBundle.getBundle("messages", Locale.of("en"));
				this.localeInfo = Locale.of("en");
			}
			
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			// Default to English if there's an error
			currentLanguage = DEFAULT_LANGUAGE;
			messages = ResourceBundle.getBundle("messages", Locale.of("en"));
			this.localeInfo = Locale.of("en");
		}
	}
	
	private void updateTableHeaders() {
		// Update table headers
		String[] columns = {
				messages.getString("salary.column.id"),
				messages.getString("salary.column.photo"),
				messages.getString("salary.column.name"),
				messages.getString("salary.column.position"),
				messages.getString("salary.column.level"),
				messages.getString("salary.column.email"),
				messages.getString("salary.column.phone"),
				messages.getString("salary.column.identitynumber"),
				messages.getString("salary.column.hire_date"),
				messages.getString("salary.column.salary"),
				messages.getString("salary.column.salarycoefficient"),
				messages.getString("salary.column.experience_level"),
				messages.getString("salary.column.grosssalary"),
			};
		tableModel.setColumnIdentifiers(columns);

		salaryTable.getTableHeader().repaint();
		
		// Modern photo column renderer
		salaryTable.getColumnModel().getColumn(1).setPreferredWidth(60);
		salaryTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
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
				} else {
					// Alternate row colors for better readability
					if (row % 2 == 0) {
						label.setBackground(WHITE);
					} else {
						label.setBackground(TABLE_ALTERNATE_ROW);
					}
					label.setForeground(table.getForeground());
				}
				
				return label;
			}
		});
		
		// Modern mouse listener with improved UX
		salaryTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = salaryTable.getColumnModel().getColumnIndexAtX(e.getX());
				int row = e.getY() / salaryTable.getRowHeight();
				
				if (row < salaryTable.getRowCount() && row >= 0 && column < salaryTable.getColumnCount() && column >= 0) {
					if(e.getClickCount() == 2) {
						if(row != -1) {
							openEmployeeDetails(row);
						}
					}
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				salaryTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				salaryTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}



}
