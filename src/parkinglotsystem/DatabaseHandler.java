package parkinglotsystem;

import java.sql.*;
import parkinglotsystem.core.ParkingLot;
import parkinglotsystem.core.ParkingSpot;

public class DatabaseHandler {
    private static final String URL = "jdbc:sqlite:parking_system.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
        return conn;
    }

    public static void createNewTable() {
        // 1. VEHICLES TABLE
        String sqlVehicles = "CREATE TABLE IF NOT EXISTS vehicles (\n"
                + " plate_number text PRIMARY KEY,\n"
                + " vehicle_type text NOT NULL,\n"
                + " has_handicapped_card integer DEFAULT 0\n" 
                + ");";

        // 2. PARKING_SPOTS TABLE
        String sqlSpots = "CREATE TABLE IF NOT EXISTS parking_spots (\n"
                + " spot_id text PRIMARY KEY,\n"
                + " floor integer NOT NULL,\n"
                + " row integer NOT NULL,\n"
                + " spot_type text NOT NULL,\n"
                + " status text DEFAULT 'Available' CHECK(status IN ('Available', 'Occupied')),\n"
                + " hours_rate real NOT NULL\n"
                + ");";

        // 3. PARKING_TICKETS TABLE
        //-- Format: T-PLATE-TIMESTAMP
        String sqlTickets = "CREATE TABLE IF NOT EXISTS parking_tickets (\n"
                + " ticket_id text PRIMARY KEY,\n"
                + " plate_number text NOT NULL,\n"
                + " spot_id text NOT NULL,\n"
                + " entry_time datetime DEFAULT CURRENT_TIMESTAMP,\n"
                + " exit_time datetime,\n"
                + " parking_fee real,\n"
                + " status text CHECK(status IN ('Active', 'Paid')),\n"
                + " FOREIGN KEY (plate_number) REFERENCES vehicles(plate_number),\n"
                + " FOREIGN KEY (spot_id) REFERENCES parking_spots(spot_id)\n"
                + ");";

        String sqlFines = "CREATE TABLE IF NOT EXISTS parking_fines (\n"
                + " fine_id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " plate_number text NOT NULL,\n"
                + " amount real NOT NULL,\n"
                + " issued_date datetime DEFAULT CURRENT_TIMESTAMP,\n"
                + " status text CHECK(status IN ('Unpaid', 'Paid')),\n"
                + " FOREIGN KEY (plate_number) REFERENCES vehicles(plate_number)\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlVehicles);
            stmt.execute(sqlSpots);
            stmt.execute(sqlTickets);
            stmt.execute(sqlFines); 
        } catch (SQLException e) {
            System.out.println("Table Creation Error: " + e.getMessage());
        }
    }

    public static void initializeSpots(ParkingLot lot) {
        String sql = "INSERT OR IGNORE INTO parking_spots (spot_id, floor, row, spot_type, status, hours_rate) VALUES (?, ?, ?, ?, 'Available', ?)";
        
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (ParkingSpot spot : lot.getAllSpots()) {
                pstmt.setString(1, spot.getSpotId());
                pstmt.setInt(2, spot.getFloorNumber());
                pstmt.setInt(3, spot.getRowNumber());
                pstmt.setString(4, spot.getSpotType().name());
                pstmt.setDouble(5, spot.getHourlyRate());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Database spots initialized!");
        } catch (SQLException e) {
            System.out.println("Error initializing spots: " + e.getMessage());
        }
    }
}
