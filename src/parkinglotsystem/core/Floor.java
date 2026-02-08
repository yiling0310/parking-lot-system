package parkinglotsystem.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Represents one floor in the parking lot
public class Floor {

    // Floor number (e.g. 1, 2, 3...)
    private final int floorNumber;

    // List of rows on this floor
    private final List<Row> rows;

    // Constructor to create a floor with a given floor number
    public Floor(int floorNumber) {

        // Ensure floor number is valid
        if (floorNumber <= 0) {
            throw new IllegalArgumentException("floorNumber must be >= 1");
        }

        this.floorNumber = floorNumber;

        // Initialize row list
        this.rows = new ArrayList<>();
    }

    // Get the floor number
    public int getFloorNumber() {
        return floorNumber;
    }

    // Add a row to this floor
    public void addRow(Row row) {

        // Ensure row is not null
        if (row == null) {
            throw new IllegalArgumentException("row cannot be null");
        }

        rows.add(row);
    }

    // Get all rows (read-only list)
    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }

    // Get all parking spots on this floor (flattened from all rows)
    // Used mainly for admin listing and reporting
    public List<ParkingSpot> getSpots() {

        List<ParkingSpot> all = new ArrayList<>();

        // Collect spots from each row
        for (Row r : rows) {
            all.addAll(r.getSpots());
        }

        return Collections.unmodifiableList(all);
    }

    // Get total number of parking spots on this floor
    public int getTotalSpots() {

        int total = 0;

        // Sum up spots from all rows
        for (Row r : rows)
            total += r.getTotalSpots();

        return total;
    }

    // Get number of occupied parking spots on this floor
    public int getOccupiedSpots() {

        int occupied = 0;

        // Count occupied spots from all rows
        for (Row r : rows)
            occupied += r.getOccupiedSpots();

        return occupied;
    }

    // Calculate occupancy rate (occupied / total)
    public double getOccupancyRate() {

        int total = getTotalSpots();

        // Avoid division by zero
        if (total == 0)
            return 0.0;

        return (double) getOccupiedSpots() / total;
    }
}
