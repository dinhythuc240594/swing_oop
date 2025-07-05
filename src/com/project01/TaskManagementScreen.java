package com.project01;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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
	
	
	public TaskManagementScreen(Connection connection) {
		this.connection = connection;
		this.sessionManager = SessionManager.getInstance();
		this.messages = ResourceBundle.getBundle("messages", Locale.of("en"));
		initComponents();
		loadTasks();
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
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		loadUserLanguage();

		// Add search panel
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchField = new JTextField(20);
		searchFilter = new JComboBox<>(new String[]{
				messages.getString("search.task.all"),
				messages.getString("search.task.title"),
				messages.getString("search.task.description"),
				messages.getString("search.task.assigned_to"),
				messages.getString("search.task.assigned_by"),
				messages.getString("search.task.priority"),
				messages.getString("search.task.status")
		});
		searchButton = new JButton(messages.getString("search.task.button"));
		
		searchLabel = new JLabel(messages.getString("search.task.label"));
		filterLabel = new JLabel(messages.getString("search.task.filter"));
		
		searchPanel.add(searchLabel);
		searchPanel.add(searchField);
		searchPanel.add(filterLabel);
		searchPanel.add(searchFilter);
		searchPanel.add(searchButton);
		searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		searchButton.addActionListener(e -> searchTasks(searchField.getText(), (String)searchFilter.getSelectedItem()));
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchTasks(searchField.getText(), (String)searchFilter.getSelectedItem());
				}
			}
		});
		
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
		taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		taskTable.getColumnModel().getColumn(0).setPreferredWidth(50); //ID
		taskTable.getColumnModel().getColumn(1).setPreferredWidth(150); // TITLE
		taskTable.getColumnModel().getColumn(2).setPreferredWidth(200); // DESCRIPTION
		taskTable.getColumnModel().getColumn(3).setPreferredWidth(100); // ASSIGNED TO
		taskTable.getColumnModel().getColumn(4).setPreferredWidth(80); // PRIORITY
		taskTable.getColumnModel().getColumn(5).setPreferredWidth(100); // STATUS
		taskTable.getColumnModel().getColumn(6).setPreferredWidth(100); // DUE DATE
		taskTable.getColumnModel().getColumn(7).setPreferredWidth(100); // CREATE BY
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(searchPanel, BorderLayout.CENTER);
		
		JScrollPane tableScrollPanel = new JScrollPane(taskTable);
//		add(tableScrollPanel);
		// add table to scroll pane
		JPanel taskPanel = new JPanel(new BorderLayout());
		taskPanel.add(topPanel, BorderLayout.NORTH);
		taskPanel.add(tableScrollPanel, BorderLayout.CENTER);
		
		add(taskPanel);
		
		// create control panel
		JPanel controlPanel = createControlPanel();
		add(controlPanel, BorderLayout.EAST);

		// add table selection listener
		taskTable.getSelectionModel().addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting()) {
				int selectedRow = taskTable.getSelectedRow();
				if(selectedRow >= 0) {
					populaField(selectedRow);
				}
			}
		});
	}
	
	private JPanel createControlPanel() {
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		boolean isAdmin = sessionManager.isAdmin();
		boolean isManager = sessionManager.isManager();
		boolean isEmployee = sessionManager.isEmployee();
		
		descriptionField = new JTextArea(4, 20);
		JScrollPane desScroolPanel = new JScrollPane(descriptionField);
		
		employeeCombobox = new JComboBox<>();
		employeeCombobox.setPreferredSize(new Dimension(100, employeeCombobox.getPreferredSize().height));
		loadEmployees();
		
		assingnedByCombobox = new JComboBox<>();
		assingnedByCombobox.setPreferredSize(new Dimension(100, assingnedByCombobox.getPreferredSize().height));
		loadAssignedBy();
		
		priorityCombobox = new JComboBox<>(new String[] {"Low", "Medium", "High"});
		priorityCombobox.setPreferredSize(new Dimension(100, priorityCombobox.getPreferredSize().height));
		
		statusCombobox = new JComboBox<>(new String[] {"Pending", "In Progress", "Completed"});
		statusCombobox.setPreferredSize(new Dimension(100, statusCombobox.getPreferredSize().height));
		
		dueDateChooser = new JDateChooser();
		dueDateChooser.setPreferredSize(new Dimension(100, dueDateChooser.getPreferredSize().height));
		
		addFormField(formPanel, titleTask = new JLabel(messages.getString("task.form.title")), taskTitleField = new JTextField(20));
		addFormField(formPanel, descriptionTask = new JLabel(messages.getString("task.form.description")), desScroolPanel);
		
		if(isEmployee) {
			addFormField(formPanel, assignedByTask = new JLabel(messages.getString("task.form.assigned_by")), assingnedByCombobox);
		} else {
			addFormField(formPanel, assignedToTask = new JLabel(messages.getString("task.form.assigned_to")), employeeCombobox);
		}
		
		addFormField(formPanel, priorityTask = new JLabel(messages.getString("task.form.priority")), priorityCombobox);
		addFormField(formPanel, statusTask = new JLabel(messages.getString("task.form.status")), statusCombobox);
		addFormField(formPanel, dueDateTask = new JLabel(messages.getString("task.form.due_date")), dueDateChooser);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addButton = new JButton("Add");
		editButton = new JButton("Edit");
		deleteButton = new JButton("Delete");
		refreshButton = new JButton("Refresh");
		updateButton =  new JButton("Update");
		loadEmployeeButton = new JButton("Load Employee");
		
		addButton.setVisible(isAdmin || isManager);
		editButton.setVisible(isAdmin || isManager);
		deleteButton.setVisible(isAdmin || isManager);
		updateButton.setVisible(isEmployee);
		loadEmployeeButton.setVisible(isAdmin || isManager);
		
		buttonPanel.add(addButton);
		buttonPanel.add(editButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(updateButton);
		buttonPanel.add(refreshButton);
		buttonPanel.add(loadEmployeeButton);
		
		formPanel.add(buttonPanel);
		
		addButton.addActionListener(e -> addTask());
		editButton.addActionListener(e -> editTask());
		deleteButton.addActionListener(e -> deleteTask());
		refreshButton.addActionListener(e -> loadTasks());
		updateButton.addActionListener(e -> editTask());
		loadEmployeeButton.addActionListener(e -> loadEmployees());
		
		if(isEmployee) {
			taskTitleField.setEnabled(false);
			priorityCombobox.setEnabled(false);
			dueDateChooser.setEnabled(false);
			assingnedByCombobox.setEnabled(false);
		}

		updateOtherComponents();
		return formPanel;
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
