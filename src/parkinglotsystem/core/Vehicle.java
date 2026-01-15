/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package parkinglotsystem.core;

import java.time.LocalDateTime;

/**
 * Represents a vehicle entering the parking system.
 * A vehicle is identified by its license plate and type.
 */
public class Vehicle {

    private final String licensePlate;          // Unique vehicle identifier
    private final VehicleType vehicleType;       // Type of vehicle (CAR, MOTORCYCLE, etc.)
    private final boolean handicappedCardHolder; // Whether the driver has a handicapped card

    private LocalDateTime entryTime;              // Time when vehicle enters parking lot
    private LocalDateTime exitTime;               // Time when vehicle exits parking lot

    /**
     * Create a vehicle with basic information.
     */
    public Vehicle(String licensePlate, VehicleType vehicleType, boolean handicappedCardHolder) {
        // Validate license plate
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("licensePlate cannot be empty");
        }
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.handicappedCardHolder = handicappedCardHolder;
    }

    /**
     * Get vehicle license plate.
     */
    public String getLicensePlate() {
        return licensePlate;
    }

    /**
     * Get vehicle type.
     */
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    /**
     * Check if this vehicle belongs to a handicapped card holder.
     */
    public boolean isHandicappedCardHolder() {
        return handicappedCardHolder;
    }

    /**
     * Get vehicle entry time.
     */
    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    /**
     * Set vehicle entry time.
     * Usually called when the vehicle enters the parking lot.
     */
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    /**
     * Get vehicle exit time.
     */
    public LocalDateTime getExitTime() {
        return exitTime;
    }

    /**
     * Set vehicle exit time.
     * Usually called when the vehicle exits the parking lot.
     */
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
}
