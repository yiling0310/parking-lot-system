package parkinglotsystem.core;

public class HandicappedVehicle extends Vehicle {
    
    public HandicappedVehicle(String licensePlate, boolean hasHandicappedCard) {
        super(licensePlate, VehicleType.HANDICAPPED, hasHandicappedCard);
    }

    public boolean hasHandicappedCard() {
        return isHandicappedCardHolder();
    }
}