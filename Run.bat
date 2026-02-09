@echo off
REM =====================================================
REM Parking Lot System: Compile & Run (CLI + GUI)
REM =====================================================

REM Go to the src folder
cd /d "%~dp0src"
if errorlevel 1 (
    echo Failed to change directory to src
    pause
    exit /b 1
)

REM =============================
REM Compile all Java files
REM =============================
echo Compiling all Java files...
javac parkinglotsystem\*.java parkinglotsystem\core\*.java parkinglotsystem\admin\*.java parkinglotsystem\ui\*.java
if errorlevel 1 (
    echo Compilation failed. Check errors above.
    pause
    exit /b 1
)
echo Compilation successful!
echo.

REM =============================
REM Run CLI test (CoreTest)
REM =============================
echo Running CLI test (CoreTest)...
java parkinglotsystem.CoreTest
echo.
echo CLI test finished.
echo.

REM =============================
REM Launch GUI (MainFrame)
REM =============================
echo Launching GUI (MainFrame)...
java parkinglotsystem.MainFrame

pause
