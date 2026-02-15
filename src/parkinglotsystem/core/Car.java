package parkinglotsystem.core;

public class Car extends Vehicle {
    
    public Car(String licensePlate, boolean isHandicapped) {
        super(licensePlate, VehicleType.CAR, isHandicapped);
    }
}