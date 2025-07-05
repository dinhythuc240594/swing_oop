package com.project01;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SalaryCalculator {
    
    // Fixed values for salary calculation
    private static final double BASE_SALARY = 1000.0;           // Base salary
    private static final double MANAGER_LEVEL_MULTIPLIER = 2.8;       // Multiplier if level is manager
    private static final double MANAGER_POSITION_BONUS = 500.0; // Bonus for Manager position
    

    //Function to get coefficient by job position
    public static double getPositionCoefficient(String position) {
        if (position.equals("Developer")) {
            return 1.5;
        } else if (position.equals("Designer")) {
            return 1.4;
        } else if (position.equals("QA")) {
            return 1.3;
        } else if (position.equals("QC")) {
            return 1.3;
        } else if (position.equals("Manager")) {
            return 3.5;
        } else {
            return 1.0;
        }
    }
    

    //Function to get experience coefficient
    private static double getExperienceCoefficient(int yearsOfExperience) {
        if (yearsOfExperience >= 8) {
            return 1.8;//Leader
        } else if (yearsOfExperience >= 5) {
            return 1.5;//senior
        } else if (yearsOfExperience >= 2) {
            return 1.2;//middle
        } else {
            return 1.0; //Junior
        }
    }


    //Function to get experience level name
    private static String getExperienceLevelName(int yearsOfExperience) {
        if (yearsOfExperience >= 8) {
            return "Leader";
        } else if (yearsOfExperience >= 5) {
            return "Senior";
        } else if (yearsOfExperience >= 2) {
            return "Middle";
        } else {
            return "Junior";
        }
    }
    

    //Function to calculate years of work from hire date
    private static int calculateYearsOfWork(String hireDate) {
        if (hireDate == null || hireDate.trim().isEmpty()) {
            return 0;
        }
        
        try {
            String[] dateParts = hireDate.split("-");
            if (dateParts.length > 0) {
                int hireYear = Integer.parseInt(dateParts[0]);
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                return currentYear - hireYear;
            }
        } catch (Exception error) {
            System.out.println("Error calculating work years: " + error.getMessage());
        }
        
        return 0;
    }
    


    // Main function to calculate employee salary
    public static SalaryInfo calculateSalary(int employeeId, Connection dbConnection) {
        SalaryInfo salaryInfo = new SalaryInfo();
        
        try {
            String sqlQuery = "SELECT e.first_name, e.last_name, e.position, e.salary, e.hire_date, u.role " +
                               "FROM employees e " +
                               "INNER JOIN users u ON e.user_id = u.id " +
                               "WHERE e.id = ?";
            
            PreparedStatement pstmt = dbConnection.prepareStatement(sqlQuery);
            pstmt.setInt(1, employeeId);
            ResultSet result = pstmt.executeQuery();
            
            if (result.next()) {
                String fullName = result.getString("first_name") + " " + result.getString("last_name");
                String position = result.getString("position");
                String level = result.getString("role");
                double baseSalary = result.getDouble("salary");
                String hireDate = result.getString("hire_date");
                
                int yearsOfExperience = calculateYearsOfWork(hireDate);
                double positionCoefficient = getPositionCoefficient(position);
                double levelCoefficient = level.equals("manager") ? MANAGER_LEVEL_MULTIPLIER : 1.0;
                double experienceCoefficient = level.equals("manager") ? 1.0 : getExperienceCoefficient(yearsOfExperience);
                String experienceLevelName = getExperienceLevelName(yearsOfExperience);
                
                double finalSalary;
                double additionalBonus = 0;
                
                if (level.equals("manager")) {
                    finalSalary = baseSalary * positionCoefficient * levelCoefficient;
                } else {
                    finalSalary = baseSalary * positionCoefficient * experienceCoefficient;
                }
                
                if (position.equals("Manager")) {
                    additionalBonus = MANAGER_POSITION_BONUS;
                    finalSalary += additionalBonus;
                }
                
                salaryInfo.employeeId = employeeId;
                salaryInfo.fullName = fullName;
                salaryInfo.position = position;
                salaryInfo.level = level;
                salaryInfo.basicSalary = baseSalary;
                salaryInfo.positionCoefficient = positionCoefficient;
                salaryInfo.levelCoefficient = levelCoefficient;
                salaryInfo.yearsOfExperience = yearsOfExperience;
                salaryInfo.experienceLevel = experienceLevelName;
                salaryInfo.experienceCoefficient = experienceCoefficient;
                salaryInfo.additionalBonus = additionalBonus;
                salaryInfo.finalSalary = finalSalary;
                
                System.out.println("Calculated salary for " + fullName + ": " + finalSalary);
            }
            
            result.close();
            pstmt.close();
            
        } catch (SQLException error) {
            System.out.println("Error calculating salary: " + error.getMessage());
        }
        
        return salaryInfo;
    }
}