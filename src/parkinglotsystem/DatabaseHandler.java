package parkinglotsystem;

import java.sql.*;
import parkinglotsystem.core.*; // Import ALL core classes (Vehicle, Ticket, etc.)

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

    public static double getTotalUnpaidFines() {
        String sql = "SELECT SUM(amount) FROM parking_fines WHERE status = 'Unpaid'";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("Error calculating fines: " + e.getMessage());
        }
        return 0.0;
    }

    public static void saveSpot(ParkingSpot spot) {
        String sql = "INSERT OR IGNORE INTO parking_spots (spot_id, floor, row, spot_type, status, hours_rate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, spot.getSpotId());
            pstmt.setInt(2, spot.getFloorNumber());
            pstmt.setInt(3, spot.getRowNumber());
            pstmt.setString(4, spot.getSpotType().name());
            pstmt.setString(5, spot.isAvailable() ? "Available" : "Occupied");
            pstmt.setDouble(6, spot.getHourlyRate());
            pstmt.executeUpdate();
            System.out.println("Saved spot " + spot.getSpotId() + " to DB.");
        } catch (SQLException e) {
            System.out.println("Error saving spot: " + e.getMessage());
        }
    }

    public static void loadSpotsFromDB(ParkingLot lot) {
        String sql = "SELECT * FROM parking_spots";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("spot_id");
                int floorNum = rs.getInt("floor");
                String typeStr = rs.getString("spot_type");
                
                Floor floor = lot.getFloor(floorNum);
                if (floor == null) {
                    floor = new Floor(floorNum);
                    lot.addFloor(floor);
                }

                boolean exists = false;
                for (ParkingSpot s : floor.getSpots()) {
                    if (s.getSpotId().equals(id)) {
                        exists = true;
                        break;
                    }
                }
                
                if (!exists) {
                    SpotType type = SpotType.valueOf(typeStr);
                    ParkingSpot s = new ParkingSpot(id, floorNum, 1, type);
                    floor.addSpot(s);
                }
            }
            System.out.println("Database spots loaded into RAM.");
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Error loading spots: " + e.getMessage());
        }
    }

    public static void saveTicket(Ticket ticket) {
        String sql = "INSERT INTO parking_tickets (ticket_id, plate_number, spot_id, entry_time, status) VALUES (?, ?, ?, ?, 'Active')";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            saveVehicle(ticket.getVehicle());

            pstmt.setString(1, ticket.getTicketId());
            pstmt.setString(2, ticket.getVehicle().getLicensePlate());
            pstmt.setString(3, ticket.getSpotId());
            pstmt.setTimestamp(4, Timestamp.valueOf(ticket.getEntryTime()));
            
            pstmt.executeUpdate();
            
            updateSpotStatus(ticket.getSpotId(), "Occupied");
            
            System.out.println("Ticket saved: " + ticket.getTicketId());
        } catch (SQLException e) {
            System.out.println("Error saving ticket: " + e.getMessage());
        }
    }

    private static void saveVehicle(Vehicle v) {
        String sql = "INSERT OR IGNORE INTO vehicles (plate_number, vehicle_type) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, v.getLicensePlate());
            pstmt.setString(2, v.getVehicleType().name()); 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving vehicle: " + e.getMessage());
        }
    }

    public static void updateSpotStatus(String spotId, String status) {
        String sql = "UPDATE parking_spots SET status = ? WHERE spot_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, spotId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating spot status: " + e.getMessage());
        }
    }

    // --- 3. LOAD Active Tickets (Fixed Constructors) ---
    public static void loadActiveTickets(ParkingLot lot) {
        String sql = "SELECT t.ticket_id, t.plate_number, t.spot_id, t.entry_time, " +
                     "v.vehicle_type, v.has_handicapped_card " + 
                     "FROM parking_tickets t " +
                     "JOIN vehicles v ON t.plate_number = v.plate_number " +
                     "WHERE t.status = 'Active'";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String spotId = rs.getString("spot_id");
                String plate = rs.getString("plate_number");
                String typeStr = rs.getString("vehicle_type");
                Timestamp entryTime = rs.getTimestamp("entry_time");
                
                boolean isHandicapped = rs.getInt("has_handicapped_card") == 1;

                Vehicle vehicle = null;
                try {
                    VehicleType vType = VehicleType.valueOf(typeStr);
                    switch (vType) {
                        case CAR -> vehicle = new Car(plate, isHandicapped);
                        case MOTORCYCLE -> vehicle = new Motorcycle(plate, isHandicapped);
                        case SUV_TRUCK -> vehicle = new Suv(plate, isHandicapped);
                        case HANDICAPPED -> vehicle = new HandicappedVehicle(plate, isHandicapped);
                        default -> vehicle = new Car(plate, isHandicapped);
                    }
                } catch (Exception e) {
                    vehicle = new Car(plate, isHandicapped);
                }
                
                if (vehicle != null && entryTime != null) {
                    vehicle.setEntryTime(entryTime.toLocalDateTime());
                }

                ParkingSpot spot = null;
                for (Floor f : lot.getFloors()) {
                    for (ParkingSpot s : f.getSpots()) {
                        if (s.getSpotId().equals(spotId)) {
                            spot = s;
                            break;
                        }
                    }
                }

                if (spot != null && vehicle != null) {
                    spot.occupy(vehicle); 
                    lot.forceRestoreVehicle(vehicle, spot); 
                }
            }
            System.out.println("Active tickets loaded. System state restored.");
        } catch (Exception e) {
            System.out.println("Error loading active tickets: " + e.getMessage());
        }
    }

    // ==========================================================
    //  FEATURE 3: NEW METHODS FOR EXIT AND BILLING
    // ==========================================================

    // 1. Helper: Get Active Ticket ID by Plate (For Billing)
    public static String getActiveTicketId(String plate) {
        String sql = "SELECT ticket_id FROM parking_tickets WHERE plate_number = ? AND status = 'Active'";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plate);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("ticket_id");
        } catch (SQLException e) {
            System.out.println("Error fetching ticket ID: " + e.getMessage());
        }
        return null;
    }

    // 2. Helper: Get total unpaid fines from PREVIOUS visits
    public static double getPreviousUnpaidFines(String plateNumber) {
        String sql = "SELECT SUM(amount) FROM parking_fines WHERE plate_number = ? AND status = 'Unpaid'";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plateNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching fines: " + e.getMessage());
        }
        return 0.0;
    }

    // 3. Process Exit: Update Ticket, Spot, and Clear Fines if paid
    public static void processExit(String ticketId, String spotId, double fee, double paidFines) {
        String updateTicket = "UPDATE parking_tickets SET exit_time = CURRENT_TIMESTAMP, parking_fee = ?, status = 'Paid' WHERE ticket_id = ?";
        String updateSpot   = "UPDATE parking_spots SET status = 'Available' WHERE spot_id = ?";
        String clearFines   = "UPDATE parking_fines SET status = 'Paid' WHERE plate_number = (SELECT plate_number FROM parking_tickets WHERE ticket_id = ?) AND status = 'Unpaid'";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Start Transaction

            try (PreparedStatement psTicket = conn.prepareStatement(updateTicket);
                 PreparedStatement psSpot   = conn.prepareStatement(updateSpot);
                 PreparedStatement psFines  = conn.prepareStatement(clearFines)) {

                // A. Mark Ticket as Paid
                psTicket.setDouble(1, fee);
                psTicket.setString(2, ticketId);
                psTicket.executeUpdate();

                // B. Free up the Spot
                psSpot.setString(1, spotId);
                psSpot.executeUpdate();

                // C. Mark old fines as Paid (if any were collected)
                if (paidFines > 0) {
                    psFines.setString(1, ticketId); // Finds plate via subquery
                    psFines.executeUpdate();
                }

                conn.commit(); // Commit Transaction
                System.out.println("Exit processed successfully for Ticket: " + ticketId);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Exit Transaction Failed: " + e.getMessage());
        }
    }
    // ... inside DatabaseHandler.java ...

    // 4. ADMIN: Get Total Revenue from Paid Tickets + Paid Fines
    public static double getTotalRevenue() {
        String sqlTickets = "SELECT SUM(parking_fee) FROM parking_tickets WHERE status = 'Paid'";
        String sqlFines = "SELECT SUM(amount) FROM parking_fines WHERE status = 'Paid'";
        
        double total = 0.0;
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            // Sum Parking Fees
            ResultSet rs1 = stmt.executeQuery(sqlTickets);
            if (rs1.next()) total += rs1.getDouble(1);
            
            // Sum Collected Fines
            ResultSet rs2 = stmt.executeQuery(sqlFines);
            if (rs2.next()) total += rs2.getDouble(1);
            
        } catch (SQLException e) {
            System.out.println("Error calculating revenue: " + e.getMessage());
        }
        return total;
    }
}