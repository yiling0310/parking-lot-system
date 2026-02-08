package parkinglotsystem.core;

import java.util.*;

// Represents the entire parking lot and manages all floors and parking operations
public class ParkingLot {

    // Name of the parking lot
    private final String name;

    // List of all floors in the parking lot
    private final List<Floor> floors;

    // Mapping: license plate -> parking spot
    // Used for fast lookup when releasing vehicles
    private final Map<String, ParkingSpot> plateToSpot;

    // Constructor to create a parking lot with a given name
    public ParkingLot(String name) {

        // Ensure name is valid
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be empty");
        }

        this.name = name;

        // Initialize floor list
        this.floors = new ArrayList<>();

        // Initialize plate-to-spot mapping
        this.plateToSpot = new HashMap<>();
    }

    // Get the parking lot name
    public String getName() {
        return name;
    }

    // Add a floor to the parking lot
    public void addFloor(Floor floor) {

        // Ensure floor is not null
        if (floor == null) {
            throw new IllegalArgumentException("floor cannot be null");
        }

        floors.add(floor);
    }

    // Get all floors (read-only list)
    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    // Get total number of parking spots in the parking lot
    public int getTotalSpots() {

        int total = 0;

        // Sum spots from all floors
        for (Floor f : floors)
            total += f.getTotalSpots();

        return total;
    }

    // Get total number of occupied spots in the parking lot
    public int getOccupiedSpots() {

        int occupied = 0;

        // Sum occupied spots from all floors
        for (Floor f : floors)
            occupied += f.getOccupiedSpots();

        return occupied;
    }

    // Calculate overall occupancy rate
    public double getOverallOccupancyRate() {

        int total = getTotalSpots();

        // Avoid division by zero
        if (total == 0)
            return 0.0;

        return (double) getOccupiedSpots() / total;
    }

    // Find a parking spot using vehicle license plate
    public Optional<ParkingSpot> findSpotByPlate(String licensePlate) {

        // Return empty if plate is null
        if (licensePlate == null)
            return Optional.empty();

        // Look up in map
        return Optional.ofNullable(
                plateToSpot.get(licensePlate.trim().toUpperCase())
        );
    }

    // Allocate a parking spot to a vehicle (Entry operation)
    public void allocateSpot(ParkingSpot spot, Vehicle vehicle) {

        // Ensure inputs are valid
        if (spot == null || vehicle == null) {
            throw new IllegalArgumentException("spot/vehicle cannot be null");
        }

        // Normalize license plate
        String plate = vehicle.getLicensePlate().trim().toUpperCase();

        // Prevent same vehicle from parking twice
        if (plateToSpot.containsKey(plate)) {
            throw new IllegalStateException("Vehicle already parked: " + plate);
        }

        // Ensure spot is available
        if (!spot.isAvailable()) {
            throw new IllegalStateException("Spot is not available: " + spot.getSpotId());
        }

        // Occupy the spot
        spot.occupy(vehicle);

        // Record mapping for fast lookup
        plateToSpot.put(plate, spot);
    }

    // Release a parking spot using license plate (Exit operation)
    public void releaseSpotByPlate(String licensePlate) {

        // Ensure plate is not null
        if (licensePlate == null) {
            throw new IllegalArgumentException("licensePlate cannot be null");
        }

        // Normalize license plate
        String plate = licensePlate.trim().toUpperCase();

        // Remove mapping and get corresponding spot
        ParkingSpot spot = plateToSpot.remove(plate);

        // Throw error if vehicle not found
        if (spot == null) {
            throw new NoSuchElementException("No parked vehicle found for plate: " + plate);
        }

        // Release the spot
        spot.release();
    }

    // Get formatted summary of parking lot status
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
