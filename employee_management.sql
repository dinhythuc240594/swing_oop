-- Create the database schema for employee management system

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('administrator', 'manager', 'employee')),
    theme VARCHAR(20) DEFAULT 'Light',
    font_size VARCHAR(20) DEFAULT 'Medium',
    language VARCHAR(20) DEFAULT 'English',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Managers table
CREATE TABLE IF NOT EXISTS managers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    department VARCHAR(50),
    photo BLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);


-- Employees table
CREATE TABLE IF NOT EXISTS employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    position VARCHAR(50),
    department TEXT,
    manager_id INTEGER,
    hire_date DATE,
    photo BLOB,
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
    number_of_children INTEGER DEFAULT 0,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (manager_id) REFERENCES managers(id)
);


--- Task table
CREATE TABLE IF NOT EXISTS tasks (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	title TEXT NOT NULL,
	description TEXT,
	assigned_to INTEGER NOT NULL,
	assigned_by INTEGER NOT NULL,
	status TEXT NOT NULL DEFAULT 'peding',
	priority VARCHAR(20) NOT NULL DEFAULT 'medium',
	due_date TIMESTAMP NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (assigned_to) REFERENCES employees(id),
	FOREIGN KEY (assigned_by) REFERENCES employees(id)
);


-- create index
CREATE INDEX IF NOT EXISTS idx_employees_email ON employees(id);
-- CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX IF NOT EXISTS idx_task_status ON tasks(status);

-- Insert sample data for testing
-- Sample manager user and account manager

-- admin and manager account
INSERT INTO users (username, password, role) VALUES
('admin', 'admin123', 'administrator'),
('thucdinh', '240594', 'manager');

-- Sample manager
INSERT INTO managers (user_id, first_name, last_name, email, phone, department, photo) VALUES
(1, 'Admin', 'Ttest', 'admin.ttest@company.com', '0982109103', 'IT', null),
(2, 'Thuc', 'Dinh', 'thuc.dinh@company.com', '0982109103', 'IT', null);


-- Sample employee and user account

-- user account
INSERT INTO users (username, password, role) VALUES
('anna.nguyen', 'annaNguyen', 'employee'),
('lily.Hoang', 'lilyhoang', 'employee'),
('bob.nguyen', 'bob654321', 'employee'),
('binh.nguyen', 'nguyen321', 'employee'),
('alice.johnson', '222johnson', 'employee'),
('bob.brown', 'brownbob4444', 'employee'),
('charlie.davis', '333davischar', 'employee'),
('diana.evans', '555evanshat', 'employee'),
('ethan.fowler', '08fowler321', 'employee'),
('fiona.garcia', '677garcia', 'employee'),
('george.harris', 'george234', 'employee'),
('hannah.clark', 'clark134', 'employee');

-- employee profile
INSERT INTO employees (user_id, first_name, last_name, email, phone, position, manager_id, hire_date, photo) VALUES
(3, 'An', 'Nguyen', 'anna.nguyen@company.com', '0927111321', 'Designer', 1, '2024-01-01', null),
(4, 'lily', 'Hoang', 'lily.hoang@company.com', '0982344321', 'QA', 1, '2024-01-01', null),
(5, 'Bob', 'Nguyen', 'bob.nguyen@company.com', '0927654321', 'Developer', 1, '2024-01-01', null),
(6, 'Binh', 'Nguyen', 'binh.nguyen@company.com', '0987654321', 'Developer', 1, '2024-01-01', null),
(7, 'Alice', 'Johnson', 'alice.johnson@company.com', '111-222-3333', 'Designer', 1, '2024-02-15', null),
(8, 'Bob', 'Brown', 'bob.brown@company.com', '222-333-4444', 'QA', 1, '2024-03-20', null),
(9, 'Charlie', 'Davis', 'charlie.davis@company.com', '333-444-5555', 'Manager', 1, '2024-04-10', null),
(10, 'Diana', 'Evans', 'diana.evans@company.com', '444-555-6666', 'Developer', 1, '2024-05-05', null),
(11, 'Ethan', 'Fowler', 'ethan.fowler@company.com', '555-666-7777', 'Developer', 1, '2024-06-15', null),
(12, 'Fiona', 'Garcia', 'fiona.garcia@company.com', '666-777-8888', 'Designer', 1, '2024-07-20', null),
(13, 'George', 'Harris', 'george.harris@company.com', '777-888-9999', 'QA', 1, '2024-08-10', null),
(14, 'Hannah', 'Clark', 'hannah.clark@company.com', '888-999-0000', 'QC', 1, '2024-09-05', null);
