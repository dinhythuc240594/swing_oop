package com.project01;

import java.sql.Connection;

public class RegularEmployee extends SalaryEmployee {
    
    public RegularEmployee(int employeeId, String position, Connection connection) {
        super(employeeId, position, connection);
    }
    
    @Override
    public double calculateTotalSalary() {
        // Basic salary calculation: base salary * position coefficient
        return baseSalary * coefficient;
    }
} 