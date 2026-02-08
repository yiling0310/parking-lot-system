package parkinglotsystem.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Represents a row inside a parking floor
// Each row contains multiple parking spots
public class Row {

    // Row number (e.g. 1, 2, 3...)
    private final int rowNumber;

    // List of parking spots in this row
    private final List<ParkingSpot> spots;

    // Constructor to create a row with a specific row number
    public Row(int rowNumber) {

        // Validate row number
        if (rowNumber <= 0) {
            throw new IllegalArgumentException("rowNumber must be >= 1");
        }

        this.rowNumber = rowNumber;

        // Initialize empty list of parking spots
        this.spots = new ArrayList<>();
    }

    // Get the row number
    public int getRowNumber() {
        return rowNumber;
    }

    // Add a parking spot to this row
    public void addSpot(ParkingSpot spot) {

        // Ensure spot is not null
        if (spot == null) {
            throw new IllegalArgumentException("spot cannot be null");
        }

        spots.add(spot);
    }

    // Get all parking spots in this row (read-only)
    public List<ParkingSpot> getSpots() {

        // Return unmodifiable list to protect internal data
        return Collections.unmodifiableList(spots);
    }

    // Get total number of spots in this row
    public int getTotalSpots() {
        return spots.size();
    }

    // Count how many spots are currently occupied
    public int getOccupiedSpots() {

        int count = 0;

        // Loop through all spots and count occupied ones
        for (ParkingSpot s : spots) {
            if (!s.isAvailable()) count++;
        }

        return count;
    }
}
