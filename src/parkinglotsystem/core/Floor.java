/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkinglotsystem.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one parking floor.
 * A floor contains multiple parking spots.
 */
public class Floor {

    private final int floorNumber;                 // Floor identifier (e.g. 1, 2, 3)
    private final List<ParkingSpot> spots;          // All parking spots on this floor

    /**
     * Create a floor with a floor number.
     */
    public Floor(int floorNumber) {
        if (floorNumber <= 0) {
            throw new IllegalArgumentException("floorNumber must be >= 1");
        }
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
    }

    /**
     * Get the floor number.
     */
    public int getFloorNumber() {
        return floorNumber;
    }

    /**
     * Add a parking spot to this floor.
     */
    public void addSpot(ParkingSpot spot) {
        if (spot == null) {
            throw new IllegalArgumentException("spot cannot be null");
        }
        spots.add(spot);
    }

    /**
     * Get all parking spots on this floor (read-only list).
     */
    public List<ParkingSpot> getSpots() {
        return Collections.unmodifiableList(spots);
    }

    /**
     * Get total number of parking spots on this floor.
     */
    public int getTotalSpots() {
        return spots.size();
    }

    /**
     * Get number of occupied parking spots on this floor.
     */
    public int getOccupiedSpots() {
        int count = 0;
        for (ParkingSpot spot : spots) {
            if (!spot.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculate occupancy rate for this floor.
     * Example: 0.5 means 50% occupied.
     */
    public double getOccupancyRate() {
        int total = getTotalSpots();
        if (total == 0) {
            return 0.0;
        }
        return (double) getOccupiedSpots() / total;
    }
}
