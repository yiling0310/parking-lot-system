package parkinglotsystem.admin;

import java.sql.Connection;
import java.sql.PreparedStatement; 
import java.sql.ResultSet;
import java.util.ArrayList;  
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import parkinglotsystem.DatabaseHandler;
import parkinglotsystem.core.*;

public class AdminService {

    public static class SpotInfo {
        public final int floorNo;
        public final String spotId;
        public final String type;
        public final String status;
        public final String plateOrDash;
        public final double hourlyRate;

        public SpotInfo(int floorNo, String spotId, String type, String status, String plateOrDash, double hourlyRate) {
            this.floorNo = floorNo;
            this.spotId = spotId;
            this.type = type;
            this.status = status;
            this.plateOrDash = plateOrDash;
            this.hourlyRate = hourlyRate;
        }
    }

    //Data Structure for Report
    public static class TypeStat {
        public final String typeName;
        public final int total;
        public final int occupied;

        public TypeStat(String typeName, int total, int occupied) {
            this.typeName = typeName;
            this.total = total;
            this.occupied = occupied;
        }

        public double getRate() {
            return (total == 0) ? 0.0 : (double) occupied / total;
        }
    }

    public static class ParkedVehicleInfo {
        public final String plate;
        public final String vehicleType;
        public final String spotId;
        public final String entryTime;

        public ParkedVehicleInfo(String plate, String vehicleType, String spotId, String entryTime) {
            this.plate = plate;
            this.vehicleType = vehicleType;
            this.spotId = spotId;
            this.entryTime = entryTime;
        }
    }

    public static class FineInfo {
        public final String plate;
        public final double unpaidAmount;

        public FineInfo(String plate, double unpaidAmount) {
            this.plate = plate;
            this.unpaidAmount = unpaidAmount;
        }
    }

    private final ParkingLot parkingLot;
    private final PaymentService paymentService;

    public AdminService(ParkingLot parkingLot, PaymentService paymentService) {
        if (parkingLot == null) throw new IllegalArgumentException("parkingLot cannot be null");
        if (paymentService == null) throw new IllegalArgumentException("paymentService cannot be null");
        this.parkingLot = parkingLot;
        this.paymentService = paymentService;
    }

    //REVENUE & FINE METHODS FOR ADMIN PANEL
    public String getTotalRevenueString() {
        return String.format("RM %.2f", paymentService.getTotalRevenue());
    }

    public void setFineScheme(FineType type) {
        paymentService.setFineScheme(type);
    }
    
    public FineType getCurrentFineScheme() {
        return paymentService.getCurrentFineScheme();
    }
    
    public String getTotalUnpaidFinesString() {
        double total = DatabaseHandler.getTotalUnpaidFines();
        return String.format("RM %.2f", total);
    }

    //Logic for Reporting (Occupancy by Type)
    public Map<String, TypeStat> getOccupancyBySpotType() {
        Map<String, int[]> counts = new HashMap<>();

        //1. Initialize counters for all known types
        for (SpotType type : SpotType.values()) {
            counts.put(type.name(), new int[]{0, 0}); 
        }

        //2. Scan every spot in the lot
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getSpots()) {
                String tName = spot.getSpotType().name();
                
                //Increment Total
                counts.get(tName)[0]++;
                
                //Increment Occupied if not available
                if (!spot.isAvailable()) {
                    counts.get(tName)[1]++;
                }
            }
        }

        //3. Convert to Result Map
        Map<String, TypeStat> results = new HashMap<>();
        for (Map.Entry<String, int[]> entry : counts.entrySet()) {
            results.put(entry.getKey(), new TypeStat(
                entry.getKey(), 
                entry.getValue()[0], 
                entry.getValue()[1]
            ));
        }
        return results;
    }

    public String getSummary() {
        return parkingLot.getStatusSummary();
    }

    public double getOverallOccupancyRate() {
        return parkingLot.getOverallOccupancyRate();
    }

    public List<String> getFloorOccupancyLines() {
        List<String> result = new ArrayList<>();
        for (Floor floor : parkingLot.getFloors()) {
            String line = "Floor " + floor.getFloorNumber()
                    + ": " + String.format("%.1f", floor.getOccupancyRate() * 100) + "%";
            result.add(line);
        }
        return result;
    }

    public List<SpotInfo> getAllSpotInfos() {
        List<SpotInfo> list = new ArrayList<>();
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getSpots()) {
                // Safe check for vehicle
                Vehicle v = spot.getVehicle(); 
                String plate = (v == null) ? "-" : v.getLicensePlate();

                list.add(new SpotInfo(
                        floor.getFloorNumber(),
                        spot.getSpotId(),
                        spot.getSpotType().name(),
                        spot.getStatus().name(),
                        plate,
                        spot.getHourlyRate()
                ));
            }
        }
        return list;
    }

    public List<ParkedVehicleInfo> getCurrentlyParkedVehicles() {
        List<ParkedVehicleInfo> results = new ArrayList<>();
        String sql = "SELECT t.plate_number, v.vehicle_type, t.spot_id, t.entry_time " +
                "FROM parking_tickets t JOIN vehicles v ON t.plate_number = v.plate_number " +
                "WHERE t.status = 'Active' ORDER BY t.entry_time";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                results.add(new ParkedVehicleInfo(
                        rs.getString("plate_number"),
                        rs.getString("vehicle_type"),
                        rs.getString("spot_id"),
                        rs.getString("entry_time")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error loading parked vehicles: " + e.getMessage());
        }
        return results;
    }

    public List<FineInfo> getOutstandingFinesByPlate() {
        List<FineInfo> results = new ArrayList<>();
        String sql = "SELECT plate_number, SUM(amount) AS total_unpaid " +
                "FROM parking_fines WHERE status = 'Unpaid' " +
                "GROUP BY plate_number ORDER BY total_unpaid DESC";
        try (Connection conn = DatabaseHandler.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                results.add(new FineInfo(
                        rs.getString("plate_number"),
                        rs.getDouble("total_unpaid")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error loading unpaid fines: " + e.getMessage());
        }
        return results;
    }

    public void createNewFloor(int floorNumber) {
        if (floorNumber <= 0) {
            throw new IllegalArgumentException("Floor number must be >= 1");
        }
        if (parkingLot.getFloor(floorNumber) != null) {
            throw new IllegalArgumentException("Floor " + floorNumber + " already exists!");
        }
        parkingLot.addFloor(new Floor(floorNumber));
    }

    public void createNewRow(int floorNum, int rowNum) {
        if (rowNum <= 0) {
            throw new IllegalArgumentException("Row number must be >= 1");
        }
        Floor floor = parkingLot.getFloor(floorNum);
        if (floor == null) {
            throw new IllegalArgumentException("Floor " + floorNum + " does not exist!");
        }
        if (floor.getRow(rowNum) != null) {
            throw new IllegalArgumentException("Row " + rowNum + " already exists on floor " + floorNum + "!");
        }
        floor.addRow(new Row(rowNum));
    }

    public void createNewSpot(int floorNum, int rowNum, String spotId, SpotType type, double rate) {
        if (rowNum <= 0) {
            throw new IllegalArgumentException("Row number must be >= 1");
        }
        if (spotId == null || spotId.isBlank()) {
            throw new IllegalArgumentException("Spot ID cannot be empty");
        }
        Floor floor = parkingLot.getFloor(floorNum);
        if (floor == null) {
            throw new IllegalArgumentException("Floor " + floorNum + " does not exist!");
        }
        for (ParkingSpot spot : floor.getSpots()) {
            if (spot.getSpotId().equalsIgnoreCase(spotId.trim())) {
                throw new IllegalArgumentException("Spot ID already exists: " + spotId);
            }
        }
        Row row = floor.getRow(rowNum);
        if (row == null) {
            throw new IllegalArgumentException("Row " + rowNum + " does not exist on floor " + floorNum + "!");
        }
        ParkingSpot newSpot = new ParkingSpot(spotId.trim(), floorNum, rowNum, type);
        row.addSpot(newSpot);
        DatabaseHandler.saveSpot(newSpot);
    }
}
