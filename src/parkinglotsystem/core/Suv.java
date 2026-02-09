package parkinglotsystem.core;

public class Suv extends Vehicle {
    
    public Suv(String licensePlate, boolean isHandicapped) {
        // SUVs are always VehicleType.SUV_TRUCK
        super(licensePlate, VehicleType.SUV_TRUCK, isHandicapped);
    }
}