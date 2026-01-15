/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkinglotsystem.admin;

import parkinglotsystem.core.Floor;
import parkinglotsystem.core.ParkingLot;
import parkinglotsystem.core.ParkingSpot;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminService provides read-only information for admin/reporting purposes.
 * UI should call this service instead of accessing core classes directly.
 */
public class AdminService {

    private final ParkingLot parkingLot;

    /**
     * Create AdminService for a parking lot.
     */
    public AdminService(ParkingLot parkingLot) {
        if (parkingLot == null) {
            throw new IllegalArgumentException("parkingLot cannot be null");
        }
        this.parkingLot = parkingLot;
    }

    /**
     * Get a summary string of the whole parking lot.
     * Example: "Occupied=3/10 (30%)"
     */
    public String getSummary() {
        return parkingLot.getStatusSummary();
    }

    /**
     * Get overall occupancy rate (0.0 - 1.0).
     */
    public double getOverallOccupancyRate() {
        return parkingLot.getOverallOccupancyRate();
    }

    /**
     * Get occupancy rate for each floor.
     * Example output:
     * "Floor 1: 40.0%"
     */
    public List<String> getFloorOccupancyLines() {
        List<String> result = new ArrayList<>();

        for (Floor floor : parkingLot.getFloors()) {
            String line = "Floor " + floor.getFloorNumber()
                    + ": " + String.format("%.1f", floor.getOccupancyRate() * 100) + "%";
            result.add(line);
        }
        return result;
    }

    /**
     * List all parking spots in the parking lot.
     * Used by Admin UI to display spot status.
     */
    public List<String> listAllSpots() {
        List<String> result = new ArrayList<>();

        for (Floor floor : parkingLot.getFloors()) {
            result.add("=== Floor " + floor.getFloorNumber() + " ===");
            for (ParkingSpot spot : floor.getSpots()) {
                result.add(spot.toString());
            }
        }
        return result;
    }
}

