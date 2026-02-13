package parkinglotsystem.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Floor {

    private final int floorNumber;
    private final List<Row> rows = new ArrayList<>(); 
    // This is the ONLY list for admin-added spots
    private final List<ParkingSpot> directSpots = new ArrayList<>(); 

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    // Add a spot directly (Admin feature)
    public void addSpot(ParkingSpot spot) {
        if (spot == null) return;
        directSpots.add(spot);
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void addRow(Row row) {
        if (row == null) throw new IllegalArgumentException("row cannot be null");
        rows.add(row);
    }

    public Row getRow(int rowNumber) {
        for (Row row : rows) {
            if (row.getRowNumber() == rowNumber) {
                return row;
            }
        }
        return null;
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }

    // COMBINE spots from Rows + Admin-added spots
    public List<ParkingSpot> getSpots() {
        List<ParkingSpot> all = new ArrayList<>();
        
        // 1. Get spots from Rows
        for (Row r : rows) {
            all.addAll(r.getSpots());
        }
        // 2. Get spots added directly
        all.addAll(directSpots);

        return Collections.unmodifiableList(all);
    }

    public int getTotalSpots() {
        int total = 0;
        for (Row r : rows) total += r.getTotalSpots();
        total += directSpots.size(); // Add direct spots
        return total;
    }

    public int getOccupiedSpots() {
        int occupied = 0;
        for (Row r : rows) occupied += r.getOccupiedSpots();
        for (ParkingSpot s : directSpots) {
            if (!s.isAvailable()) occupied++;
        }
        return occupied;
    }

    public double getOccupancyRate() {
        int total = getTotalSpots();
        return (total == 0) ? 0.0 : (double) getOccupiedSpots() / total;
    }
}
