package com.project01;

//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Map;

//public abstract class SalaryEmployee {
//    protected static final Map<String, Double> POSITION_COEFFICIENTS = new HashMap<>();
//    protected static final Map<String, Double> EXPERIENCE_MULTIPLIERS = new HashMap<>();
//    protected static final double BASE_SALARY = 1000.0; // Base salary in currency units
//    protected static final double MANAGER_LEVEL_MULTIPLIER = 2.0; // Hệ số cho level manager

//    static {
//        // Initialize position coefficients
//        POSITION_COEFFICIENTS.put("Developer", 1.5);
//        POSITION_COEFFICIENTS.put("Designer", 1.4);
//        POSITION_COEFFICIENTS.put("QA", 1.3);
//        POSITION_COEFFICIENTS.put("QC", 1.3);
//        POSITION_COEFFICIENTS.put("Manager", 3.5);
//
//        // Initialize experience multipliers - chỉ áp dụng cho level employee
//        EXPERIENCE_MULTIPLIERS.put("Junior", 1.0);    // 0-2 years
//        EXPERIENCE_MULTIPLIERS.put("Mid-level", 1.2); // 2-5 years
//        EXPERIENCE_MULTIPLIERS.put("Senior", 1.5);    // 5-8 years
//        EXPERIENCE_MULTIPLIERS.put("Lead", 1.8);      // 8+ years
//    }

//    protected int employeeId;
//    protected String position;
//    protected String level; // manager hoặc employee từ users.role
//    protected double baseSalary;
//    protected double coefficient;
//    protected int experienceYears;
//    protected String experienceLevel;
//    protected Connection connection;
//
//    public SalaryEmployee(int employeeId, String position, Connection connection) {
//        this.employeeId = employeeId;
//        this.position = position;
//        this.connection = connection;
//        this.baseSalary = BASE_SALARY;
//        this.experienceYears = 0;
//        this.experienceLevel = "Junior";
//        this.level = "employee"; // default
//
//        // Set coefficient based on position
//        this.coefficient = POSITION_COEFFICIENTS.getOrDefault(position, 1.0);
//
//        // Load info from database
//        loadSalaryInfo();
//    }
//
//    // Abstract method to calculate total salary
//    public abstract double calculateTotalSalary();
//
//    // Method to get experience multiplier based on years of experience
//    protected double getExperienceMultiplier() {
//        // Nếu level là manager thì không áp dụng experience multiplier
//        if ("manager".equals(level)) {
//            return 1.0;
//        }
//
//        if (experienceYears >= 8) {
//            return EXPERIENCE_MULTIPLIERS.get("Lead");
//        } else if (experienceYears >= 5) {
//            return EXPERIENCE_MULTIPLIERS.get("Senior");
//        } else if (experienceYears >= 2) {
//            return EXPERIENCE_MULTIPLIERS.get("Mid-level");
//        } else {
//            return EXPERIENCE_MULTIPLIERS.get("Junior");
//        }
//    }
//
//    // Method to get level multiplier
//    protected double getLevelMultiplier() {
//        return "manager".equals(level) ? MANAGER_LEVEL_MULTIPLIER : 1.0;
//    }

    // Method to determine experience level based on years
//    protected String determineExperienceLevel() {
//        // Nếu level là manager thì không hiển thị experience level
//        if ("manager".equals(level)) {
//            return "Manager Level";
//        }
//
//        if (experienceYears >= 8) {
//            return "Lead";
//        } else if (experienceYears >= 5) {
//            return "Senior";
//        } else if (experienceYears >= 2) {
//            return "Mid-level";
//        } else {
//            return "Junior";
//        }
//    }
//
//    // Method to load salary information from database
//    public void loadSalaryInfo() {
//        try {
//            // Query để lấy thông tin salary và level từ users.role
//            String query = "SELECT e.salary, e.hire_date, u.role " +
//                          "FROM employees e " +
//                          "INNER JOIN users u ON e.user_id = u.id " +
//                          "WHERE e.id = ?";
//            PreparedStatement pstmt = connection.prepareStatement(query);
//            pstmt.setInt(1, employeeId);
//            ResultSet rs = pstmt.executeQuery();
//
//            if (rs.next()) {
//                double dbSalary = rs.getDouble("salary");
//                String userRole = rs.getString("role");
//                String hireDateString = rs.getString("hire_date");
//
//                // Load salary from database if exists
//                if (dbSalary > 0) {
//                    this.baseSalary = dbSalary;
//                }
//
//                // Set level from user role
//                this.level = userRole != null ? userRole.toLowerCase() : "employee";
//
//                // Calculate experience years from hire_date (chỉ nếu level không phải manager)
//                if (!"manager".equals(this.level) && hireDateString != null && !hireDateString.trim().isEmpty()) {
//                    this.experienceYears = calculateExperienceYears(hireDateString);
//                }
//
//                //this.experienceLevel = determineExperienceLevel();
//
//                // Coefficient luôn dựa trên position
//                this.coefficient = POSITION_COEFFICIENTS.getOrDefault(position, 1.0);
//            }
//
//            rs.close();
//            pstmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            // Fallback values
//            this.level = "employee";
//            this.coefficient = POSITION_COEFFICIENTS.getOrDefault(position, 1.0);
//        }
//    }
//
//    // Helper method to calculate experience years from hire date
//    private int calculateExperienceYears(String hireDateString) {
//        try {
//            // Parse date string với format yyyy-MM-dd
//            String[] parts = hireDateString.split("-");
//            if (parts.length >= 1) {
//                int hireYear = Integer.parseInt(parts[0]);
//                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
//                return Math.max(0, currentYear - hireYear);
//            }
//        } catch (Exception e) {
//            System.err.println("Error parsing hire date: " + hireDateString);
//        }
//        return 0;
//    }
//
//    // Method to update salary information in database
//    public boolean updateSalaryInfo(double newBaseSalary) {
//        SessionManager sessionManager = SessionManager.getInstance();
//        if (!sessionManager.isAdmin() && !sessionManager.isManager()) {
//            return false;
//        }
//
//        try {
//            String query = "UPDATE employees SET salary = ? WHERE id = ?";
//            PreparedStatement pstmt = connection.prepareStatement(query);
//            pstmt.setDouble(1, newBaseSalary);
//            pstmt.setInt(2, employeeId);
//
//            int result = pstmt.executeUpdate();
//            pstmt.close();
//
//            if (result > 0) {
//                this.baseSalary = newBaseSalary;
//                return true;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    // Getters and setters
//    public double getBaseSalary() {
//        return baseSalary;
//    }
//
//    public double getCoefficient() {
//        return coefficient;
//    }
//
//    public String getPosition() {
//        return position;
//    }
//
//    public String getLevel() {
//        return level;
//    }
//
//    public int getExperienceYears() {
//        return experienceYears;
//    }
//
//    public String getExperienceLevel() {
//        return experienceLevel;
//    }
//
//    public void setPosition(String position) {
//        this.position = position;
//        this.coefficient = POSITION_COEFFICIENTS.getOrDefault(position, 1.0);
//    }
//
//    public void setExperienceYears(int experienceYears) {
//        this.experienceYears = experienceYears;
//       // this.experienceLevel = determineExperienceLevel();
//    }

//    // Static methods for external access
//    public static Map<String, Double> getPositionCoefficients() {
//        return new HashMap<>(POSITION_COEFFICIENTS);
//    }
//
//    public static double getCoefficientForPosition(String position) {
//        return POSITION_COEFFICIENTS.getOrDefault(position, 1.0);
//    }
//
//    public static double getManagerLevelMultiplier() {
//        return MANAGER_LEVEL_MULTIPLIER;
//    }
//}