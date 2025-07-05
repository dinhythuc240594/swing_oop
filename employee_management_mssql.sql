-- Create the database schema for employee management system (SQL Server)

-- Create database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'EmployeeManagement')
BEGIN
    CREATE DATABASE EmployeeManagement;
END
GO

USE EmployeeManagement;
GO

-- Users table for authentication
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
BEGIN
    CREATE TABLE users (
        id INT IDENTITY(1,1) PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        role VARCHAR(20) NOT NULL CHECK (role IN ('administrator', 'manager', 'employee')),
        theme VARCHAR(20) DEFAULT 'Light',
        font_size VARCHAR(20) DEFAULT 'Medium',
        language VARCHAR(20) DEFAULT 'English',
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
END
GO

-- Managers table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='managers' AND xtype='U')
BEGIN
    CREATE TABLE managers (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        first_name VARCHAR(50) NOT NULL,
        last_name VARCHAR(50) NOT NULL,
        email VARCHAR(100) NOT NULL UNIQUE,
        phone VARCHAR(20),
        department VARCHAR(50),
        photo VARBINARY(MAX),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        FOREIGN KEY (user_id) REFERENCES users(id)
    );
END
GO

-- Employees table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='employees' AND xtype='U')
BEGIN
    CREATE TABLE employees (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        first_name VARCHAR(50) NOT NULL,
        last_name VARCHAR(50) NOT NULL,
        email VARCHAR(100) NOT NULL UNIQUE,
        phone VARCHAR(20),
        position VARCHAR(50),
        department TEXT,
        manager_id INT,
        hire_date DATE,
        photo VARBINARY(MAX),
        identity_number VARCHAR(50) UNIQUE,
        address TEXT,
        city VARCHAR(100),
        state VARCHAR(100),
        country VARCHAR(100),
        postal_code VARCHAR(20),
        salary DECIMAL(12,2) DEFAULT 1000.00,
        salary_coefficient DECIMAL(4,2) DEFAULT 1.00,
        marital_status VARCHAR(20),
        spouse_name VARCHAR(100),
        number_of_children INT DEFAULT 0,
        emergency_contact_name VARCHAR(100),
        emergency_contact_phone VARCHAR(20),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        FOREIGN KEY (user_id) REFERENCES users(id),
        FOREIGN KEY (manager_id) REFERENCES managers(id)
    );
END
GO

-- Task table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tasks' AND xtype='U')
BEGIN
    CREATE TABLE tasks (
        id INT IDENTITY(1,1) PRIMARY KEY,
        title TEXT NOT NULL,
        description TEXT,
        assigned_to INT NOT NULL,
        assigned_by INT NOT NULL,
        status TEXT NOT NULL DEFAULT 'pending',
        priority VARCHAR(20) NOT NULL DEFAULT 'medium',
        due_date DATETIME2 NOT NULL,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        FOREIGN KEY (assigned_to) REFERENCES employees(id),
        FOREIGN KEY (assigned_by) REFERENCES managers(id)
    );
END
GO

-- Province table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='provinces' AND xtype='U')
BEGIN
    CREATE TABLE provinces (
        id INT IDENTITY(1,1) PRIMARY KEY,
        code VARCHAR(10),
        name VARCHAR(100) NOT NULL,
        division_type VARCHAR(50),
        codename VARCHAR(100),
        phone_code VARCHAR(10)
    );
END
GO

-- Create indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_employees_email')
BEGIN
    CREATE INDEX idx_employees_email ON employees(id);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tasks_assigned_to')
BEGIN
    CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_task_status')
BEGIN
    CREATE INDEX idx_task_status ON tasks(status);
END
GO

-- Insert sample data for testing
-- Sample manager user and account manager

-- admin and manager account
IF NOT EXISTS (SELECT * FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, password, role) VALUES
    ('admin', 'admin123', 'administrator');
END

IF NOT EXISTS (SELECT * FROM users WHERE username = 'thucdinh')
BEGIN
    INSERT INTO users (username, password, role) VALUES
    ('thucdinh', '240594', 'manager');
END

-- Sample manager
IF NOT EXISTS (SELECT * FROM managers WHERE email = 'admin.ttest@company.com')
BEGIN
    INSERT INTO managers (user_id, first_name, last_name, email, phone, department, photo) VALUES
    (1, 'Admin', 'Ttest', 'admin.ttest@company.com', '0982109103', 'IT', null);
END

IF NOT EXISTS (SELECT * FROM managers WHERE email = 'thuc.dinh@company.com')
BEGIN
    INSERT INTO managers (user_id, first_name, last_name, email, phone, department, photo) VALUES
    (2, 'Thuc', 'Dinh', 'thuc.dinh@company.com', '0982109103', 'IT', null);
END

PRINT 'Employee Management Database created successfully!';
GO 