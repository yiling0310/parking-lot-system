package parkinglotsystem.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ParkingLot {

    private static ParkingLot instance;

    private final String name;
    private final List<Floor> floors;
    private final Map<String, ParkingSpot> plateToSpot;
    

    public List<ParkingSpot> getAllSpots() {
        List<ParkingSpot> allSpots = new ArrayList<>();
        for (Floor floor : floors) {
  
            allSpots.addAll(floor.getSpots());
        }
        return allSpots;
    }
  
    private ParkingLot(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        this.name = name;
        this.floors = new ArrayList<>();
        this.plateToSpot = new HashMap<>();
    }

    public static ParkingLot getInstance(String name) {
        if (instance == null) {
            instance = new ParkingLot(name);
        }
        return instance;
    }
    
    //Helper to get the instance if it's already created
    public static ParkingLot getInstance() {
        if (instance == null) {
             throw new IllegalStateException("ParkingLot has not been initialized yet!");
        }
        return instance;
    }

    public String getName() {
        return name;
    }

    public void addFloor(Floor floor) {
        if (floor == null) {
            throw new IllegalArgumentException("floor cannot be null");
        }
        floors.add(floor);
    }

    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    public Floor getFloor(int floorNumber) {
        for (Floor f : floors) {
            if (f.getFloorNumber() == floorNumber) return f;
        }
        return null;
    }

    public int getTotalSpots() {
        int total = 0;
        for (Floor f : floors) {
            total += f.getTotalSpots();
        }
        return total;
    }

    public int getOccupiedSpots() {
        int occupied = 0;
        for (Floor f : floors) {
            occupied += f.getOccupiedSpots();
        }
        return occupied;
    }

    public double getOverallOccupancyRate() {
        int total = getTotalSpots();
        if (total == 0) {
            return 0.0;
        }
        return (double) getOccupiedSpots() / total;
    }

    public Optional<ParkingSpot> findSpotByPlate(String licensePlate) {
        return Optional.ofNullable(plateToSpot.get(licensePlate));
    }

    public void allocateSpot(ParkingSpot spot, Vehicle vehicle) {
        if (spot == null || vehicle == null) {
            throw new IllegalArgumentException("spot/vehicle cannot be null");
        }

        String plate = vehicle.getLicensePlate();

        if (plateToSpot.containsKey(plate)) {
            throw new IllegalStateException("Vehicle already parked: " + plate);
        }

        if (!spot.isAvailable()) {
            throw new IllegalStateException("Spot is not available: " + spot.getSpotId());
        }

        spot.occupy(vehicle);
        plateToSpot.put(plate, spot);
    }
    public void forceRestoreVehicle(Vehicle vehicle, ParkingSpot spot) {
    if (vehicle != null && spot != null) {
        plateToSpot.put(vehicle.getLicensePlate(), spot);
    }
}

    public void releaseSpotByPlate(String licensePlate) {
        ParkingSpot spot = plateToSpot.remove(licensePlate);
        if (spot == null) {
            throw new NoSuchElementException("No parked vehicle found for plate: " + licensePlate);
        }
        spot.release();
    }

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