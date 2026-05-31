# Parking Lot Management System

A Java Swing desktop application for university parking lot management.  
Developed for CCP6224 – Object-Oriented Analysis and Design.

---

## Features

- **Vehicle Entry** — Register vehicle, select available spot, generate ticket
- **Vehicle Exit** — Calculate fee, detect fines, process cash/card payment
- **Fine Management** — Fixed / Progressive / Hourly fine schemes (admin-selectable)
- **Admin Panel** — View occupancy, revenue, parked vehicles, outstanding fines
- **Reports** — Occupancy, revenue, and fine summaries

## Tech Stack

- Java Swing
- SQLite (`sqlite-jdbc-3.51.1.0.jar`)

## How to Run

**Windows:**
```
Run.bat
```

**Manual:**
```bash
cd src
javac -cp sqlite-jdbc-3.51.1.0.jar parkinglotsystem/ui/*.java parkinglotsystem/*.java
java -cp .;sqlite-jdbc-3.51.1.0.jar parkinglotsystem.MainFrame
```
