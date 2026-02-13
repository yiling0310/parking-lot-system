package parkinglotsystem.core;

public class ParkingSpot {
    private final String spotId;
    private final int floorNumber;
    private final int rowNumber;
    private final SpotType spotType;
    private double hourlyRate;
    private Vehicle currentVehicle; 

    public ParkingSpot(String spotId, int floorNumber, int rowNumber, SpotType spotType) {
        this.spotId = spotId;
        this.floorNumber = floorNumber;
        this.rowNumber = rowNumber;
        this.spotType = spotType;
        this.currentVehicle = null;
    }

    public String getSpotId() { return spotId; }
    public int getFloorNumber() { return floorNumber; }
    public int getRowNumber() { return rowNumber; }
    public SpotType getSpotType() { return spotType; }

    public double getHourlyRate() {
        if (currentVehicle == null) {
            return switch (spotType) {
                case COMPACT -> 2.0;
                case REGULAR -> 5.0;
                case HANDICAPPED -> 2.0;
                case RESERVED -> 10.0;
                default -> 5.0;
            };
        }

        if (currentVehicle instanceof HandicappedVehicle) {
            HandicappedVehicle hv = (HandicappedVehicle) currentVehicle;
            
            if (hv.isHandicappedCardHolder()) {
                if (spotType == SpotType.HANDICAPPED) {
                    return 0.0; 
                } else {
                    return 2.0;
                }
            } else {
                return spotType.hourlyRateFor(currentVehicle); 
            }
        }

        return switch (spotType) {
            case COMPACT -> 2.0;
            case REGULAR -> 5.0;
            case HANDICAPPED -> 2.0; 
            case RESERVED -> 10.0;
            default -> 5.0;
        };
    }

    // This is the getter your AdminService needs!
    public Vehicle getVehicle() {
        return currentVehicle;
    }
    
    // Alias if needed by older code
    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    public boolean isAvailable() {
        return currentVehicle == null;
    }

    public SpotStatus getStatus() {
        return isAvailable() ? SpotStatus.AVAILABLE : SpotStatus.OCCUPIED;
    }

    public void occupy(Vehicle vehicle) {
        if (vehicle == null) throw new IllegalArgumentException("Vehicle cannot be null");
        this.currentVehicle = vehicle;
    }

    public void release() {
        this.currentVehicle = null;
    }
}
