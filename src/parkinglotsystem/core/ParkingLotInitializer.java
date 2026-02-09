package parkinglotsystem.core;

// Utility class used to build and initialize a default parking lot structure
public class ParkingLotInitializer {

    // Private constructor to prevent object creation
    // This class is used only for static initialization methods
    private ParkingLotInitializer() {}

    // Create and initialize a parking lot with default structure
    public static ParkingLot createDefaultLot(
            String name,          // Parking lot name
            int floorCount,       // Number of floors
            int rowsPerFloor,     // Number of rows per floor
            int spotsPerRow,      // Number of spots per row
            SpotType[] typePattern // Pattern for assigning spot types
    ) {

        // Validate input parameters
        if (floorCount <= 0 || rowsPerFloor <= 0 || spotsPerRow <= 0) {
            throw new IllegalArgumentException("floorCount/rowsPerFloor/spotsPerRow must be > 0");
        }

        // Ensure spot type pattern is valid
        if (typePattern == null || typePattern.length == 0) {
            throw new IllegalArgumentException("typePattern cannot be empty");
        }

        // Create parking lot instance
        // Use Singleton Instance
        ParkingLot lot = ParkingLot.getInstance(name);

        // Index used to cycle through spot type pattern
        int idx = 0;

        // Create floors
        for (int f = 1; f <= floorCount; f++) {

            Floor floor = new Floor(f);

            // Create rows for each floor
            for (int r = 1; r <= rowsPerFloor; r++) {

                Row row = new Row(r);

                // Create parking spots for each row
                for (int s = 1; s <= spotsPerRow; s++) {

                    // Assign spot type using repeating pattern
                    SpotType type = typePattern[idx % typePattern.length];
                    idx++;

                    // Generate unique spot ID (F?-R?-S?)
                    String spotId = "F" + f + "-R" + r + "-S" + s;

                    // Create parking spot
                    ParkingSpot spot = new ParkingSpot(spotId, type);

                    // Add spot to row
                    row.addSpot(spot);
                }

                // Add row to floor
                floor.addRow(row);
            }

            // Add floor to parking lot
            lot.addFloor(floor);
        }

        // Return fully initialized parking lot
        return lot;
    }
}
