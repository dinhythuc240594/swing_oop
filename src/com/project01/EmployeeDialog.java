package com.project01;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import java.util.*;
import java.util.regex.Pattern;

import com.toedter.calendar.JDateChooser;
import java.util.Locale;
import java.util.ResourceBundle;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;


public class EmployeeDialog extends JDialog {
	
	//// 2025-06-01 - inhered JDialog to create employee detail ////
	////	private JDialog dialog;
	private boolean employeeAdded = false;
	private boolean employeeUpdated = false;
	private Connection connection;
	private int employeeId = -1;
	private JTextField firstNameField;
	private JTextField lastNameField;
	private JTextField emailField;
	private JTextField phoneField;
	private JComboBox<String> positionField;
	private JDateChooser hireDateField;
	private JPasswordField passwordField;
	private JTextField usernameField;
	private JButton saveButton;
	private JButton cancelButton;
	private JComboBox<String> roleField;
	private JComboBox<String> manageField;
	private JLabel imagePreviewLabel;
	private JButton uploadImageButton;
	private byte[] avatarImage;
	private static final String DEFAULT_AVATAR_PATH = "img/avatar.png";
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
	private HandlerImage hImage = new HandlerImage();
	private JLabel emailErrorLabel;
	private JLabel usernameErrorLabel;
	private JLabel identityNumberErrorLabel;
	private JLabel salaryErrorLabel;
	private ResourceBundle messages;
	private static final String DEFAULT_LANGUAGE = "English";
	private String currentLanguage = DEFAULT_LANGUAGE;
	private JComboBox<String> departmentField;
	private JTextComponent identityNumber;
	private JTextComponent addressField;
	private JComboBox<String> cityField;
	private JComboBox<String> stateField;
	private JComboBox<String> countryField;
	private JTextComponent postalCodeField;
	private JComboBox<String> maritalStatusField;
	private JTextComponent spouseNameField;
	private int numberOfChildrenField;
	private JTextComponent emergencyContactNameField;
	private JTextComponent emergencyContactPhoneField;
	private JTextField identityNumberField;
	private JTextField salaryField;
	private JTextField salaryCoefficientField;
	private JTextField grosssalary;
	private JTextComponent experienceField;
	
	// Modern color scheme
	private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
	private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
	private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
	private static final Color DANGER_COLOR = new Color(231, 76, 60);
	private static final Color LIGHT_GRAY = new Color(245, 245, 245);
	private static final Color MEDIUM_GRAY = new Color(189, 195, 199);
	private static final Color DARK_GRAY = new Color(52, 73, 94);
	private static final Color WHITE = Color.WHITE;
	
	public EmployeeDialog(JFrame parent, Connection conn) {
		this(parent, conn, -1);
		this.messages = ResourceBundle.getBundle("messages", Locale.of("en"));
	}
	
	public EmployeeDialog(JFrame parent, Connection conn, int employee_id) {
		
		super(parent, "", true);

		this.connection = conn;
		this.employeeId = employee_id;
		
		loadUserLanguage();

		setTitle(messages.getString("employee.dialog.title"));
		
		setSize(700, 900);
		setLocationRelativeTo(parent);
		setLayout(new BorderLayout());
		setResizable(true);
		
		// Enable double buffering for smoother rendering
		((JComponent) getContentPane()).setDoubleBuffered(true);
		
		// Set modern look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SessionManager sessionManager = SessionManager.getInstance();
		if(sessionManager.isAdmin()) {
			EmployeeDialogAdmin(parent, conn, employee_id, sessionManager);
		} else if(sessionManager.isManager()) {
			EmployeeDialogSalary(parent, conn, employee_id, sessionManager);
		} else {
			EmployeeDialogUser(parent, conn, employee_id, sessionManager);
		}
		updateOtherComponents();
	}
		
	public void EmployeeDialogAdmin(JFrame parent, Connection conn, int employee_Id, SessionManager sessionManager) {
	
		// create form panel
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		formPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(20, 20, 20, 20),
			BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true)
		));
		formPanel.setOpaque(true);
		formPanel.setBackground(new Color(248, 249, 250));

		createForm(formPanel, sessionManager);
		
		// add button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
		buttonPanel.setBackground(new Color(248, 249, 250));
		
		saveButton = createModernButton("Save", PRIMARY_COLOR, 80, 30);
		saveButton.addActionListener(e -> saveEmployee());
		saveButton.setVisible(sessionManager.isAdmin());
		styleButton(saveButton, new Color(40, 167, 69), Color.WHITE);
		
		cancelButton = createModernButton("Cancel", PRIMARY_COLOR, 80, 30);
		cancelButton.addActionListener(e -> dispose());
		styleButton(cancelButton, new Color(108, 117, 125), Color.WHITE);
		
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		
		JScrollPane scrollPanel = new JScrollPane(formPanel);
		scrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPanel.getVerticalScrollBar().setUnitIncrement(16);
		scrollPanel.getHorizontalScrollBar().setUnitIncrement(16);
		scrollPanel.setWheelScrollingEnabled(true);
		scrollPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		scrollPanel.getViewport().setBackground(new Color(248, 249, 250));
		
		add(scrollPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		// load employee data if editing
		if(employee_Id != -1) {
			loadDetailEmployeeData();
			
			if(!sessionManager.isAdmin()) {
				firstNameField.setEditable(false);
				lastNameField.setEditable(false);
				emailField.setEditable(false);
				phoneField.setEditable(false);
				positionField.setEnabled(false);
				hireDateField.setEnabled(false);
				uploadImageButton.setEnabled(false);
			}	
		}
		
	}
	
	public void EmployeeDialogUser(JFrame parent, Connection conn, int employee_Id, SessionManager sessionManager) {
		
		// create form panel
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		createForm(formPanel, sessionManager);
		
		// add button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		saveButton = new JButton("Save");
		saveButton.addActionListener(e -> saveEmployee());
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		
		add(formPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		loadDetailEmployeeData();
		positionField.setEnabled(false);
		hireDateField.setEnabled(false);
	}
	
	public void EmployeeDialogSalary(JFrame parent, Connection conn, int employee_Id, SessionManager sessionManager) {
		
		// create form panel
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		createForm(formPanel, sessionManager);
		
		// add button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		saveButton = new JButton("Save");
		saveButton.addActionListener(e -> saveEmployee());
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		
		add(formPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		loadDetailEmployeeData();
		positionField.setEnabled(false);
		hireDateField.setEnabled(false);
	}
	
	private void createForm(JPanel formPanel, SessionManager sessionManager) {
		
		//// 2025-05-29 - except GridBagConstraints ////
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		avatarPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(10, 0, 20, 0),
			BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true)
		));
		avatarPanel.setBackground(new Color(255, 255, 255));
		
		JLabel avatarLabel = new JLabel(messages.getString("employee.dialog.avatar"));
		avatarLabel.setPreferredSize(new Dimension(150, 25));
		avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
		avatarLabel.setForeground(new Color(73, 80, 87));
		
		// image preview
		imagePreviewLabel = new JLabel();
		imagePreviewLabel.setPreferredSize(new Dimension(150, 150));
		imagePreviewLabel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
			BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));
		imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		imagePreviewLabel.setBackground(new Color(248, 249, 250));
		imagePreviewLabel.setOpaque(true);
		
		// upload button
		uploadImageButton = new JButton("Upload Photo");
		uploadImageButton.addActionListener(e -> uploadImage());
		styleButton(uploadImageButton, new Color(0, 123, 255), Color.WHITE);
	
		// add panels to dialog
		avatarPanel.add(avatarLabel);
		avatarPanel.add(imagePreviewLabel);
		avatarPanel.add(uploadImageButton);
		
		formPanel.add(avatarPanel);
		formPanel.add(Box.createVerticalStrut(20));
		
		// Add section header
		JLabel sectionHeader = new JLabel("Personal Information");
		sectionHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
		sectionHeader.setForeground(new Color(52, 58, 64));
		sectionHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
		formPanel.add(sectionHeader);
		formPanel.add(Box.createVerticalStrut(10));
		
		// 2025-05-31 - create combobox for position field
		positionField = new JComboBox<>(new String[] {"Developer", "Designer", "QA", "QC"});
		positionField.setPreferredSize(new Dimension(200, positionField.getPreferredSize().height));
		
		// 2025-05-31 - create datepicker for hired field
		hireDateField = new JDateChooser();
		hireDateField.setPreferredSize(new Dimension(200, hireDateField.getPreferredSize().height));
		hireDateField.setDateFormatString("yyyy-MM-dd");
		
		// 2025-05-31 - create email format for email field
		emailField = new JTextField(24);
		emailField.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				String email = emailField.getText().trim();
				if(email.isEmpty()) {
					emailErrorLabel.setText("");
					return true;
				}
				boolean isValid = EMAIL_PATTERN.matcher(email).matches();
				if(!isValid) {
					emailErrorLabel.setText("Please enter a valid email address (e.g, user@company.com)");
				} else {
					emailErrorLabel.setText("");
				}
				return isValid;
			}
		});
		
		// Add error labels
		emailErrorLabel = new JLabel("");
		emailErrorLabel.setForeground(new Color(220, 53, 69));
		emailErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		
		usernameErrorLabel = new JLabel("");
		usernameErrorLabel.setForeground(new Color(220, 53, 69));
		usernameErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		
		identityNumberErrorLabel = new JLabel("");
		identityNumberErrorLabel.setForeground(new Color(220, 53, 69));
		identityNumberErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		
		// 2025-05-31 - create combobox for type user field
		roleField = new JComboBox<>(new String[] {"Manager", "Employee"});
		roleField.setPreferredSize(new Dimension(200, roleField.getPreferredSize().height));
		
		// add form field
		addFormField(formPanel, messages.getString("employee.dialog.firstname"), firstNameField = new JTextField(24));
		addFormField(formPanel, messages.getString("employee.dialog.lastname"), lastNameField = new JTextField(24));
		addFormField(formPanel, messages.getString("employee.dialog.email"), emailField);
		formPanel.add(emailErrorLabel);
		formPanel.add(Box.createVerticalStrut(5));
		addFormField(formPanel, messages.getString("employee.dialog.phone"), phoneField = new JTextField(24));
		addFormField(formPanel, messages.getString("employee.dialog.position"), positionField);
		addFormField(formPanel, messages.getString("employee.dialog.hiredate"), hireDateField);
		
		// 2025-06-21 - create combobox for department field
		departmentField = new JComboBox<>(new String[] {"Technology", "Human Resource", "Accounting"});
		departmentField.setPreferredSize(new Dimension(200, departmentField.getPreferredSize().height));
		
		addFormField(formPanel, "Department", departmentField);
		
		addFormField(formPanel, "ID", identityNumber = new JTextField(24));
		addFormField(formPanel, "Address", addressField = new JTextField(24));
		addFormField(formPanel, "Postal Code", postalCodeField = new JTextField(24));
		
		cityField = new JComboBox<>(new String[] {"Technology", "Human Resource", "Accounting"});
		cityField.setPreferredSize(new Dimension(200, cityField.getPreferredSize().height));
		stateField = new JComboBox<>(new String[] {"Technology", "Human Resource", "Accounting"});
		stateField.setPreferredSize(new Dimension(200, stateField.getPreferredSize().height));
		countryField = new JComboBox<>(new String[] {"Technology", "Human Resource", "Accounting"});
		countryField.setPreferredSize(new Dimension(200, countryField.getPreferredSize().height));
		
		addFormField(formPanel, "Country", countryField);
		addFormField(formPanel, "City", cityField);
		addFormField(formPanel, "State", stateField);
		
		
		maritalStatusField = new JComboBox<>(new String[] {"Technology", "Human Resource", "Accounting"});
		maritalStatusField.setPreferredSize(new Dimension(200, departmentField.getPreferredSize().height));
		
		addFormField(formPanel, "Marital Status", maritalStatusField);
		addFormField(formPanel, "Spouse Name", spouseNameField = new JTextField(24));
		
		JTextField textNumber = new JTextField(24);
		textNumber.setInputVerifier(new InputVerifier() {
	        @Override
	        public boolean verify(JComponent input) {
	            JTextField tf = (JTextField) input;
	            try {
	                tf.setBackground(Color.WHITE); // Reset background if valid
	                numberOfChildrenField = Integer.parseInt(tf.getText());
	                return true;
	            } catch (NumberFormatException e) {
	                tf.setBackground(Color.RED); // Indicate error visually
	                return false;
	            }
	        }
	    });

		addFormField(formPanel, "Number of children", textNumber);
		addFormField(formPanel, "Emergency Contact Name", emergencyContactNameField = new JTextField(24));
		addFormField(formPanel, "Emergency Contact Phone", emergencyContactPhoneField = new JTextField(24));
		
		experienceField = new JTextField(24);
		addFormField(formPanel, messages.getString("salary.column.experience"), experienceField);
		addFormField(formPanel, messages.getString("salary.column.identitynumber"), identityNumberField = new JTextField(24));
		SalaryInfo salaryInfo = SalaryCalculator.calculateSalary(this.employeeId, this.connection);
		experienceField.setText(salaryInfo.level);
		experienceField.setEnabled(false);
		if(sessionManager.isAdmin()) {
			addFormField(formPanel, messages.getString("employee.dialog.username"), usernameField = new JTextField(24));
			formPanel.add(usernameErrorLabel);
			formPanel.add(Box.createVerticalStrut(5));
			addFormField(formPanel, messages.getString("employee.dialog.password"), passwordField = new JPasswordField(24));
			addFormField(formPanel, messages.getString("employee.dialog.level"), roleField);
			
			//// 2025-06-21 - add listener if role is Employee ////
			//// user has level is manager will managed by administrator ////
			//// and who is employee will managed by Manger or administrator ////
			roleField.addItemListener(new ItemListener() {
	            @Override
	            public void itemStateChanged(ItemEvent e) {
	                if (e.getStateChange() == ItemEvent.SELECTED) {
	                    String selectedItem = (String) e.getItem();
	                    if(selectedItem.equals("Employee")) {
	                    	manageField.setEnabled(true);
	                    }
	                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
	                    String deselectedItem = (String) e.getItem();
	                    System.out.println("Item deselected: " + deselectedItem);
	                }
	            }
	        });
			
			manageField = new JComboBox<>(new String[] {});
			manageField.setPreferredSize(new Dimension(200, roleField.getPreferredSize().height));
			loadManager();
			addFormField(formPanel, messages.getString("employee.dialog.managedby"), manageField);
			manageField.setEnabled(false);
			
		} else if(sessionManager.isEmployee()) {
			grosssalary = new JTextField(24);
			addFormField(formPanel, messages.getString("salary.column.grosssalary"), grosssalary);
			grosssalary.setText(String.valueOf(salaryInfo.finalSalary));
			grosssalary.setEnabled(false);
			emailField.setEnabled(false);
		} else {
			
	        salaryField = new JTextField(24);
	        salaryField.setText(String.valueOf(salaryInfo.basicSalary));
	        salaryErrorLabel = new JLabel("");
	        salaryErrorLabel.setForeground(new Color(220, 53, 69));
	        salaryErrorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
			formPanel.add(salaryErrorLabel);
			formPanel.add(Box.createVerticalStrut(5));
	        addFormField(formPanel, messages.getString("salary.column.salary"), salaryField);
			addFormField(formPanel, messages.getString("salary.column.salarycoefficient"), salaryCoefficientField = new JTextField(24));
			Double salary = SalaryCalculator.getPositionCoefficient(positionField.getSelectedItem().toString());
			salaryCoefficientField.setText(String.valueOf(salary));
			salaryCoefficientField.setEnabled(false);

			grosssalary = new JTextField(24);
			addFormField(formPanel, messages.getString("salary.column.grosssalary"), grosssalary);
			grosssalary.setText(String.valueOf(salaryInfo.finalSalary));
			grosssalary.setEnabled(false);
			
	        salaryField.setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(JComponent input) {
					String salary = salaryField.getText().trim();
					String coefficient = salaryCoefficientField.getText().trim();
	                try {
	                    Double number_salary = Double.parseDouble(salary);
	                    Double number_coefficient = Double.parseDouble(coefficient);
	                    Double salary_amount = number_salary*number_coefficient;
	                    grosssalary.setText(String.valueOf(salary_amount));
	                    return true;
	                } catch (NumberFormatException ex) {
	                	salaryErrorLabel.setText("Invalid input, please input number");
	                    return false;
	                }
				}
			});
		}

		// add table to scroll pane
		JPanel employeePanel = new JPanel(new BorderLayout());
		employeePanel.add(formPanel, BorderLayout.CENTER);
		
	}
	
	private void addFormField(JPanel panel, String label, JComponent field) {
		
		// parameter gbc except
		
		if(field instanceof JPasswordField && employeeId != -1) {
			field.setEnabled(false);
		}
		
		JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
		fieldPanel.setOpaque(false);
		fieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		JLabel labelComponent = new JLabel(label);
		labelComponent.setPreferredSize(new Dimension(180, 30));
		labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 12));
		labelComponent.setForeground(new Color(73, 80, 87));
		
		// Style the field
		if (field instanceof JTextField) {
			styleTextField((JTextField) field);
		} else if (field instanceof JComboBox) {
			styleComboBox((JComboBox<?>) field);
		} else if (field instanceof JPasswordField) {
			styleTextField((JPasswordField) field);
		} else if (field instanceof JDateChooser) {
			styleDateChooser((JDateChooser) field);
		}
		
		fieldPanel.add(labelComponent);
		fieldPanel.add(field);
		panel.add(fieldPanel);
		panel.add(Box.createVerticalStrut(8));
	}
		
	public void loadDetailEmployeeData() {
		System.out.println("loadDetailEmployeeData");
		try {
			//// 2025-05-30 - query employees base on user is admin or manager
			//// error get employee
			String query;
			PreparedStatement prstmt;
			SessionManager sessionManager = SessionManager.getInstance();
			if(sessionManager.isAdmin() || sessionManager.isManager()) {
				query = "SELECT e.*, u.username, u.password, u.role, m.first_name as m_first_name, m.last_name as m_last_name " +
						"FROM employees e " +
						"JOIN users u ON e.user_id = u.id " +
						"JOIN managers m ON e.manager_id = m.id " +
						"WHERE e.id = ?";				
			} else {
				query = "SELECT e.*" +
						"FROM employees e " +
						"JOIN users u ON e.user_id = u.id " +
						"WHERE e.id = ?";

			}
			
			prstmt = connection.prepareStatement(query);
			prstmt.setInt(1, employeeId);
			ResultSet rs = prstmt.executeQuery();
			
			if(rs.next()) {
				firstNameField.setText(rs.getString("first_name"));
				lastNameField.setText(rs.getString("last_name"));
				emailField.setText(rs.getString("email"));
				phoneField.setText(rs.getString("phone"));
				
				positionField.setSelectedItem(rs.getString("position"));
				identityNumberField.setText(rs.getString("identity_number"));
				String hireDateStr = rs.getString("hire_date");
				if(hireDateStr != null && !hireDateStr.isEmpty()) {
					try {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						Date hireDate = dateFormat.parse(hireDateStr);
						hireDateField.setDate(hireDate);
					} catch (Exception e) {
						System.out.print("Error parsing hiredate: " + e.getMessage());
					}
				}

				//// 2025-05-31 - just only allow admin user view username and password field ////
				if(sessionManager.isAdmin()) {
					usernameField.setText(rs.getString("username"));
					// disable field password for security reasons
					passwordField.setText(rs.getString("password"));
					String upperRole = rs.getString("role").substring(0, 1).toUpperCase() + rs.getString("role").substring(1);
					roleField.setSelectedItem(upperRole);
					
					String manager = rs.getString("m_first_name") + " " + rs.getString("m_last_name");
					manageField.setSelectedItem(manager);
				}
				
				//// 2025-05-29 - load avatar if exists else set default avatar icon ////
				ImageIcon icon;
				byte[] bytePhoto;
				bytePhoto = rs.getBytes("photo"); 
				if(bytePhoto != null && bytePhoto.length > 0) {
					icon = new ImageIcon(bytePhoto);
				} else {
					bytePhoto = hImage.fileImage(DEFAULT_AVATAR_PATH, 200, 200);
					if(bytePhoto != null) {
						icon = new ImageIcon(bytePhoto);
					} else {
						icon = new ImageIcon(Objects.requireNonNull(this.getClass().getResource(DEFAULT_AVATAR_PATH)));
					}
				}
				imagePreviewLabel.setIcon(icon);
				imagePreviewLabel.setText("");
				avatarImage = bytePhoto;
			}
			
			rs.close();
			prstmt.close();
		}catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error loading employee data: " + e.getMessage());
		}
	}
	
	private void saveEmployee() {
		System.out.println("saveEmployee: " + employeeId);
		try {
			SessionManager sessionManager = SessionManager.getInstance();
			
			String email = emailField.getText().trim();
			if(!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
				emailErrorLabel.setText("Please enter a valid email address");
				return;
			}

			connection.setAutoCommit(false);
			if(employeeId == -1) {
				
				// insert new employee
				// insert user first
				String userQuery;
				PreparedStatement userStmt;
			
				if(sessionManager.isAdmin()) {
					userQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
					userStmt = connection.prepareStatement(userQuery);
					userStmt.setString(1, usernameField.getText());
					userStmt.setString(2, new String(passwordField.getPassword()));
					userStmt.setString(3, roleField.getSelectedItem().toString().toLowerCase());
				} else {
					userQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, 'employee')";
					userStmt = connection.prepareStatement(userQuery);
					userStmt.setString(1, usernameField.getText());
					userStmt.setString(2, new String(passwordField.getPassword()));
				}

				try {
					userStmt.executeUpdate();
				} catch (SQLException e) {
					if (e.getMessage().contains("UNIQUE constraint failed: users.username")) {
						usernameErrorLabel.setText("Username already exists");
						return;
					}
					throw e;
				}
				
				boolean isManager = roleField.getSelectedItem().toString().toLowerCase().equals("manager");
				ResultSet rs = userStmt.getGeneratedKeys();
				if(rs.next()) {
					// insert employee
					int userId = rs.getInt(1);
					String employeeQuery;
					if(isManager) {
						employeeQuery = "INSERT INTO employees (user_id, first_name, last_name," +
																" email, phone, position," +
																" hire_date, photo," +
																" department, identity_number, address," + 
																" city, state, country," + 
																" postal_code, marital_status, spouse_name," +
																" number_of_children, emergency_contact_name, emergency_contact_phone) " +
										"VALUES (?, ?, ?," +
												" ?, ?, ?," +
												" ?, ?," + 
												" ?, ?, ?," + 
												" ?, ?, ?," + 
												" ?, ?, ?," +
												" ?, ?, ?)";
					} else {
						employeeQuery = "INSERT INTO employees (user_id, first_name, last_name," +
																" email, phone, position," +
																" hire_date, photo," +
																" department, identity_number, address," + 
																" city, state, country," + 
																" postal_code, marital_status, spouse_name," +
																" number_of_children, emergency_contact_name, emergency_contact_phone," + 
																" manager_id) " +
										"VALUES (?, ?, ?," +
												" ?, ?, ?," +
												" ?, ?," + 
												" ?, ?, ?," + 
												" ?, ?, ?," + 
												" ?, ?, ?," +
												" ?, ?, ?," + 
												" ?)";	
					}
					
					PreparedStatement employeeStmt = connection.prepareStatement(employeeQuery);
					employeeStmt.setInt(1, userId);
					employeeStmt.setString(2, firstNameField.getText());
					employeeStmt.setString(3, lastNameField.getText());
					employeeStmt.setString(4, email);
					employeeStmt.setString(5, phoneField.getText());
					employeeStmt.setString(6, positionField.getSelectedItem().toString());
						
					// 2025-05-31 - format hire date
					String hireDateStr = "";
					if(hireDateField.getDate() != null) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						hireDateStr = dateFormat.format(hireDateField.getDate());
					}
					employeeStmt.setString(7, hireDateStr);
						
					employeeStmt.setBytes(8, avatarImage);

					employeeStmt.setString(9, departmentField.getSelectedItem().toString());
					employeeStmt.setString(10, identityNumber.getText());
					employeeStmt.setString(11, addressField.getText());
					employeeStmt.setString(12, cityField.getSelectedItem().toString());
					employeeStmt.setString(13, stateField.getSelectedItem().toString());
					employeeStmt.setString(14, countryField.getSelectedItem().toString());
					employeeStmt.setString(15, postalCodeField.getText());
					employeeStmt.setString(16, maritalStatusField.getSelectedItem().toString());
					employeeStmt.setString(17, spouseNameField.getText());
					employeeStmt.setInt(18, numberOfChildrenField);
					employeeStmt.setString(19, emergencyContactNameField.getText());
					employeeStmt.setString(20, emergencyContactPhoneField.getText());

					if(!isManager) {
						employeeStmt.setInt(21, getManagerId((String) manageField.getSelectedItem()));
					}
					
					employeeStmt.setInt(22, getManagerId((String) manageField.getSelectedItem().toString()));
					
					try {
						employeeStmt.executeUpdate();
					} catch (SQLException e) {
						if (e.getMessage().contains("UNIQUE constraint failed: employees.email")) {
							emailErrorLabel.setText("Email already exists");
							return;
						}
						throw e;
					}
					
					if(isManager) {
						// insert manager
						String managerQuery = "INSERT INTO managers (user_id, first_name, last_name, email, phone, department, photo) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?)";
						PreparedStatement managerStmt = connection.prepareStatement(managerQuery);
						managerStmt.setInt(1, userId);
						managerStmt.setString(2, firstNameField.getText());
						managerStmt.setString(3, lastNameField.getText());
						managerStmt.setString(4, email);
						managerStmt.setString(5, phoneField.getText());
						managerStmt.setString(6, departmentField.getSelectedItem().toString());
						managerStmt.setBytes(7, avatarImage);
					}
					
					connection.commit();
					employeeAdded = true;
				}
				
				rs.close();
				userStmt.close();
			} else {
					
				PreparedStatement stmt;
				String usersQuery;
				String query;
				PreparedStatement pstmt;
	
				if(sessionManager.isEmployee()) {
					// update employee
					query = "UPDATE employees SET first_name = ?, last_name = ?, phone = ?, position = ?, hire_date = ?, photo = ?, identity_number = ? " +
									"WHERE id = ?";
					pstmt = connection.prepareStatement(query);

					pstmt.setString(1, firstNameField.getText());
					pstmt.setString(2, lastNameField.getText());
					pstmt.setString(3, phoneField.getText());
					pstmt.setString(4, positionField.getSelectedItem().toString());
					
					//// 2025-05-31 - format hire date ////
					String hireDateStr = "";
					if(hireDateField.getDate() != null) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						hireDateStr = dateFormat.format(hireDateField.getDate());
					}
					pstmt.setString(5, hireDateStr);
					pstmt.setBytes(6, avatarImage);
					pstmt.setString(7, identityNumberField.getText());
					pstmt.setInt(8, employeeId);
					pstmt.executeUpdate();
					pstmt.close();
				} else if(sessionManager.isAdmin()) {
					
					// update employee
					query = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, phone = ?, position = ?, hire_date = ?, photo = ?, manager_id = ?, identity_number = ? " +
									"WHERE id = ?";
					pstmt = connection.prepareStatement(query);

					pstmt.setString(1, firstNameField.getText());
					pstmt.setString(2, lastNameField.getText());
					pstmt.setString(3, email);
					pstmt.setString(4, phoneField.getText());
					pstmt.setString(5, positionField.getSelectedItem().toString());
					
					//// 2025-05-31 - format hire date ////
					String hireDateStr = "";
					if(hireDateField.getDate() != null) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						hireDateStr = dateFormat.format(hireDateField.getDate());
					}
					pstmt.setString(6, hireDateStr);
					pstmt.setBytes(7, avatarImage);
					pstmt.setInt(8, getManagerId((String) manageField.getSelectedItem().toString()));
					pstmt.setString(9, identityNumberField.getText());
					pstmt.setInt(10, employeeId);
					pstmt.executeUpdate();
					pstmt.close();
					
					// update user name, password and role if changed
					if(!usernameField.getText().isEmpty() && !roleField.getSelectedItem().toString().isEmpty()) {
						usersQuery = "UPDATE users SET username = ?, password = ?, role = ?  WHERE id = (SELECT user_id FROM employees WHERE id = ?)";
						stmt = connection.prepareStatement(usersQuery);
						stmt.setString(1, usernameField.getText());
						stmt.setString(2, new String(passwordField.getPassword()));
						stmt.setString(3, roleField.getSelectedItem().toString().toLowerCase());
						stmt.setInt(4, employeeId);
						stmt.executeUpdate();
						stmt.close();
					}
				} else if(sessionManager.isManager()) {
					// update employee
					query = "UPDATE employees SET first_name = ?, last_name = ?, email = ?," + 
													" phone = ?, position = ?, hire_date = ?," + 
													" photo = ?, identity_number = ?," +
													" salary = ?, salary_coefficient = ?" +
													" WHERE id = ?";
					pstmt = connection.prepareStatement(query);

					pstmt.setString(1, firstNameField.getText());
					pstmt.setString(2, lastNameField.getText());
					pstmt.setString(3, email);
					pstmt.setString(4, phoneField.getText());
					pstmt.setString(5, positionField.getSelectedItem().toString());
					//// 2025-05-31 - format hire date ////
					String hireDateStr = "";
					if(hireDateField.getDate() != null) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						hireDateStr = dateFormat.format(hireDateField.getDate());
					}
					pstmt.setString(6, hireDateStr);
					pstmt.setBytes(7, avatarImage);
					pstmt.setString(8, identityNumberField.getText());
					pstmt.setDouble(9, Double.parseDouble(salaryField.getText().trim()));
					pstmt.setDouble(10, Double.parseDouble(salaryCoefficientField.getText().trim()));
					pstmt.setInt(11, employeeId);
					pstmt.executeUpdate();
					pstmt.close();
				}
				employeeUpdated = true;
			}
			JOptionPane.showMessageDialog(this, "Saving employee successfully");
			dispose();
		}catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
//			emailErrorLabel.setText("Error saving employee: " + e.getMessage());
			JOptionPane.showMessageDialog(this, "Error saving employee: " + e.getMessage());
		} 
		finally {
			try {
				connection.setAutoCommit(true);
//				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void uploadImage() {
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif");
		fileChooser.setFileFilter(filter);
		
		int result = fileChooser.showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				// read and resize image
				
				BufferedImage originalImage = ImageIO.read(selectedFile);
				BufferedImage resizedImage = hImage.resizeImage(originalImage, 200, 200);

				// update preview
				ImageIcon icon = new ImageIcon(resizedImage.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
				imagePreviewLabel.setIcon(icon);
				imagePreviewLabel.setText("");
				
				// convert to byte array
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(resizedImage, "PNG", baos);
				avatarImage = baos.toByteArray();
				
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage());
			}
		}
	}
	
	public boolean isEmployeeAdded() {
		return employeeAdded;
	}
	
	public boolean isEmployeeUpdated() {
		return employeeUpdated;
	}
	
	private void loadUserLanguage() {
		SessionManager sessionManager = SessionManager.getInstance();
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
	
	////2025-06-21 - load item manager if role is Employee ////
	private void loadManager() {
		try {
			manageField.removeAllItems();
			String query = "SELECT id, first_name, last_name FROM managers";
			PreparedStatement stmt = connection.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				String name = rs.getString("first_name") + " " + rs.getString("last_name");
				manageField.addItem(name);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	////2025-06-21 - get manager id before add into employee ////
	private int getManagerId(String fullName) throws SQLException {
		String[] names = fullName.split(" ");
		String query = "SELECT id FROM managers WHERE first_name = ? AND last_name = ?";
		PreparedStatement stmt = connection.prepareStatement(query);
		stmt.setString(1, names[0]);
		stmt.setString(2, names[1]);
		ResultSet rs = stmt.executeQuery();
		int id = -1;
		if(rs.next()) {
			id = rs.getInt("id");
		}
		System.out.println(id);
		rs.close();
		stmt.close();
		return id;
	}
	
	private void styleButton(JButton button, Color backgroundColor, Color textColor) {
		button.setBackground(backgroundColor);
		button.setForeground(textColor);
		button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		button.setFont(new Font("Segoe UI", Font.BOLD, 12));
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	
	private void styleTextField(JTextField textField) {
		textField.setPreferredSize(new Dimension(250, 35));
		textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		textField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
			BorderFactory.createEmptyBorder(8, 12, 8, 12)
		));
	}
	
	private void styleTextField(JPasswordField passwordField) {
		passwordField.setPreferredSize(new Dimension(250, 35));
		passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		passwordField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
			BorderFactory.createEmptyBorder(8, 12, 8, 12)
		));
	}
	
	private void styleComboBox(JComboBox<?> comboBox) {
		comboBox.setPreferredSize(new Dimension(265, 35));
		comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		comboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
	}
	
	private void styleDateChooser(JDateChooser dateChooser) {
		dateChooser.setPreferredSize(new Dimension(265, 35));
		dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		dateChooser.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
	}
	
	private void updateOtherComponents() {
		
		// Update new employee button
		saveButton.setText(messages.getString("employee.dialog.save"));
		uploadImageButton.setText(messages.getString("employee.dialog.upload"));
		cancelButton.setText(messages.getString("employee.dialog.cancel"));
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
		button.setPreferredSize(new Dimension(Width, Height));
		
		return button;
	}
	
}
