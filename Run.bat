@echo off
SETLOCAL
REM =====================================================
REM Parking Lot System: Compile & Launch with SQLite Driver
REM =====================================================

REM Move to the src directory relative to the batch file
cd /d "%~dp0src"

REM Define the driver name for easy updates
set "SQLITE_JAR=sqlite-jdbc-3.51.1.0.jar"

echo [1/3] Verifying environment...
if not exist "%SQLITE_JAR%" (
    echo [ERROR] %SQLITE_JAR% not found in src folder.
    pause
    exit /b 1
)

echo [2/3] Cleaning old build and compiling project files...

REM PowerShell command to delete all .class files recursively
powershell -Command "Get-ChildItem -Path . -Include *.class -Recurse | Remove-Item -Force"

javac -cp ".;sqlite-jdbc-3.51.1.0.jar" parkinglotsystem\*.java parkinglotsystem\core\*.java parkinglotsystem\admin\*.java parkinglotsystem\ui\*.java

if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed. Please check your syntax in the Java files.
    pause
    exit /b 1
)

echo [3/3] Launching Parking System GUI...
echo Driver: %SQLITE_JAR%
echo.

java -cp ".;sqlite-jdbc-3.51.1.0.jar" parkinglotsystem.MainFrame

if errorlevel 1 (
    echo.
    echo [ERROR] Application crashed or failed to start.
)

pause
ENDLOCAL