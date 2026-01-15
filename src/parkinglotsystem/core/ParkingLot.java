/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkinglotsystem.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Represents the whole parking lot.
 * A parking lot contains multiple floors and tracks parked vehicles.
 */
public class ParkingLot {

    private final String name;                    // Parking lot name
    private final List<Floor> floors;             // All floors in the parking lot

    // Quick lookup: licensePlate -> ParkingSpot
    // This helps Exit/Admin to find where a vehicle is parked.
    private final Map<String, ParkingSpot> plateToSpot;

    /**
     * Create a parking lot with a name.
     */
    public ParkingLot(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        this.name = name;
        this.floors = new ArrayList<>();
        this.plateToSpot = new HashMap<>();
    }

    /**
     * Get parking lot name.
     */
    public String getName() {
        return name;
    }

    /**
     * Add a floor to the parking lot.
     */
    public void addFloor(Floor floor) {
        if (floor == null) {
            throw new IllegalArgumentException("floor cannot be null");
        }
        floors.add(floor);
    }

    /**
     * Get all floors (read-only list).
     */
    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    /**
     * Total number of spots in the whole parking lot.
     */
    public int getTotalSpots() {
        int total = 0;
        for (Floor f : floors) {
            total += f.getTotalSpots();
        }
        return total;
    }

    /**
     * Total number of occupied spots in the whole parking lot.
     */
    public int getOccupiedSpots() {
        int occupied = 0;
        for (Floor f : floors) {
            occupied += f.getOccupiedSpots();
        }
        return occupied;
    }

    /**
     * Overall occupancy rate of the parking lot.
     * Example: 0.75 means 75% occupied.
     */
    public double getOverallOccupancyRate() {
        int total = getTotalSpots();
        if (total == 0) {
            return 0.0;
        }
        return (double) getOccupiedSpots() / total;
    }

    /**
     * Find a parked vehicle's spot by license plate.
     */
    public Optional<ParkingSpot> findSpotByPlate(String licensePlate) {
        return Optional.ofNullable(plateToSpot.get(licensePlate));
    }

    /**
     * Allocate a specific spot to a vehicle (Entry).
     * Note: Vehicle-to-spot rules (who can park where) should be validated
     * by Entry/Exit logic (Member 2), not here.
     */
    public void allocateSpot(ParkingSpot spot, Vehicle vehicle) {
        if (spot == null || vehicle == null) {
            throw new IllegalArgumentException("spot/vehicle cannot be null");
        }

        String plate = vehicle.getLicensePlate();

        // Prevent the same vehicle from being parked twice
        if (plateToSpot.containsKey(plate)) {
            throw new IllegalStateException("Vehicle already parked: " + plate);
        }

        // Ensure the spot is available
        if (!spot.isAvailable()) {
            throw new IllegalStateException("Spot is not available: " + spot.getSpotId());
        }

        // Occupy the spot and remember where the vehicle is parked
        spot.occupy(vehicle);
        plateToSpot.put(plate, spot);
    }

    /**
     * Release a vehicle from its spot by license plate (Exit/Admin).
     */
    public void releaseSpotByPlate(String licensePlate) {
        ParkingSpot spot = plateToSpot.remove(licensePlate);
        if (spot == null) {
            throw new NoSuchElementException("No parked vehicle found for plate: " + licensePlate);
        }
        spot.release();
    }

    /**
     * Simple summary string for Admin/Reporting UI.
     */
    public String getStatusSummary() {
        return String.format(
                "%s | Floors=%d | Occupied=%d/%d (%.1f%%)",
                name,
                floors.size(),
                getOccupiedSpots(),
                getTotalSpots(),
                getOverallOccupancyRate() * 100
        );
    }
}

