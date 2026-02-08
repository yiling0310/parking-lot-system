package parkinglotsystem.core;

// Represents a single parking spot in the parking lot
public class ParkingSpot {

    // Unique ID of the parking spot (e.g. F1-R1-S1)
    private final String spotId;

    // Type of the parking spot (COMPACT / REGULAR / HANDICAPPED / RESERVED)
    private final SpotType spotType;

    // Current status of the spot (AVAILABLE / OCCUPIED)
    private SpotStatus status;

    // Vehicle currently occupying this spot (null if empty)
    private Vehicle currentVehicle;

    // Constructor to create a parking spot with ID and type
    public ParkingSpot(String spotId, SpotType spotType) {

        // Validate spot ID
        if (spotId == null || spotId.isBlank()) {
            throw new IllegalArgumentException("spotId cannot be empty");
        }

        // Validate spot type
        if (spotType == null) {
            throw new IllegalArgumentException("spotType cannot be null");
        }

        this.spotId = spotId;
        this.spotType = spotType;

        // Initialize spot as available
        this.status = SpotStatus.AVAILABLE;

        // No vehicle initially
        this.currentVehicle = null;
    }

    // Get the parking spot ID
    public String getSpotId() {
        return spotId;
    }

    // Get the parking spot type
    public SpotType getSpotType() {
        return spotType;
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
