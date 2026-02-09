package parkinglotsystem.core;

public class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate, boolean isHandicapped) {
        super(licensePlate, VehicleType.MOTORCYCLE, isHandicapped);
    }
}