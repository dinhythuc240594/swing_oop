@echo off
echo Creating Employee Management Database in SQL Server...

@REM REM Check if sqlcmd is installed
@REM where sqlcmd >nul 2>nul
@REM if %ERRORLEVEL% NEQ 0 (
@REM     echo SQL Server Command Line Utilities (sqlcmd) is not installed or not in PATH
@REM     echo.
@REM     echo Current PATH:
@REM     echo "%PATH%"
@REM     echo.
@REM     echo Please install SQL Server Command Line Utilities and try again
@REM     echo You can download it from: https://docs.microsoft.com/en-us/sql/tools/sqlcmd-utility
@REM     pause
@REM     exit /b 1
@REM )

REM Set SQL Server connection parameters
REM Modify these values according to your SQL Server setup
set SERVER_NAME=localhost
set DATABASE_NAME=EmployeeManagement
set USERNAME=sa
set PASSWORD=123456

echo.
echo SQL Server Connection Details:
echo Server: %SERVER_NAME%
echo Database: %DATABASE_NAME%
echo Username: %USERNAME%
echo.
echo Note: Please modify the connection parameters in this batch file if needed
echo.

REM Test connection to SQL Server
echo Testing connection to SQL Server...
sqlcmd -S %SERVER_NAME% -U %USERNAME% -P %PASSWORD% -Q "SELECT @@VERSION" -b
if %ERRORLEVEL% NEQ 0 (
    echo Failed to connect to SQL Server
    echo Please check your connection parameters and ensure SQL Server is running
    pause
    exit /b 1
)

echo Connection successful!

REM Create the database using the SQL Server script
echo Creating database and tables...
sqlcmd -S %SERVER_NAME% -U %USERNAME% -P %PASSWORD% -i employee_management_mssql.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Database created successfully!
    echo Database name: %DATABASE_NAME%
    echo.
    echo You can now connect to the database using:
    echo Server: %SERVER_NAME%
    echo Database: %DATABASE_NAME%
    echo Username: %USERNAME%
) else (
    echo.
    echo Error creating database
    echo Please check the SQL script and try again
)

echo.
pause
