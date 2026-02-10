package parkinglotsystem.core;

public class ParkingSpot {

    private final String spotId;
    private final int floorNumber; 
    private final int rowNumber;   
    private final SpotType spotType;
    //private SpotStatus status;
    private Vehicle currentVehicle;
    private SpotStatus status = SpotStatus.AVAILABLE;

    public ParkingSpot(String spotId, int floor, int row, SpotType type) {
        this.spotId = spotId;
        this.floorNumber = floor;
        this.rowNumber = row;
        this.spotType = type;
    }
    //     // Validate spot ID
    //     if (spotId == null || spotId.isBlank()) {
    //         throw new IllegalArgumentException("spotId cannot be empty");
    //     }

    //     // Validate spot type
    //     if (spotType == null) {
    //         throw new IllegalArgumentException("spotType cannot be null");
    //     }

    //     this.spotId = spotId;
    //     this.spotType = spotType;

    //     // Initialize spot as available
    //     this.status = SpotStatus.AVAILABLE;

    //     // No vehicle initially
    //     this.currentVehicle = null;
    // }

    // Get the parking spot ID
    public String getSpotId() {
        return spotId;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public int getFloorNumber() { 
        return floorNumber; 
    }

    public int getRowNumber() { 
        return rowNumber; 
    }

    // Get current status of the parking spot
    public SpotStatus getStatus() {
        return status;
    }

    // Check whether the spot is available
    public boolean isAvailable() {
        return status == SpotStatus.AVAILABLE;
    }

    // Get the vehicle currently parked in this spot
    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    // Occupy this parking spot with a vehicle (Entry operation)
    public void occupy(Vehicle vehicle) {

        // Ensure vehicle is not null
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }

        // Prevent double parking
        if (!isAvailable()) {
            throw new IllegalStateException("Parking spot already occupied: " + spotId);
        }

        // Assign vehicle to this spot
        this.currentVehicle = vehicle;

        // Update status
        this.status = SpotStatus.OCCUPIED;
    }

    // Release this parking spot (Exit operation)
    public void release() {

        // Remove vehicle from spot
        this.currentVehicle = null;

        // Mark spot as available
        this.status = SpotStatus.AVAILABLE;
    }

    // Get hourly parking rate for this spot
    // Applies special rule for handicapped drivers if applicable
    public double getHourlyRate() {

        // If spot is empty, return base rate
        if (currentVehicle == null) {
            return spotType.getBaseRate();
        }

        // Otherwise, calculate rate based on vehicle and spot type
        return spotType.hourlyRateFor(currentVehicle);
    }

    // Return formatted string for displaying spot information
    @Override
    public String toString() {

        // Show license plate or "-" if empty
        String plate = (currentVehicle == null) ? "-" : currentVehicle.getLicensePlate();

        return spotId + " [" + spotType + ", " + status + ", vehicle=" + plate + "]";
    }
}
