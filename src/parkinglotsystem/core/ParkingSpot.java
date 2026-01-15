/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkinglotsystem.core;

public class ParkingSpot {

    private final String spotId;          // e.g. F1-R1-S1
    private final SpotType spotType;       // COMPACT / REGULAR / HANDICAPPED / RESERVED
    private SpotStatus status;             // AVAILABLE / OCCUPIED
    private Vehicle currentVehicle;        // null if empty

    public ParkingSpot(String spotId, SpotType spotType) {
        if (spotId == null || spotId.isBlank()) {
            throw new IllegalArgumentException("spotId cannot be empty");
        }
        this.spotId = spotId;
        this.spotType = spotType;
        this.status = SpotStatus.AVAILABLE;
        this.currentVehicle = null;
    }

    public String getSpotId() {
        return spotId;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public SpotStatus getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return status == SpotStatus.AVAILABLE;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    /**
     * Assign a vehicle to this spot (entry).
     * Entry rules (which vehicle can park where)
     * should be validated by Entry/Exit service,
     * not inside this class.
     */
    public void occupy(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("vehicle cannot be null");
        }
        if (!isAvailable()) {
            throw new IllegalStateException("Parking spot already occupied: " + spotId);
        }

        this.currentVehicle = vehicle;
        this.status = SpotStatus.OCCUPIED;
    }

    /**
     * Release this spot when vehicle exits.
     */
    public void release() {
        this.currentVehicle = null;
        this.status = SpotStatus.AVAILABLE;
    }

    /**
     * Get hourly rate for the current vehicle.
     * Uses SpotType pricing rule.
     */
    public double getHourlyRate() {
        if (currentVehicle == null) {
            return 0.0;
        }
        return spotType.hourlyRateFor(currentVehicle);
    }

    @Override
    public String toString() {
        String plate = (currentVehicle == null) ? "-" : currentVehicle.getLicensePlate();
        return spotId + " [" + spotType + ", " + status + ", vehicle=" + plate + "]";
    }
}

