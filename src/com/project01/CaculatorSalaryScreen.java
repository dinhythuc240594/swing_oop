package com.project01;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	private JButton viewDetailsButton;

	public CaculatorSalaryScreen(Connection connection) {
		this.connection = connection;
		this.sessionManager = SessionManager.getInstance();
		this.messages = ResourceBundle.getBundle("messages", Locale.of("en"));
		
		// Check if user has permission to view salary information
		if (!sessionManager.isManager() && !sessionManager.isAdmin()) {
			initNoPermissionComponents();
		} else {
			initComponents();
			loadSalaryEmployees();
		}
	}
	
	public void setLanguage(Locale locale) {
		this.messages = ResourceBundle.getBundle("messages", locale);
		updateUIText();
	}

	private void updateUIText() {

		// Update table headers
		String[] columns = {
				messages.getString("salary.column.id"),
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
		
		// Force table to update
		salaryTable.getTableHeader().repaint();
	}

	private void initNoPermissionComponents() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JLabel noPermissionLabel = new JLabel(
			"<html><center><h2>Access Denied</h2>" +
			"<p>Only managers can view salary information.</p>" +
			"<p>Please contact your manager if you need access to this feature.</p></center></html>",
			JLabel.CENTER
		);
		add(noPermissionLabel, BorderLayout.CENTER);
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		loadUserLanguage();

		String[] columns = {
			messages.getString("salary.column.id"),
			messages.getString("salary.column.photo"),
			messages.getString("salary.column.name"),
			messages.getString("salary.column.position"),
			"Level",
			messages.getString("salary.column.email"),
			messages.getString("salary.column.phone"),
			messages.getString("salary.column.identitynumber"),
			"Hire Date",
			messages.getString("salary.column.salary"),
			messages.getString("salary.column.salarycoefficient"),
			"Experience Level",
			messages.getString("salary.column.grosssalary"),
		};
		
		//// 2025-05-31 - customize table model for employees table view ////
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// Only allow editing for admin/manager users 
				return false; // Make read-only for now
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
		
		salaryTable.getColumnModel().getColumn(0).setPreferredWidth(50); //ID
		salaryTable.getColumnModel().getColumn(1).setPreferredWidth(50); //PHOTO
		salaryTable.getColumnModel().getColumn(2).setPreferredWidth(150); // NAME
		salaryTable.getColumnModel().getColumn(3).setPreferredWidth(100); // POSITION
		salaryTable.getColumnModel().getColumn(4).setPreferredWidth(200); // EMAIL
		salaryTable.getColumnModel().getColumn(5).setPreferredWidth(100); // PHONE
		salaryTable.getColumnModel().getColumn(6).setPreferredWidth(100); // IDENTITY
		salaryTable.getColumnModel().getColumn(7).setPreferredWidth(100); // HIRE DATE
		salaryTable.getColumnModel().getColumn(8).setPreferredWidth(80); // SALARY
		salaryTable.getColumnModel().getColumn(9).setPreferredWidth(80); // SALARY COEFFICIENT
		salaryTable.getColumnModel().getColumn(10).setPreferredWidth(100); // GROSS SALARY
				
		//// 2025-05-29 - customize center the photo column ////
		salaryTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		salaryTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
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
	
		salaryTable.getColumnModel().getColumn(0).setWidth(0);
		salaryTable.getColumnModel().getColumn(0).setMinWidth(0);
		salaryTable.getColumnModel().getColumn(0).setMaxWidth(0);
		
		//// 2025-05-30 - add mouse listener for row selection and delete button ////
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
			public void mouseExited(MouseEvent e) {
				salaryTable.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		
		JScrollPane tableScrollPanel = new JScrollPane(salaryTable);
		add(tableScrollPanel);
		
		// Create control panel with View Details button
		JPanel controlPanel = createControlPanel();
		add(controlPanel, BorderLayout.SOUTH);

		// add table selection listener
		salaryTable.getSelectionModel().addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting()) {
				int selectedRow = salaryTable.getSelectedRow();
				viewDetailsButton.setEnabled(selectedRow >= 0);
			}
		});
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
	
	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		viewDetailsButton = new JButton("View Salary Details");
		viewDetailsButton.setEnabled(false); // Disabled until a row is selected
		viewDetailsButton.addActionListener(e -> viewSalaryDetails());
		
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> loadSalaryEmployees());
		
		controlPanel.add(viewDetailsButton);
		controlPanel.add(refreshButton);
		
		return controlPanel;
	}
	
	private void viewSalaryDetails() {
		int dongDuocChon = salaryTable.getSelectedRow();
		if (dongDuocChon < 0) {
			return;
		}
		
		try {
			String tenNhanVien = (String) tableModel.getValueAt(dongDuocChon, 2);
			String viTri = (String) tableModel.getValueAt(dongDuocChon, 3);
			
			String sql = "SELECT e.id FROM employees e " +
						"WHERE CONCAT(e.first_name, ' ', e.last_name) = ? AND e.position = ?";
			PreparedStatement pstmt = connection.prepareStatement(sql);
			pstmt.setString(1, tenNhanVien);
			pstmt.setString(2, viTri);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				int maNhanVien = rs.getInt("id");
				
				// Sử dụng class ThongTinLuong riêng
				ThongTinLuong luong = SalaryCalculator.tinhLuong(maNhanVien, connection);
				
				javax.swing.JOptionPane.showMessageDialog(
					this,
					luong.xemChiTiet(),
					"Chi tiết lương - " + tenNhanVien,
					javax.swing.JOptionPane.INFORMATION_MESSAGE
				);
			}
			
			rs.close();
			pstmt.close();
		} catch (SQLException loi) {
			System.out.println("Lỗi khi xem chi tiết: " + loi.getMessage());
			javax.swing.JOptionPane.showMessageDialog(
				this,
				"Lỗi khi xem chi tiết lương: " + loi.getMessage(),
				"Lỗi",
				javax.swing.JOptionPane.ERROR_MESSAGE
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
					"INNER JOIN users u ON e.user_id = u.id";
			
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(sql);
			
			while(rs.next()) {
				int maNhanVien = rs.getInt("id");
				
				// Sử dụng class ThongTinLuong riêng
				ThongTinLuong luong = SalaryCalculator.tinhLuong(maNhanVien, connection);
				
				Vector<Object> dong = new Vector<>();

				// Xử lý ảnh đại diện
				ImageIcon icon;
				byte[] anhBytes = rs.getBytes("photo"); 
				if(anhBytes != null && anhBytes.length > 0) {
					icon = new ImageIcon(anhBytes);
				} else {
					anhBytes = hImage.fileImage(DEFAULT_AVATAR_PATH, 40, 40);
					if(anhBytes != null) {
						icon = new ImageIcon(anhBytes);
					} else {
						icon = new ImageIcon(Objects.requireNonNull(this.getClass().getResource(DEFAULT_AVATAR_PATH)));
					}
				}
				dong.add(maNhanVien);
				dong.add(icon);
				
				dong.add(rs.getString("first_name") + " " + rs.getString("last_name"));
				dong.add(rs.getString("position"));
				dong.add(luong.capBac.toUpperCase());
				dong.add(rs.getString("email"));
				dong.add(rs.getString("phone"));
				dong.add(rs.getString("identity_number"));
				dong.add(rs.getString("hire_date"));
				dong.add(String.format("%.0f", rs.getDouble("salary")));
				dong.add(String.format("%.1f", luong.heSoViTri));
				dong.add(luong.tenKinhNghiem);
				dong.add(String.format("%.0f", luong.luongCuoiCung));
				
				tableModel.addRow(dong);
			}
			rs.close();
			st.close();
		} catch (Exception loi) {
			System.out.println("Lỗi khi load dữ liệu: " + loi.getMessage());
			loi.printStackTrace();
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
	




}
