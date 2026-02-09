package parkinglotsystem.admin;

import java.util.ArrayList;
import java.util.List;
import parkinglotsystem.core.FineType;
import parkinglotsystem.core.Floor;
import parkinglotsystem.core.ParkingLot;
import parkinglotsystem.core.ParkingSpot;
import parkinglotsystem.core.PaymentService; // Import PaymentService
import parkinglotsystem.core.Vehicle;       // Import FineType

// Service class for providing admin-related data and operations
public class AdminService {

    // Data class used to store parking spot information for UI display
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

    // References to main system components
    private final ParkingLot parkingLot;
    private final PaymentService paymentService; // <--- NEW: Access to money logic

    // Constructor to initialize AdminService
    public AdminService(ParkingLot parkingLot, PaymentService paymentService) {

        if (parkingLot == null) {
            throw new IllegalArgumentException("parkingLot cannot be null");
        }
        if (paymentService == null) {
            throw new IllegalArgumentException("paymentService cannot be null");
        }

        this.parkingLot = parkingLot;
        this.paymentService = paymentService;
    }

    // Get overall parking lot summary information
    public String getSummary() {
        return parkingLot.getStatusSummary();
    }

    // Get overall occupancy rate of the parking lot
    public double getOverallOccupancyRate() {
        return parkingLot.getOverallOccupancyRate();
    }

    // Get formatted occupancy information for each floor
    public List<String> getFloorOccupancyLines() {
        List<String> result = new ArrayList<>();
        for (Floor floor : parkingLot.getFloors()) {
            String line = "Floor " + floor.getFloorNumber()
                    + ": " + String.format("%.1f", floor.getOccupancyRate() * 100) + "%";
            result.add(line);
        }
        return result;
    }

    // Get detailed information for all parking spots
    public List<SpotInfo> getAllSpotInfos() {
        List<SpotInfo> list = new ArrayList<>();
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getSpots()) {
                Vehicle v = spot.getCurrentVehicle();
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

    // --- NEW METHODS FOR REVENUE & FINES ---

    /**
     * Get total revenue formatted as a string (e.g., "RM 150.00").
     */
    public String getTotalRevenueString() {
        return String.format("RM %.2f", paymentService.getTotalRevenue());
    }

    /**
     * Change the fine calculation scheme (Fixed / Hourly / Progressive).
     */
    public void setFineScheme(FineType type) {
        paymentService.setFineScheme(type);
    }
    
    /**
     * Get the currently active fine scheme.
     */
    public FineType getCurrentFineScheme() {
        return paymentService.getCurrentFineScheme();
    }
}