package parkinglotsystem.core;

import java.time.LocalDateTime;

public abstract class Vehicle {

    private final String licensePlate;
    private final VehicleType vehicleType;
    private final boolean handicappedCardHolder;

    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    
    protected Vehicle(String licensePlate, VehicleType vehicleType, boolean handicappedCardHolder) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("licensePlate cannot be empty");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("vehicleType cannot be null");
        }
        
        this.licensePlate = licensePlate.trim().toUpperCase();
        this.vehicleType = vehicleType;
        this.handicappedCardHolder = handicappedCardHolder;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public boolean isHandicappedCardHolder() {
        return handicappedCardHolder;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
}