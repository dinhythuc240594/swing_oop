package com.project01;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.toedter.calendar.JDateChooser;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

public class TaskManagementScreen extends JPanel {

	private JTable taskTable;
	private DefaultTableModel tableModel;
	private JTextField taskTitleField;
	private JTextArea descriptionField;
	private JComboBox<String> employeeCombobox;
	private JComboBox<String> assingnedByCombobox;
	private JComboBox<String> priorityCombobox;
	private JComboBox<String> statusCombobox;
	private JButton addButton, editButton, deleteButton, refreshButton, updateButton, loadEmployeeButton;
	private Connection connection;
	private SessionManager sessionManager;
	private JDateChooser dueDateChooser;
	private ResourceBundle messages;
	private static final String DEFAULT_LANGUAGE = "English";
	private String currentLanguage = DEFAULT_LANGUAGE;
	private JLabel titleTask, descriptionTask, assignedByTask, assignedToTask, priorityTask, statusTask, dueDateTask;
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
	
	public TaskManagementScreen(Connection connection) {
		this.connection = connection;
		this.sessionManager = SessionManager.getInstance();
		this.messages = ResourceBundle.getBundle("messages", Locale.of("en"));
		
		// Set modern look and feel
		setupModernLookAndFeel();
		
		initComponents();
		loadTasks();
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
		updateUIText();
	}

	private void updateUIText() {
		// Update table headers
		String[] columns = {
			messages.getString("task.id"),
			messages.getString("task.title"),
			messages.getString("task.description"),
			messages.getString("task.assigned_to"),
			messages.getString("task.priority"),
			messages.getString("task.status"),
			messages.getString("task.due_date"),
			messages.getString("task.created_by")
		};
		tableModel.setColumnIdentifiers(columns);

		addButton.setText(messages.getString("task.button.add"));
		editButton.setText(messages.getString("task.button.edit"));
		deleteButton.setText(messages.getString("task.button.delete"));
		refreshButton.setText(messages.getString("task.button.refresh"));
		updateButton.setText(messages.getString("task.button.update"));
		
		taskTable.getTableHeader().repaint();
		
		titleTask.setText(messages.getString("task.form.title"));
		descriptionTask.setText(messages.getString("task.form.description"));
		if(sessionManager.isEmployee()) {
			assignedByTask.setText(messages.getString("task.form.assigned_by"));
		} else {
			assignedToTask.setText(messages.getString("task.form.assigned_to"));
		}
		
		priorityTask.setText(messages.getString("task.form.priority"));
		statusTask.setText(messages.getString("task.form.status"));
		dueDateTask.setText(messages.getString("task.form.due_date"));
		
		updateSearchFilter();
	}
	
	private void updateSearchFilter() {
		// Store the currently selected index
		int selectedIndex = searchFilter.getSelectedIndex();
		
		// Update the items
		searchFilter.removeAllItems();
		searchFilter.addItem(messages.getString("search.task.all"));
		searchFilter.addItem(messages.getString("search.task.title"));
		searchFilter.addItem(messages.getString("search.task.description"));
		searchFilter.addItem(messages.getString("search.task.assigned_to"));
		searchFilter.addItem(messages.getString("search.task.assigned_by"));
		searchFilter.addItem(messages.getString("search.task.priority"));
		searchFilter.addItem(messages.getString("search.task.status"));
		
		// Restore the selected index
		if (selectedIndex >= 0 && selectedIndex < searchFilter.getItemCount()) {
			searchFilter.setSelectedIndex(selectedIndex);
		}
		
		// Update other search components
		searchLabel.setText(messages.getString("search.task.label"));
		filterLabel.setText(messages.getString("search.task.filter"));
		searchButton.setText(messages.getString("search.task.button"));
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		setBackground(WHITE);
		setBorder(new EmptyBorder(20, 20, 20, 20));
		
		loadUserLanguage();

		// Create modern search panel
		JPanel searchPanel = createModernSearchPanel();
		
		// Create modern table panel
		JPanel tablePanel = createModernTablePanel();
		
		// Create modern form panel
		JPanel formPanel = createModernFormPanel();
		
		// Create modern control panel
		JPanel controlPanel = createModernControlPanel();
		
		// Layout components
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(WHITE);
		topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
		topPanel.add(searchPanel, BorderLayout.CENTER);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(WHITE);
		centerPanel.add(tablePanel, BorderLayout.CENTER);
		centerPanel.add(formPanel, BorderLayout.EAST);
		
		add(topPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.SOUTH);
		
		// Add table selection listener
		taskTable.getSelectionModel().addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting()) {
				int selectedRow = taskTable.getSelectedRow();
				if(selectedRow >= 0) {
					populaField(selectedRow);
				}
			}
		});
		
		// Set button visibility based on user role
		boolean isAdmin = sessionManager.isAdmin();
		boolean isManager = sessionManager.isManager();
		boolean isEmployee = sessionManager.isEmployee();
		
		addButton.setVisible(isAdmin || isManager);
		editButton.setVisible(isAdmin || isManager);
		deleteButton.setVisible(isAdmin || isManager);
		updateButton.setVisible(isEmployee);
		loadEmployeeButton.setVisible(isAdmin || isManager);
		assignedByTask.setVisible(isEmployee);
		assingnedByCombobox.setVisible(isEmployee);
		
		// Load initial data
		loadEmployees();
		loadAssignedBy();
		
		updateUIText();
	}
	
	private JPanel createModernSearchPanel() {
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		searchPanel.setBackground(WHITE);
		searchPanel.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(15, 15, 15, 15)
		));
		
		// Search field
		searchField = createModernTextField(20);
		searchField.setToolTipText("Enter search term...");
		
		// Search filter
		searchFilter = createModernComboBox(new String[]{
			messages.getString("search.task.all"),
			messages.getString("search.task.title"),
			messages.getString("search.task.description"),
			messages.getString("search.task.assigned_to"),
			messages.getString("search.task.assigned_by"),
			messages.getString("search.task.priority"),
			messages.getString("search.task.status")
		});
		
		// Search button
		searchButton = createModernButton(messages.getString("search.task.button"), PRIMARY_COLOR);
		searchButton.setIcon(new ImageIcon("🔍"));
		searchButton.setSize(200, 60);
		
		// Labels
		searchLabel = new JLabel(messages.getString("search.task.label"));
		searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		searchLabel.setForeground(DARK_GRAY);
		
		filterLabel = new JLabel(messages.getString("search.task.filter"));
		filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		filterLabel.setForeground(DARK_GRAY);
		
		searchPanel.add(searchLabel);
		searchPanel.add(searchField);
		searchPanel.add(filterLabel);
		searchPanel.add(searchFilter);
		searchPanel.add(searchButton);
		
		// Add event listeners
		searchButton.addActionListener(e -> searchTasks(searchField.getText(), (String)searchFilter.getSelectedItem()));
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchTasks(searchField.getText(), (String)searchFilter.getSelectedItem());
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
		JLabel tableTitle = new JLabel("📋 Task List");
		tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
		tableTitle.setForeground(DARK_GRAY);
		tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
		
		String[] columns = {
			messages.getString("task.id"),
			messages.getString("task.title"),
			messages.getString("task.description"),
			messages.getString("task.assigned_to"),
			messages.getString("task.priority"),
			messages.getString("task.status"),
			messages.getString("task.due_date"),
			messages.getString("task.created_by")
		};

		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		taskTable = new JTable(tableModel);
		setupModernTable();
		
		JScrollPane tableScrollPanel = new JScrollPane(taskTable);
		tableScrollPanel.setBorder(BorderFactory.createEmptyBorder());
		tableScrollPanel.getViewport().setBackground(WHITE);
		
		tablePanel.add(tableTitle, BorderLayout.NORTH);
		tablePanel.add(tableScrollPanel, BorderLayout.CENTER);
		
		return tablePanel;
	}
	
	private void setupModernTable() {
		taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		taskTable.setRowHeight(50);
		taskTable.setShowGrid(false);
		taskTable.setIntercellSpacing(new Dimension(0, 0));
		taskTable.setBackground(WHITE);
		taskTable.setSelectionBackground(new Color(52, 152, 219, 30));
		taskTable.setSelectionForeground(DARK_GRAY);
		taskTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		
		// Modern table header
		JTableHeader header = taskTable.getTableHeader();
		header.setBackground(TABLE_HEADER_BG);
		header.setForeground(DARK_GRAY);
		header.setFont(new Font("Segoe UI", Font.BOLD, 13));
		header.setBorder(new MatteBorder(0, 0, 2, 0, MEDIUM_GRAY));
		
		// Set column widths
		taskTable.getColumnModel().getColumn(0).setPreferredWidth(50); //ID
		taskTable.getColumnModel().getColumn(1).setPreferredWidth(150); // TITLE
		taskTable.getColumnModel().getColumn(2).setPreferredWidth(200); // DESCRIPTION
		taskTable.getColumnModel().getColumn(3).setPreferredWidth(100); // ASSIGNED TO
		taskTable.getColumnModel().getColumn(4).setPreferredWidth(80); // PRIORITY
		taskTable.getColumnModel().getColumn(5).setPreferredWidth(100); // STATUS
		taskTable.getColumnModel().getColumn(6).setPreferredWidth(100); // DUE DATE
		taskTable.getColumnModel().getColumn(7).setPreferredWidth(100); // CREATE BY
	}
	
	private JPanel createModernFormPanel() {
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		formPanel.setBackground(WHITE);
		formPanel.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(20, 20, 20, 20)
		));
		formPanel.setPreferredSize(new Dimension(300, 0));
		
		// Form title
		JLabel formTitle = new JLabel("✏️ Task Form");
		formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
		formTitle.setForeground(DARK_GRAY);
		formTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		formTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
		formPanel.add(formTitle);
		
		// Task title
		titleTask = new JLabel(messages.getString("task.form.title"));
		titleTask.setFont(new Font("Segoe UI", Font.BOLD, 12));
		titleTask.setForeground(DARK_GRAY);
		taskTitleField = createModernTextField(15);
		addFormField(formPanel, titleTask, taskTitleField);
		
		// Description
		descriptionTask = new JLabel(messages.getString("task.form.description"));
		descriptionTask.setFont(new Font("Segoe UI", Font.BOLD, 12));
		descriptionTask.setForeground(DARK_GRAY);
		descriptionField = createModernTextArea(5, 15);
		addFormField(formPanel, descriptionTask, new JScrollPane(descriptionField));
		
		// Assigned to/by
//		if(sessionManager.isEmployee()) {
//			assignedByTask = new JLabel(messages.getString("task.form.assigned_by"));
//			assignedByTask.setFont(new Font("Segoe UI", Font.BOLD, 12));
//			assignedByTask.setForeground(DARK_GRAY);
//			assingnedByCombobox = createModernComboBox(new String[]{});
//			addFormField(formPanel, assignedByTask, assingnedByCombobox);
//		} else {
//			assignedToTask = new JLabel(messages.getString("task.form.assigned_to"));
//			assignedToTask.setFont(new Font("Segoe UI", Font.BOLD, 12));
//			assignedToTask.setForeground(DARK_GRAY);
//			employeeCombobox = createModernComboBox(new String[]{});
//			addFormField(formPanel, assignedToTask, employeeCombobox);
//		}
		
		
		//// 2025-07-06 - fix setVisible for field assigned by
		assignedByTask = new JLabel(messages.getString("task.form.assigned_by"));
		assignedByTask.setFont(new Font("Segoe UI", Font.BOLD, 12));
		assignedByTask.setForeground(DARK_GRAY);
		assingnedByCombobox = createModernComboBox(new String[]{});
		addFormField(formPanel, assignedByTask, assingnedByCombobox);
		
		assignedToTask = new JLabel(messages.getString("task.form.assigned_to"));
		assignedToTask.setFont(new Font("Segoe UI", Font.BOLD, 12));
		assignedToTask.setForeground(DARK_GRAY);
		employeeCombobox = createModernComboBox(new String[]{});
		addFormField(formPanel, assignedToTask, employeeCombobox);
		
		// Priority
		priorityTask = new JLabel(messages.getString("task.form.priority"));
		priorityTask.setFont(new Font("Segoe UI", Font.BOLD, 12));
		priorityTask.setForeground(DARK_GRAY);
		priorityCombobox = createModernComboBox(new String[]{
			messages.getString("task.priority.low"),
			messages.getString("task.priority.medium"),
			messages.getString("task.priority.high")
		});
		addFormField(formPanel, priorityTask, priorityCombobox);
		
		// Status
		statusTask = new JLabel(messages.getString("task.form.status"));
		statusTask.setFont(new Font("Segoe UI", Font.BOLD, 12));
		statusTask.setForeground(DARK_GRAY);
		statusCombobox = createModernComboBox(new String[]{
			messages.getString("task.status.pending"),
			messages.getString("task.status.in_progress"),
			messages.getString("task.status.completed")
		});
		addFormField(formPanel, statusTask, statusCombobox);
		
		// Due date
		dueDateTask = new JLabel(messages.getString("task.form.due_date"));
		dueDateTask.setFont(new Font("Segoe UI", Font.BOLD, 12));
		dueDateTask.setForeground(DARK_GRAY);
		dueDateChooser = new JDateChooser();
		dueDateChooser.setPreferredSize(new Dimension(200, 35));
		dueDateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		dueDateChooser.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(8, 12, 8, 12)
		));
		addFormField(formPanel, dueDateTask, dueDateChooser);
		
		return formPanel;
	}
	
	private JPanel createModernControlPanel() {
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		controlPanel.setBackground(WHITE);
		controlPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
		
		addButton = createModernButton(messages.getString("task.button.add"), SUCCESS_COLOR);
		addButton.setIcon(new ImageIcon("➕"));
		addButton.addActionListener(e -> addTask());
		
		editButton = createModernButton(messages.getString("task.button.edit"), WARNING_COLOR);
		editButton.setIcon(new ImageIcon("✏️"));
		editButton.addActionListener(e -> editTask());
		
		deleteButton = createModernButton(messages.getString("task.button.delete"), DANGER_COLOR);
		deleteButton.setIcon(new ImageIcon("🗑️"));
		deleteButton.addActionListener(e -> deleteTask());
		
		refreshButton = createModernButton(messages.getString("task.button.refresh"), PRIMARY_COLOR);
		refreshButton.setIcon(new ImageIcon("🔄"));
		refreshButton.addActionListener(e -> loadTasks());
		
		updateButton = createModernButton(messages.getString("task.button.update"), SUCCESS_COLOR);
		updateButton.setIcon(new ImageIcon("💾"));
		updateButton.addActionListener(e -> updateTask());
		updateButton.setVisible(false);
		
		loadEmployeeButton = createModernButton(messages.getString("task.button.load"), SECONDARY_COLOR);
		loadEmployeeButton.setIcon(new ImageIcon("👥"));
		loadEmployeeButton.addActionListener(e -> loadEmployees());
		
		controlPanel.add(addButton);
		controlPanel.add(editButton);
		controlPanel.add(deleteButton);
		controlPanel.add(refreshButton);
		controlPanel.add(updateButton);
		controlPanel.add(loadEmployeeButton);
		
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
	
	private JTextArea createModernTextArea(int rows, int columns) {
		JTextArea textArea = new JTextArea(rows, columns);
		textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		textArea.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(MEDIUM_GRAY, 1, true),
			new EmptyBorder(8, 12, 8, 12)
		));
		textArea.setBackground(WHITE);
		textArea.setForeground(DARK_GRAY);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		return textArea;
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
	
	private void addFormField(JPanel panel, JLabel labelComponent, JComponent field) {
		JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		labelComponent.setPreferredSize(new Dimension(150, 25));
		fieldPanel.add(labelComponent);
		fieldPanel.add(field);
		panel.add(fieldPanel);
		panel.add(Box.createVerticalStrut(5));
	}
	
	private void populaField(int row) {
		taskTitleField.setText(tableModel.getValueAt(row, 1).toString());
		descriptionField.setText(tableModel.getValueAt(row, 2).toString());
		employeeCombobox.setSelectedItem(tableModel.getValueAt(row, 3).toString());
		priorityCombobox.setSelectedItem(tableModel.getValueAt(row, 4).toString());
		statusCombobox.setSelectedItem(tableModel.getValueAt(row, 5).toString());
		dueDateChooser.setDate((java.util.Date) tableModel.getValueAt(row, 6));
	}
	
	public void loadTasks() {
		try {
			
			tableModel.setRowCount(0);
			String query;
			PreparedStatement stmt;
			
			if(sessionManager.isAdmin() || sessionManager.isManager()) {
				query = "SELECT t.*, e.first_name as assigned_first_name, e.last_name as assigned_last_name, " + 
						"m.first_name as created_first_name, m.last_name as created_last_name " +
						"FROM tasks t " +
						"JOIN employees e ON t.assigned_to = e.id " +
						"JOIN managers m ON t.assigned_by = m.id ";
				stmt =  connection.prepareStatement(query);
			} else {
				query = "SELECT t.*, e.first_name as assigned_first_name, e.last_name as assigned_last_name, " + 
						"m.first_name as created_first_name, m.last_name as created_last_name " +
						"FROM tasks t " +
						"JOIN employees e ON t.assigned_to = e.id " +
						"JOIN managers m ON t.assigned_by = m.id " +
						"WHERE t.assigned_to = (SELECT e2.id FROM employees e2 WHERE e2.user_id = ?)";
				stmt =  connection.prepareStatement(query);
				stmt.setInt(1, sessionManager.getUserId());	
			}
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				Object[] row = {
					rs.getInt("id"),
					rs.getString("title"),
					rs.getString("description"),
					rs.getString("assigned_first_name") + " " + rs.getString("assigned_last_name"),
					rs.getString("priority"),
					rs.getString("status"),
					rs.getDate("due_date"),
					rs.getString("created_first_name") + " " + rs.getString("created_last_name"),
				};
				tableModel.addRow(row);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadEmployees() {
		try {
			employeeCombobox.removeAllItems();
			String query = "SELECT id, first_name, last_name FROM employees";
			PreparedStatement stmt = connection.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				String name = rs.getString("first_name") + " " + rs.getString("last_name") ;
				employeeCombobox.addItem(name);
			}
		} catch (SQLException e) {
		}
	}
	
	private void loadAssignedBy() {
		try {
			assingnedByCombobox.removeAllItems();
			String query = "SELECT id, first_name, last_name FROM managers";
			PreparedStatement stmt = connection.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				String name = rs.getString("first_name") + " " + rs.getString("last_name") ;
				assingnedByCombobox.addItem(name);
			}
		} catch (SQLException e) {
			// TODO: handle exception
		}
	}
	
	private void addTask() {
		if(!validateFields()) {
			return;
		}
		
		try {
			String query = "INSERT INTO tasks (title, description, assigned_to, assigned_by, priority, status, due_date) "
					+ "VALUES(?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = connection.prepareStatement(query);
			
			stmt.setString(1, taskTitleField.getText());
			stmt.setString(2, descriptionField.getText());
			stmt.setInt(3, getEmployeeId((String) employeeCombobox.getSelectedItem()));
			stmt.setInt(4, sessionManager.getUserId());
			stmt.setString(5, (String) priorityCombobox.getSelectedItem());
			stmt.setString(6, (String) statusCombobox.getSelectedItem());
			stmt.setDate(7, new java.sql.Date(dueDateChooser.getDate().getTime()));
			
			stmt.executeUpdate();
			stmt.close();
			
			clearFields();
			loadTasks();
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	private void editTask() {
		int selectedRow = taskTable.getSelectedRow();

		if(selectedRow < 0) {
			return;
		}
		if(!validateFields()) return;
		
		try {
			
			if(sessionManager.isEmployee()) {
				String query = "UPDATE tasks SET title = ?, description = ?, " + 
						"priority = ?, status = ?, due_date = ? " +
						"WHERE id = ? AND assigned_to = (SELECT e.id FROM employees e WHERE e.user_id = ?)";
				PreparedStatement stmt = connection.prepareStatement(query);
				
				stmt.setString(1, taskTitleField.getText());
				stmt.setString(2, descriptionField.getText());
				stmt.setString(3, (String) priorityCombobox.getSelectedItem());
				stmt.setString(4, (String) statusCombobox.getSelectedItem());
				stmt.setDate(5, new java.sql.Date(dueDateChooser.getDate().getTime()));
				stmt.setInt(6, (int) tableModel.getValueAt(selectedRow, 0));
				stmt.setInt(7, sessionManager.getUserId());
				
				stmt.executeUpdate();
				stmt.close();
			} else {
				String query = "UPDATE tasks SET title = ?, description = ?, assigned_to = ?, " + 
						"priority = ?, status = ?, due_date = ? " +
						"WHERE id = ?";
				PreparedStatement stmt = connection.prepareStatement(query);
				
				stmt.setString(1, taskTitleField.getText());
				stmt.setString(2, descriptionField.getText());
				stmt.setInt(3, getEmployeeId((String) employeeCombobox.getSelectedItem()));
				stmt.setString(4, (String) priorityCombobox.getSelectedItem());
				stmt.setString(5, (String) statusCombobox.getSelectedItem());
				stmt.setDate(6, new java.sql.Date(dueDateChooser.getDate().getTime()));
				stmt.setInt(7, (int) tableModel.getValueAt(selectedRow, 0));
				
				stmt.executeUpdate();
				stmt.close();				
			}
						
			clearFields();
			loadTasks();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateTask() {
		// This method is for employees to update their task status
		int selectedRow = taskTable.getSelectedRow();
		if(selectedRow < 0) {
			JOptionPane.showMessageDialog(this, "Please select a task to update");
			return;
		}

		try {
			String query = "UPDATE tasks SET status = ? WHERE id = ? AND assigned_to = (SELECT e.id FROM employees e WHERE e.user_id = ?)";
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setString(1, (String) statusCombobox.getSelectedItem());
			stmt.setInt(2, (int) tableModel.getValueAt(selectedRow, 0));
			stmt.setInt(3, sessionManager.getUserId());
			
			int rowsAffected = stmt.executeUpdate();
			stmt.close();
			
			if(rowsAffected > 0) {
				JOptionPane.showMessageDialog(this, "✅ Task status updated successfully!");
			} else {
				JOptionPane.showMessageDialog(this, "❌ You can only update tasks assigned to you.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "❌ Error updating task: " + e.getMessage());
		}
						
		clearFields();
		loadTasks();
	}
	
	private void deleteTask() {
		int selectedRow = taskTable.getSelectedRow();
		if(selectedRow < 0) {
			JOptionPane.showMessageDialog(this, "Please select a task to delete");
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, 
						"Are you sure you want to delete this task",
						"Confirm Delete?",
						JOptionPane.YES_NO_OPTION);
		
		if(confirm == JOptionPane.YES_OPTION) {
			try {
				String query = "DELETE FROM tasks WHERE id = ?";
				PreparedStatement stmt = connection.prepareStatement(query);
				stmt.setInt(1, (int) tableModel.getValueAt(selectedRow, 0));
				stmt.executeUpdate();
				stmt.close();
				
				clearFields();
				loadTasks();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private boolean validateFields() {
		if(taskTitleField.getText().trim().isEmpty()) {
			return false;
		}
		if(descriptionField.getText().trim().isEmpty()) {
			return false;
		}
		if(dueDateChooser.getDate() == null) {
			return false;
		}
		return true;
	}
	
	private int getEmployeeId(String fullName) throws SQLException {
		String[] names = fullName.split(" ");
		String query = "SELECT id FROM employees WHERE first_name = ? AND last_name = ?";
		PreparedStatement stmt = connection.prepareStatement(query);
		stmt.setString(1, names[0]);
		stmt.setString(2, names[1]);
		ResultSet rs = stmt.executeQuery();
		int id = -1;
		if(rs.next()) {
			id = rs.getInt("id");
		}
		rs.close();
		stmt.close();
		return id;
	}
	
	private void clearFields() {
		taskTitleField.setText("");
		descriptionField.setText("");
		employeeCombobox.setSelectedIndex(0);
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

	private void updateOtherComponents() {
		// Update new employee button
		addButton.setText(messages.getString("task.button.add"));
		editButton.setText(messages.getString("task.button.edit"));
		deleteButton.setText(messages.getString("task.button.delete"));
		refreshButton.setText(messages.getString("task.button.refresh"));
		updateButton.setText(messages.getString("task.button.update"));
		loadEmployeeButton.setText(messages.getString("task.button.load"));
	}
	
	//// 2025-06-16 - add function search for task management ////
	private void searchTasks(String searchText, String filter) {
		try {
			tableModel.setRowCount(0);
			String query;
			if(sessionManager.isAdmin() || sessionManager.isManager()) {
				query = "SELECT t.*, e.first_name as assigned_first_name, e.last_name as assigned_last_name, " +
						"m.first_name as created_first_name, m.last_name as created_last_name " +
						"FROM tasks t " +
						"JOIN employees e ON t.assigned_to = e.id " +
						"JOIN managers m ON t.assigned_by = m.id " +
						"WHERE 1=1";	
			}else {
				query = "SELECT t.*, e.first_name as assigned_first_name, e.last_name as assigned_last_name, " +
						"m.first_name as created_first_name, m.last_name as created_last_name " +
						"FROM tasks t " +
						"JOIN employees e ON t.assigned_to = e.id " +
						"JOIN managers m ON t.assigned_by = m.id " +
						"WHERE t.assigned_to = (SELECT e2.id FROM employees e2 WHERE e2.user_id = " + sessionManager.getUserId() + ")";
			}
			
			
			if (!searchText.isEmpty()) {
				switch (filter) {
					case "Title":
						query += " AND t.title LIKE ?";
						break;
					case "Description":
						query += " AND t.description LIKE ?";
						break;
					case "Assigned To":
						query += " AND (assigned_first_name LIKE ? OR assigned_last_name LIKE ?)";
						break;
					case "Assigned By":
						query += " AND (created_first_name LIKE ? OR created_last_name LIKE ?)";
						break;
					case "Priority":
						query += " AND t.priority LIKE ?";
						break;
					case "Status":
						query += " AND t.status LIKE ?";
						break;
					default:
						query += " AND (t.title LIKE ? OR t.description LIKE ? OR (assigned_first_name LIKE ? OR assigned_last_name LIKE ?) OR t.priority LIKE ? OR (created_first_name LIKE ? OR created_last_name LIKE ?) OR t.status LIKE ?)";
				}
			}
			
			PreparedStatement stmt = connection.prepareStatement(query);
			
			if (!searchText.isEmpty()) {
				String searchPattern = "%" + searchText + "%";
				switch (filter) {
					case "Title":
						stmt.setString(1, searchPattern);
						break;
					case "Description":
						stmt.setString(1, searchPattern);
						break;
					case "Assigned To":
						stmt.setString(1, searchPattern);
						break;
					case "Assigned By":
						stmt.setString(1, searchPattern);
						break;
					case "Priority":
						stmt.setString(1, searchPattern);
						break;
					case "Status":
						stmt.setString(1, searchPattern);
						break;
					default:
						stmt.setString(1, searchPattern);
						stmt.setString(2, searchPattern);
						stmt.setString(3, searchPattern);
						stmt.setString(4, searchPattern);
						stmt.setString(5, searchPattern);
						stmt.setString(6, searchPattern);
				}
			}
			
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				Vector<Object> row = new Vector<>();
				row.add(rs.getInt("id"));
				
//				ImageIcon icon;
//				byte[] bytePhoto = rs.getBytes("photo");
//				if(bytePhoto != null && bytePhoto.length > 0) {
//					icon = new ImageIcon(bytePhoto);
//				} else {
//					bytePhoto = hImage.fileImage(DEFAULT_AVATAR_PATH, 40, 40);
//					if(bytePhoto != null) {
//						icon = new ImageIcon(bytePhoto);
//					} else {
//						icon = new ImageIcon(Objects.requireNonNull(this.getClass().getResource(DEFAULT_AVATAR_PATH)));
//					}
//				}
//				row.add(icon);
				
				row.add(rs.getString("title"));
				row.add(rs.getString("description"));
				row.add(rs.getString("assigned_first_name") + " " + rs.getString("assigned_last_name"));
				row.add(rs.getString("priority"));
				row.add(rs.getString("status"));
				row.add(rs.getDate("due_date"));
				row.add(rs.getString("created_first_name") + " " + rs.getString("created_last_name"));
				
				tableModel.addRow(row);
			}
			
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error searching employees: " + e.getMessage());
		}
	}
	
}
