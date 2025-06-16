package com.project01;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

public class CaculatorSalaryScreen extends JPanel {
	private ResourceBundle messages;
	private static final String DEFAULT_LANGUAGE = "English";
	private String currentLanguage = DEFAULT_LANGUAGE;
	private JLabel titleTask, descriptionTask, assignedByTask, assignedToTask, priorityTask, statusTask, dueDateTask;
	private Connection connection;
	private SessionManager sessionManager;
	private DefaultTableModel tableModel;
	private JTable salaryTable;
	private static final String DEFAULT_AVATAR_PATH = "img/avatar.png";
	private HandlerImage hImage = new HandlerImage();

	public CaculatorSalaryScreen(Connection connection) {
		this.connection = connection;
		this.sessionManager = SessionManager.getInstance();
		this.messages = ResourceBundle.getBundle("messages", Locale.of("en"));
		initComponents();
		loadSalaryEmployees();
	}
	
	public void setLanguage(Locale locale) {
		this.messages = ResourceBundle.getBundle("messages", locale);
		updateUIText();
	}

	private void updateUIText() {

		// Update table headers
		String[] columns = {
				messages.getString("salary.column.photo"),
				messages.getString("salary.column.name"),
				messages.getString("salary.column.position"),
				messages.getString("salary.column.email"),
				messages.getString("salary.column.phone"),
				messages.getString("salary.column.identitynumber"),
				messages.getString("salary.column.salary"),
				messages.getString("salary.column.salarycoefficient"),
				messages.getString("salary.column.grosssalary")
		};
		tableModel.setColumnIdentifiers(columns);
		
//		addButton.setText(messages.getString("task.button.add"));
//		editButton.setText(messages.getString("task.button.edit"));
//		deleteButton.setText(messages.getString("task.button.delete"));
//		refreshButton.setText(messages.getString("task.button.refresh"));
//		updateButton.setText(messages.getString("task.button.update"));
		
		// Force table to update
		salaryTable.getTableHeader().repaint();
		
//		titleTask.setText(messages.getString("task.form.title"));
//		descriptionTask.setText(messages.getString("task.form.description"));
//		if(sessionManager.isEmployee()) {
//			assignedByTask.setText(messages.getString("task.form.assigned_by"));
//		} else {
//			assignedToTask.setText(messages.getString("task.form.assigned_to"));
//		}
//		
//		priorityTask.setText(messages.getString("task.form.priority"));
//		statusTask.setText(messages.getString("task.form.status"));
//		dueDateTask.setText(messages.getString("task.form.due_date"));
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		loadUserLanguage();

		String[] columns = {
			messages.getString("salary.column.photo"),
			messages.getString("salary.column.name"),
			messages.getString("salary.column.position"),
			messages.getString("salary.column.email"),
			messages.getString("salary.column.phone"),
			messages.getString("salary.column.identitynumber"),
			messages.getString("salary.column.salary"),
			messages.getString("salary.column.salarycoefficient"),
			messages.getString("salary.column.grosssalary")
		};
		
		//// 2025-05-31 - customize table model for employees table view ////
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// 2025-05-31 - only allow editing of action column for admin users 
				return column == 8 && sessionManager.isAdmin();
			}
			
			@Override
			public Class<?> getColumnClass(int column){
				if(column == 0) {
					return ImageIcon.class;
				}	
				return String.class;
			}
		};
		
		salaryTable = new JTable(tableModel);
		salaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		salaryTable.setRowHeight(50);

		salaryTable.getColumnModel().getColumn(0).setPreferredWidth(50); //PHOTO
		salaryTable.getColumnModel().getColumn(1).setPreferredWidth(150); // NAME
		salaryTable.getColumnModel().getColumn(2).setPreferredWidth(200); // EMAIL
		salaryTable.getColumnModel().getColumn(3).setPreferredWidth(100); // PHONE
		salaryTable.getColumnModel().getColumn(4).setPreferredWidth(80); // IDENTITY
		salaryTable.getColumnModel().getColumn(5).setPreferredWidth(100); // SALARY
		salaryTable.getColumnModel().getColumn(6).setPreferredWidth(100); // SALARY COEFFICIENT
		salaryTable.getColumnModel().getColumn(7).setPreferredWidth(100); // GROSS SALARY
				
		//// 2025-05-29 - customize center the photo column ////
		salaryTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		salaryTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
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
		
		JScrollPane tableScrollPanel = new JScrollPane(salaryTable);
		add(tableScrollPanel);
		
//		// create control panel
//		JPanel controlPanel = createControlPanel();
//		add(controlPanel, BorderLayout.EAST);

		// add table selection listener
		salaryTable.getSelectionModel().addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting()) {
				int selectedRow = salaryTable.getSelectedRow();
				if(selectedRow >= 0) {
//					populaField(selectedRow);
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
		
//		descriptionField = new JTextArea(4, 20);
//		JScrollPane desScroolPanel = new JScrollPane(descriptionField);
//		
//		employeeCombobox = new JComboBox<>();
//		employeeCombobox.setPreferredSize(new Dimension(100, employeeCombobox.getPreferredSize().height));
//		loadEmployees();
//		
//		assingnedByCombobox = new JComboBox<>();
//		assingnedByCombobox.setPreferredSize(new Dimension(100, assingnedByCombobox.getPreferredSize().height));
//		loadAssignedBy();
//		
//		priorityCombobox = new JComboBox<>(new String[] {"Low", "Medium", "High"});
//		priorityCombobox.setPreferredSize(new Dimension(100, priorityCombobox.getPreferredSize().height));
//		
//		statusCombobox = new JComboBox<>(new String[] {"Pending", "In Progress", "Completed"});
//		statusCombobox.setPreferredSize(new Dimension(100, statusCombobox.getPreferredSize().height));
//		
//		dueDateChooser = new JDateChooser();
//		dueDateChooser.setPreferredSize(new Dimension(100, dueDateChooser.getPreferredSize().height));
//		
//		addFormField(formPanel, titleTask = new JLabel(messages.getString("task.form.title")), taskTitleField = new JTextField(20));
//		addFormField(formPanel, descriptionTask = new JLabel(messages.getString("task.form.description")), desScroolPanel);
//		
//		if(isEmployee) {
//			addFormField(formPanel, assignedByTask = new JLabel(messages.getString("task.form.assigned_by")), assingnedByCombobox);
//		} else {
//			addFormField(formPanel, assignedToTask = new JLabel(messages.getString("task.form.assigned_to")), employeeCombobox);
//		}
//		
//		addFormField(formPanel, priorityTask = new JLabel(messages.getString("task.form.priority")), priorityCombobox);
//		addFormField(formPanel, statusTask = new JLabel(messages.getString("task.form.status")), statusCombobox);
//		addFormField(formPanel, dueDateTask = new JLabel(messages.getString("task.form.due_date")), dueDateChooser);
		
//		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		addButton = new JButton("Add");
//		editButton = new JButton("Edit");
//		deleteButton = new JButton("Delete");
//		refreshButton = new JButton("Refresh");
//		updateButton =  new JButton("Update");
//		
//		addButton.setVisible(isAdmin || isManager);
//		editButton.setVisible(isAdmin || isManager);
//		deleteButton.setVisible(isAdmin || isManager);
//		updateButton.setVisible(isEmployee);
//		
//		buttonPanel.add(addButton);
//		buttonPanel.add(editButton);
//		buttonPanel.add(deleteButton);
//		buttonPanel.add(updateButton);
//		buttonPanel.add(refreshButton);
//		
//		formPanel.add(buttonPanel);
		
//		addButton.addActionListener(e -> addTask());
//		editButton.addActionListener(e -> editTask());
//		deleteButton.addActionListener(e -> deleteTask());
//		refreshButton.addActionListener(e -> loadTasks());
//		updateButton.addActionListener(e -> editTask());
//		
//		if(isEmployee) {
//			taskTitleField.setEnabled(false);
//			priorityCombobox.setEnabled(false);
//			dueDateChooser.setEnabled(false);
//			assingnedByCombobox.setEnabled(false);
//		}
//
//		updateOtherComponents();
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
	
	private void loadSalaryEmployees() {
		try {
			
			tableModel.setRowCount(0);
			
			String query = "SELECT e.id, e.first_name, e.last_name, e.email, e.position, e.phone, " +
					"e.identity_number, e.photo, e.salary, e.salary_coefficient " +
					"FROM employees e";
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next()) {
				RegularEmployee regularEmployee = new RegularEmployee(rs.getInt("id"), rs.getString("position"), connection);
				int grossSalary = (int) regularEmployee.calculateTotalSalary();
				
				Vector<Object> row = new Vector<>();

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
				
				row.add(rs.getString("first_name") + " " + rs.getString("last_name"));
				row.add(rs.getString("position"));
				row.add(rs.getString("email"));
				row.add(rs.getString("phone"));
				row.add(rs.getString("identity_number"));
				row.add(rs.getString("salary"));
				row.add(rs.getString("salary_coefficient"));
				row.add(grossSalary);
				
				tableModel.addRow(row);
			}
			rs.close();
			st.close();
		} catch (Exception e) {
			e.printStackTrace();
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
	
	private void populaField(int row) {
//		taskTitleField.setText(tableModel.getValueAt(row, 1).toString());
//		descriptionField.setText(tableModel.getValueAt(row, 2).toString());
//		employeeCombobox.setSelectedItem(tableModel.getValueAt(row, 3).toString());
//		priorityCombobox.setSelectedItem(tableModel.getValueAt(row, 4).toString());
//		statusCombobox.setSelectedItem(tableModel.getValueAt(row, 5).toString());
//		dueDateChooser.setDate((java.util.Date) tableModel.getValueAt(row, 6));
	}
}
