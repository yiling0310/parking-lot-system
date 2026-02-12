package parkinglotsystem.admin;

import java.util.ArrayList;
import java.util.List;
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
                        spot.isAvailable() ? "Available" : "Occupied",
                        plate,
                        spot.getHourlyRate()
                ));
            }
        }
        return list;
    }

    public void createNewFloor(int floorNumber) {
        if (parkingLot.getFloor(floorNumber) != null) {
            throw new IllegalArgumentException("Floor " + floorNumber + " already exists!");
        }
        parkingLot.addFloor(new Floor(floorNumber));
    }

    public void createNewSpot(int floorNum, String spotId, SpotType type, double rate) {
        Floor floor = parkingLot.getFloor(floorNum);
        if (floor == null) {
            throw new IllegalArgumentException("Floor " + floorNum + " does not exist!");
        }
        ParkingSpot newSpot = new ParkingSpot(spotId, floorNum, 1, type);
        floor.addSpot(newSpot);
        DatabaseHandler.saveSpot(newSpot);
    }
}