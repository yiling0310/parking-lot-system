@echo off
REM =====================================================
REM Parking Lot System: Compile & Launch with SQLite Driver
REM =====================================================

cd /d "%~dp0src"


echo Compiling project files...
javac -cp ".;sqlite-jdbc-3.51.1.0.jar" parkinglotsystem\*.java parkinglotsystem\core\*.java parkinglotsystem\admin\*.java parkinglotsystem\ui\*.java
if errorlevel 1 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)

echo Launching Parking System GUI...
echo Using Driver: sqlite-jdbc-3.51.1.0.jar

java -cp ".;sqlite-jdbc-3.51.1.0.jar" parkinglotsystem.MainFrame

if errorlevel 1 (
    echo [ERROR] Run failed. Check if the JAR file is exactly named: sqlite-jdbc-3.51.1.0.jar
)

pause