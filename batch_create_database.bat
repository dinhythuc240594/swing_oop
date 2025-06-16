@echo off
echo Creating Employee Management Database...

REM Check if SQLite is installed
where sqlite3 >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo SQLite3 is not installed or not in PATH
    echo.
    echo Current PATH:
    echo %PATH%
    echo.
    echo Please install SQLite3 and try again
    pause
    exit /b 1
)


REM Create the database
sqlite3 employee_management.db < employee_management.sql


if %ERRORLEVEL% EQU 0 (
    echo Database created successfully!
    echo Database file: employee_management.db
) else (
    echo Error creating database
)


pause
