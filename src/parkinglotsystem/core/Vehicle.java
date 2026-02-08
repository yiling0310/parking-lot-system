package parkinglotsystem.core;

import java.time.LocalDateTime;

// Represents a vehicle entering and exiting the parking lot
// Stores vehicle identity and parking time information
public class Vehicle {

    // Unique license plate number of the vehicle
    private final String licensePlate;

    // Type of the vehicle (CAR, MOTORCYCLE, SUV, etc.)
    private final VehicleType vehicleType;

    // Indicates whether the driver has a handicapped card
    private final boolean handicappedCardHolder;

    // Time when the vehicle entered the parking lot
    private LocalDateTime entryTime;

    // Time when the vehicle exited the parking lot
    private LocalDateTime exitTime;

    // Constructor to create a vehicle object
    public Vehicle(String licensePlate, VehicleType vehicleType, boolean handicappedCardHolder) {

        // Validate license plate
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("licensePlate cannot be empty");
        }

        // Validate vehicle type
        if (vehicleType == null) {
            throw new IllegalArgumentException("vehicleType cannot be null");
        }

        // Normalize license plate format (uppercase and trimmed)
        this.licensePlate = licensePlate.trim().toUpperCase();

        this.vehicleType = vehicleType;

        // Store handicapped card status
        this.handicappedCardHolder = handicappedCardHolder;
    }

    // Get vehicle license plate
    public String getLicensePlate() {
        return licensePlate;
    }

    // Get vehicle type
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    // Check if driver is a handicapped card holder
    public boolean isHandicappedCardHolder() {
        return handicappedCardHolder;
    }

    // Get vehicle entry time
    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    // Set vehicle entry time when entering parking lot
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    // Get vehicle exit time
    public LocalDateTime getExitTime() {
        return exitTime;
    }

    // Set vehicle exit time when leaving parking lot
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
}
