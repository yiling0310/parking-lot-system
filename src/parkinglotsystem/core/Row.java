package parkinglotsystem.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Row {

    private final int rowNumber;
    private final List<ParkingSpot> spots;

    public Row(int rowNumber) {

        //Validate row number
        if (rowNumber <= 0) {
            throw new IllegalArgumentException("rowNumber must be >= 1");
        }

        this.rowNumber = rowNumber;

        //Initialize empty list of parking spots
        this.spots = new ArrayList<>();
    }

    //Get row number
    public int getRowNumber() {
        return rowNumber;
    }

    //Add a parking spot to this row
    public void addSpot(ParkingSpot spot) {

        //Ensure spot is not null
        if (spot == null) {
            throw new IllegalArgumentException("spot cannot be null");
        }

        spots.add(spot);
    }

    //Get all parking spots in this row
    public List<ParkingSpot> getSpots() {

        //Return unmodifiable list to protect internal data
        return Collections.unmodifiableList(spots);
    }

    //Get total number of spots in this row
    public int getTotalSpots() {
        return spots.size();
    }

    //Count how many spots are currently occupied
    public int getOccupiedSpots() {

        int count = 0;

        //Loop through all spots and count occupied ones
        for (ParkingSpot s : spots) {
            if (!s.isAvailable()) count++;
        }

        return count;
    }
}
