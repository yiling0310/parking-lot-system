package parkinglotsystem.core;

public class HandicappedVehicle extends Vehicle {
    
    public HandicappedVehicle(String licensePlate, boolean hasHandicappedCard) {
        // Handicapped vehicles are always VehicleType.HANDICAPPED
        // And by definition, they have the card (true)
        super(licensePlate, VehicleType.HANDICAPPED, hasHandicappedCard);
    }
}