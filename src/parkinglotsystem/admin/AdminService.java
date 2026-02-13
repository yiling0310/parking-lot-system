package parkinglotsystem.admin;

import java.util.ArrayList;
import java.util.HashMap; // NEW
import java.util.List;
import java.util.Map;     // NEW
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

    // --- NEW: Data Structure for Report ---
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
    // --------------------------------------

    private final ParkingLot parkingLot;
    private final PaymentService paymentService;

    public AdminService(ParkingLot parkingLot, PaymentService paymentService) {
        if (parkingLot == null) throw new IllegalArgumentException("parkingLot cannot be null");
        if (paymentService == null) throw new IllegalArgumentException("paymentService cannot be null");
        this.parkingLot = parkingLot;
        this.paymentService = paymentService;
    }

    // --- REVENUE & FINE METHODS (Required by your UI) ---
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
    // --------------------------------------------------

    // --- NEW: Logic for Reporting (Occupancy by Type) ---
    public Map<String, TypeStat> getOccupancyBySpotType() {
        Map<String, int[]> counts = new HashMap<>();

        // 1. Initialize counters for all known types
        for (SpotType type : SpotType.values()) {
            counts.put(type.name(), new int[]{0, 0}); // {Total, Occupied}
        }

        // 2. Scan every spot in the lot
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getSpots()) {
                String tName = spot.getSpotType().name();
                
                // Increment Total
                counts.get(tName)[0]++;
                
                // Increment Occupied if not available
                if (!spot.isAvailable()) {
                    counts.get(tName)[1]++;
                }
            }
        }

        // 3. Convert to Result Map
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
    // ----------------------------------------------------

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
