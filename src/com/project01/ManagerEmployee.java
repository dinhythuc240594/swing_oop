package com.project01;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//public class ManagerEmployee extends SalaryEmployee {
//    private static final double MANAGER_BONUS = 500.0; // Additional bonus for managers
//
//    public ManagerEmployee(int employeeId, String position, Connection connection) {
//        super(employeeId, position, connection);
//    }
//
//    @Override
//    public double calculateTotalSalary() {
//        // Manager salary calculation: (base salary * position coefficient) + manager bonus
//        return (baseSalary * coefficient) + MANAGER_BONUS;
//    }
//
//    // Additional method for managers to adjust employee salaries
//    public boolean adjustEmployeeSalary(int targetEmployeeId, double newBaseSalary, double newCoefficient) {
//        SessionManager sessionManager = SessionManager.getInstance();
//        if (!sessionManager.isManager()) {
//            return false;
//        }
//
//        try {
//            String query = "UPDATE employees SET salary = ?, salary_coefficient = ? WHERE id = ?";
//            PreparedStatement pstmt = connection.prepareStatement(query);
//            pstmt.setDouble(1, newBaseSalary);
//            pstmt.setDouble(2, newCoefficient);
//            pstmt.setInt(3, targetEmployeeId);
//
//            int result = pstmt.executeUpdate();
//            pstmt.close();
//
//            return result > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//}