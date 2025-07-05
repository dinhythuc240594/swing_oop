package com.project01;


import java.util.Locale;

import java.util.ResourceBundle;

public class SalaryInfo {
    // Basic information
    public int employeeId;
    public String fullName;
    public String position;           // position: Developer, Designer, QA, QC, Manager
    public String level;              // level: manager hoặc employee
    public double basicSalary;
    
    // Salary calculation coefficients
    public double positionCoefficient;
    public double levelCoefficient;
    public int yearsOfExperience;
    public String experienceLevel;
    public double experienceCoefficient;
    public double additionalBonus;    // Bonus 500 for Manager position
    
    // Final result
    public double finalSalary;
    
	private ResourceBundle messages;
	private static final String DEFAULT_LANGUAGE = "English";
	private String currentLanguage = DEFAULT_LANGUAGE;
    
	public void setLanguage(Locale locale) {
		this.messages = ResourceBundle.getBundle("messages", locale);
	}
	
	//Function to display salary details
    public String viewDetails() {
        String details = "";
        details += "=== " + messages.getString("salary.title") +"===\n";
        details += messages.getString("salary.employee") + ": " + fullName + "\n";
        details += messages.getString("salary.position") + ": " + position + "\n";

        details += messages.getString("salary.basic") + ": " + String.format("%.0f", basicSalary) + "\n";
        details += messages.getString("salary.coefficient") + ": " + String.format("%.1f", positionCoefficient) + "\n";

        if (level.equals("manager")) {  //level
            details += messages.getString("salary.level") + ": " + level.toUpperCase() + "\n";
            details += messages.getString("salary.manage.level") + ": " + String.format("%.1f", levelCoefficient) + "\n";
        }else if (position.equals("Manager")) {
            details += messages.getString("salary.experience") + ": " + yearsOfExperience + " " + messages.getString("salary.year") + "\n";
        }
        else {
            details += messages.getString("salary.experience") + ": " + yearsOfExperience + " " + messages.getString("salary.year") + " (" + experienceLevel +
                      " - x" + String.format("%.1f", experienceCoefficient) + ")\n";
            details += messages.getString("salary.level") + ": " + level.toUpperCase() + "\n";
            details += messages.getString("salary.coefficient.employee") + ": " + String.format("%.1f", levelCoefficient) + "\n";
        }

        // Display bonus if position is Manager
        if (additionalBonus > 0) {
            details += messages.getString("salary.manage.benefit") + ": " + String.format("%.0f", additionalBonus) + "\n";
        }

        details += messages.getString("salary.sum.salary") + ": " + String.format("%.0f", finalSalary) + "\n";
        return details;
    }
    
    public String viewSummary() {
        return fullName + " (" + position + "): " + String.format("%.0f", finalSalary);
    }
	
	
} 