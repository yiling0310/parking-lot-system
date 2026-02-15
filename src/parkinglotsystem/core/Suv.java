package parkinglotsystem.core;

public class Suv extends Vehicle {
    
    public Suv(String licensePlate, boolean isHandicapped) {
        super(licensePlate, VehicleType.SUV_TRUCK, isHandicapped);
    }
}