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
                + " fine_scheme text DEFAULT 'FIXED_PENALTY',\n"
                + " has_reservation integer DEFAULT 0,\n"
                + " parking_fee real,\n"
                + " payment_method text,\n"
                + " amount_paid real,\n"
                + " remaining_balance real,\n"
                + " status text CHECK(status IN ('Active', 'Paid')),\n"
                + " FOREIGN KEY (plate_number) REFERENCES vehicles(plate_number),\n"
                + " FOREIGN KEY (spot_id) REFERENCES parking_spots(spot_id)\n"
                + ");";

        String sqlFines = "CREATE TABLE IF NOT EXISTS parking_fines (\n"
                + " fine_id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " plate_number text NOT NULL,\n"
                + " amount real NOT NULL,\n"
                + " issued_date datetime DEFAULT CURRENT_TIMESTAMP,\n"
                + " status text DEFAULT 'Unpaid' CHECK(status IN ('Unpaid', 'Paid')),\n"
                + " FOREIGN KEY (plate_number) REFERENCES vehicles(plate_number)\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlVehicles);
            stmt.execute(sqlSpots);
            stmt.execute(sqlTickets);
            stmt.execute(sqlFines); 
            ensureSchemaEvolution(conn);
        } catch (SQLException e) {
            System.out.println("Table Creation Error: " + e.getMessage());
        }
    }

    private static void ensureSchemaEvolution(Connection conn) {
        addColumnIfMissing(conn, "parking_tickets", "fine_scheme", "text DEFAULT 'FIXED_PENALTY'");
        addColumnIfMissing(conn, "parking_tickets", "has_reservation", "integer DEFAULT 0");
        addColumnIfMissing(conn, "parking_tickets", "payment_method", "text");
        addColumnIfMissing(conn, "parking_tickets", "amount_paid", "real");
        addColumnIfMissing(conn, "parking_tickets", "remaining_balance", "real");
    }

    private static void addColumnIfMissing(Connection conn, String table, String column, String definition) {
        String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException ignored) {
            // Column already exists for existing databases.
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
                    int rowNum = rs.getInt("row");
                    ParkingSpot s = new ParkingSpot(id, floorNum, rowNum, type);
                    Row row = floor.getRow(rowNum);
                    if (row == null) {
                        row = new Row(rowNum);
                        floor.addRow(row);
                    }
                    row.addSpot(s);
                }
            }
            System.out.println("Database spots loaded into RAM.");
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Error loading spots: " + e.getMessage());
        }
    }

    public static void saveTicket(Ticket ticket, FineType fineScheme, boolean hasReservation) {
        String sql = "INSERT INTO parking_tickets (ticket_id, plate_number, spot_id, entry_time, fine_scheme, has_reservation, status) VALUES (?, ?, ?, ?, ?, ?, 'Active')";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            saveVehicle(ticket.getVehicle());

            pstmt.setString(1, ticket.getTicketId());
            pstmt.setString(2, ticket.getVehicle().getLicensePlate());
            pstmt.setString(3, ticket.getSpotId());
            pstmt.setTimestamp(4, Timestamp.valueOf(ticket.getEntryTime()));
            pstmt.setString(5, fineScheme == null ? FineType.FIXED_PENALTY.name() : fineScheme.name());
            pstmt.setInt(6, hasReservation ? 1 : 0);
            
            pstmt.executeUpdate();
            
            updateSpotStatus(ticket.getSpotId(), "Occupied");
            
            System.out.println("Ticket saved: " + ticket.getTicketId());
        } catch (SQLException e) {
            System.out.println("Error saving ticket: " + e.getMessage());
        }
    }

    private static void saveVehicle(Vehicle v) {
        String sql = "INSERT OR IGNORE INTO vehicles (plate_number, vehicle_type, has_handicapped_card) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, v.getLicensePlate());
            pstmt.setString(2, v.getVehicleType().name()); 
            pstmt.setInt(3, v.isHandicappedCardHolder() ? 1 : 0);
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

    public static FineType getFineSchemeByTicketId(String ticketId) {
        String sql = "SELECT fine_scheme FROM parking_tickets WHERE ticket_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticketId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String scheme = rs.getString("fine_scheme");
                if (scheme == null || scheme.isBlank()) {
                    return FineType.FIXED_PENALTY;
                }
                return FineType.valueOf(scheme);
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Error fetching fine scheme: " + e.getMessage());
        }
        return FineType.FIXED_PENALTY;
    }

    public static boolean hasReservationByTicketId(String ticketId) {
        String sql = "SELECT has_reservation FROM parking_tickets WHERE ticket_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticketId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("has_reservation") == 1;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching reservation flag: " + e.getMessage());
        }
        return false;
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

    // 3. Process Exit: Update ticket and spot after successful payment.
    public static void processExit(
            String ticketId,
            String spotId,
            double fee,
            String paymentMethod,
            double amountPaid,
            double remainingBalance
    ) {
        String updateTicket = "UPDATE parking_tickets SET exit_time = CURRENT_TIMESTAMP, parking_fee = ?, "
                + "payment_method = ?, amount_paid = ?, remaining_balance = ?, status = 'Paid' WHERE ticket_id = ?";
        String updateSpot   = "UPDATE parking_spots SET status = 'Available' WHERE spot_id = ?";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Start Transaction

            try (PreparedStatement psTicket = conn.prepareStatement(updateTicket);
                 PreparedStatement psSpot   = conn.prepareStatement(updateSpot)) {

                // A. Mark Ticket as Paid
                psTicket.setDouble(1, fee);
                psTicket.setString(2, paymentMethod);
                psTicket.setDouble(3, amountPaid);
                psTicket.setDouble(4, remainingBalance);
                psTicket.setString(5, ticketId);
                psTicket.executeUpdate();

                // B. Free up the Spot
                psSpot.setString(1, spotId);
                psSpot.executeUpdate();

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

    public static void addFineRecord(String plateNumber, double amount, String status) {
        if (plateNumber == null || plateNumber.isBlank() || amount <= 0) {
            return;
        }
        String normalizedStatus = "Paid".equalsIgnoreCase(status) ? "Paid" : "Unpaid";
        String sql = "INSERT INTO parking_fines (plate_number, amount, status) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plateNumber);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, normalizedStatus);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving fine: " + e.getMessage());
        }
    }

    public static double applyFinePayment(String plateNumber, double amountToApply) {
        if (amountToApply <= 0) return 0.0;

        String selectSql = "SELECT fine_id, amount FROM parking_fines WHERE plate_number = ? AND status = 'Unpaid' ORDER BY issued_date, fine_id";
        String markPaidSql = "UPDATE parking_fines SET status = 'Paid' WHERE fine_id = ?";
        String reduceSql = "UPDATE parking_fines SET amount = ? WHERE fine_id = ?";
        double applied = 0.0;

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                 PreparedStatement markPaidStmt = conn.prepareStatement(markPaidSql);
                 PreparedStatement reduceStmt = conn.prepareStatement(reduceSql)) {
                selectStmt.setString(1, plateNumber);
                ResultSet rs = selectStmt.executeQuery();

                double remaining = amountToApply;
                while (rs.next() && remaining > 0.0) {
                    int fineId = rs.getInt("fine_id");
                    double fineAmount = rs.getDouble("amount");
                    if (fineAmount <= remaining) {
                        markPaidStmt.setInt(1, fineId);
                        markPaidStmt.executeUpdate();
                        remaining -= fineAmount;
                        applied += fineAmount;
                    } else {
                        double newAmount = fineAmount - remaining;
                        reduceStmt.setDouble(1, newAmount);
                        reduceStmt.setInt(2, fineId);
                        reduceStmt.executeUpdate();
                        applied += remaining;
                        remaining = 0.0;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error applying fine payment: " + e.getMessage());
        }
        return applied;
    }
}
