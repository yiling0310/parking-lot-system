package parkinglotsystem.core;

import java.util.ArrayList;
import java.util.List;

public class EntryExitService {

    private final ParkingLot parkingLot;

    public EntryExitService(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    /**
     * Requirement: "The system shows available spots of suitable types"
     */
    public List<ParkingSpot> findAvailableSpotsFor(Vehicle vehicle) {
        List<ParkingSpot> suitableSpots = new ArrayList<>();
        
        for (Floor floor : parkingLot.getFloors()) {
            for (ParkingSpot spot : floor.getSpots()) {
                if (spot.isAvailable() && isSpotSuitable(spot, vehicle)) {
                    suitableSpots.add(spot);
                }
            }
        }
        return suitableSpots;
    }

    /**
     * Core Logic: Validates if a specific vehicle type can park in a specific spot type.
     * UPDATED: Strict rules to ensure Handicapped vehicles get Free Parking.
     */
    public boolean isSpotSuitable(ParkingSpot spot, Vehicle vehicle) {
        SpotType sType = spot.getSpotType();
        VehicleType vType = vehicle.getVehicleType();

        // Rule 1: HANDICAPPED Vehicles (from Dropdown)
        // User Request: They can ONLY park in Handicapped spots.
        // Reason: This ensures they always get the 0.00 rate (Free).
        if (vType == VehicleType.HANDICAPPED) {
            return true;
        }

        // Rule 2: Reserved spots are ONLY for Reserved vehicles (blocking others)
        if (sType == SpotType.RESERVED) {
            return true; 
        }

        // Rule 3: Standard Vehicles (Car, Moto, SUV)
        // They cannot park in Handicapped spots anymore (since checkbox is gone).
        return switch (vType) {
            case MOTORCYCLE -> sType == SpotType.COMPACT;
            case CAR -> sType == SpotType.COMPACT || sType == SpotType.REGULAR;
            case SUV_TRUCK -> sType == SpotType.REGULAR;
            default -> false;
        }; 
    }

    /**
     * Process Entry: Validates, Parks, and Generates Ticket.
     */
    public Ticket parkVehicle(String spotId, Vehicle vehicle) {
        // 1. Find the spot object
        ParkingSpot spot = findSpotById(spotId);
        
        // 2. Double-check validation
        if (!isSpotSuitable(spot, vehicle)) {
            throw new IllegalArgumentException("This vehicle type cannot park in this spot.");
        }

        // 3. Occupy the spot (This sets the vehicle into the spot)
        parkingLot.allocateSpot(spot, vehicle);
        
        // 4. Record Entry Time on Vehicle
        vehicle.setEntryTime(java.time.LocalDateTime.now());

        // 5. Generate Ticket
        return new Ticket(vehicle, spot);
    }
    
    // Helper to find a spot object by its String ID
    private ParkingSpot findSpotById(String spotId) {
        for (Floor f : parkingLot.getFloors()) {
            for (ParkingSpot s : f.getSpots()) {
                if (s.getSpotId().equals(spotId)) {
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("Spot ID not found: " + spotId);
    }
}