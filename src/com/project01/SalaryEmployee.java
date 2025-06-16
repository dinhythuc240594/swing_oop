package com.project01;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class SalaryEmployee {
    protected static final Map<String, Double> POSITION_COEFFICIENTS = new HashMap<>();
    protected static final double BASE_SALARY = 1000.0; // Base salary in currency units
    
    static {
        // Initialize position coefficients
        POSITION_COEFFICIENTS.put("Developer", 1.5);
        POSITION_COEFFICIENTS.put("Designer", 1.4);
        POSITION_COEFFICIENTS.put("QA", 1.3);
        POSITION_COEFFICIENTS.put("QC", 1.3);
        POSITION_COEFFICIENTS.put("Manager", 2.0);
    }
    
    protected int employeeId;
    protected String position;
    protected double baseSalary;
    protected double coefficient;
    protected Connection connection;
    
    public SalaryEmployee(int employeeId, String position, Connection connection) {
        this.employeeId = employeeId;
        this.position = position;
        this.connection = connection;
        this.coefficient = POSITION_COEFFICIENTS.getOrDefault(position, 1.0);
        this.baseSalary = BASE_SALARY;
    }
    
    // Abstract method to calculate total salary
    public abstract double calculateTotalSalary();
    
    // Method to update salary information in database
    public boolean updateSalaryInfo(double newBaseSalary, double newCoefficient) {
        SessionManager sessionManager = SessionManager.getInstance();
        if (!sessionManager.isAdmin() && !sessionManager.isManager()) {
            return false;
        }
        
        try {
            String query = "UPDATE employees SET salary = ?, salary_coefficient = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setDouble(1, newBaseSalary);
            pstmt.setDouble(2, newCoefficient);
            pstmt.setInt(3, employeeId);
            
            int result = pstmt.executeUpdate();
            pstmt.close();
            
            if (result > 0) {
                this.baseSalary = newBaseSalary;
                this.coefficient = newCoefficient;
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Method to load salary information from database
    public void loadSalaryInfo() {
        try {
            String query = "SELECT salary, salary_coefficient FROM employees WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double dbSalary = rs.getDouble("salary");
                double dbCoefficient = rs.getDouble("salary_coefficient");
                
                // If salary info exists in database, use it
                if (dbSalary > 0) {
                    this.baseSalary = dbSalary;
                }
                if (dbCoefficient > 0) {
                    this.coefficient = dbCoefficient;
                }
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Getters and setters
    public double getBaseSalary() {
        return baseSalary;
    }
    
    public double getCoefficient() {
        return coefficient;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
        this.coefficient = POSITION_COEFFICIENTS.getOrDefault(position, 1.0);
    }
} 