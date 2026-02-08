package parkinglotsystem.admin;

import java.util.ArrayList;
import java.util.List;
import parkinglotsystem.core.Floor;
import parkinglotsystem.core.ParkingLot;
import parkinglotsystem.core.ParkingSpot;
import parkinglotsystem.core.Vehicle;

// Service class for providing admin-related data and operations
public class AdminService {

    // Data class used to store parking spot information for UI display
    public static class SpotInfo {

        // Floor number of the parking spot
        public final int floorNo;

        // Unique ID of the parking spot
        public final String spotId;

        // Type of the parking spot (REGULAR, COMPACT, etc.)
        public final String type;

        // Current status of the spot (AVAILABLE / OCCUPIED)
        public final String status;

        // Vehicle plate number or "-" if empty
        public final String plateOrDash;

        // Hourly parking rate
        public final double hourlyRate;

        // Constructor to initialize spot information
        public SpotInfo(int floorNo, String spotId, String type, String status, String plateOrDash, double hourlyRate) {
            this.floorNo = floorNo;
            this.spotId = spotId;
            this.type = type;
            this.status = status;
            this.plateOrDash = plateOrDash;
            this.hourlyRate = hourlyRate;
        }
    }

    // Reference to the main ParkingLot object
    private final ParkingLot parkingLot;

    // Constructor to initialize AdminService with a ParkingLot instance
    public AdminService(ParkingLot parkingLot) {

        // Ensure parking lot is not null
        if (parkingLot == null)
            throw new IllegalArgumentException("parkingLot cannot be null");

        this.parkingLot = parkingLot;
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

        // Loop through all floors
        for (Floor floor : parkingLot.getFloors()) {

            // Format occupancy percentage for display
            String line = "Floor " + floor.getFloorNumber()
                    + ": " + String.format("%.1f", floor.getOccupancyRate() * 100) + "%";

            result.add(line);
        }

        return result;
    }

    // Get detailed information for all parking spots
    public List<SpotInfo> getAllSpotInfos() {

        List<SpotInfo> list = new ArrayList<>();

        // Loop through each floor
        for (Floor floor : parkingLot.getFloors()) {

            // Loop through each parking spot on the floor
            for (ParkingSpot spot : floor.getSpots()) {

                // Get the current vehicle (if any)
                Vehicle v = spot.getCurrentVehicle();

                // Get license plate or "-" if empty
                String plate = (v == null) ? "-" : v.getLicensePlate();

                // Create SpotInfo object and add to list
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
}
