package parkinglotsystem.core;

public class Car extends Vehicle {
    
    public Car(String licensePlate, boolean isHandicapped) {
        // Cars are always VehicleType.CAR
        super(licensePlate, VehicleType.CAR, isHandicapped);
    }
}