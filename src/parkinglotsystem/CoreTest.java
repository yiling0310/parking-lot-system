/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkinglotsystem;

import parkinglotsystem.core.*;

/**
 * Simple test class to verify core logic works correctly.
 * This is NOT part of the final system, only for testing.
 */
public class CoreTest {

    public static void main(String[] args) {

        // 1) Create parking lot
        ParkingLot parkingLot = new ParkingLot("University Parking");

        // 2) Create floors
        Floor floor1 = new Floor(1);
        Floor floor2 = new Floor(2);

        // 3) Add parking spots to floors
        floor1.addSpot(new ParkingSpot("F1-C1", SpotType.COMPACT));
        floor1.addSpot(new ParkingSpot("F1-R1", SpotType.REGULAR));
        floor1.addSpot(new ParkingSpot("F1-H1", SpotType.HANDICAPPED));

        floor2.addSpot(new ParkingSpot("F2-R1", SpotType.REGULAR));
        floor2.addSpot(new ParkingSpot("F2-V1", SpotType.RESERVED));

        // 4) Add floors to parking lot
        parkingLot.addFloor(floor1);
        parkingLot.addFloor(floor2);

        // 5) Create vehicles
        Vehicle car = new Vehicle("ABC123", VehicleType.CAR, false);
        Vehicle handicappedCar = new Vehicle("H999", VehicleType.HANDICAPPED, true);

        // 6) Allocate vehicles to spots (Entry simulation)
        // NOTE: We are not checking parking rules here (Member2 will handle that).
        parkingLot.allocateSpot(floor1.getSpots().get(0), car);              // COMPACT spot
        parkingLot.allocateSpot(floor1.getSpots().get(2), handicappedCar);   // HANDICAPPED spot (should be FREE)

        // 7) Print overall parking status
        System.out.println("=== Parking Lot Status ===");
        System.out.println(parkingLot.getStatusSummary());

        // 8) Print each floor and each spot details + hourly rate
        for (Floor f : parkingLot.getFloors()) {
            System.out.println("\nFloor " + f.getFloorNumber()
                    + " occupancy: " + String.format("%.1f", f.getOccupancyRate() * 100) + "%");

            for (ParkingSpot s : f.getSpots()) {
                System.out.println(s + " | Hourly Rate: RM " + s.getHourlyRate());
            }
        }

        // 9) Release one vehicle (Exit simulation)
        parkingLot.releaseSpotByPlate("ABC123");

        // 10) Print status after exit
        System.out.println("\n=== After Vehicle Exit ===");
        System.out.println(parkingLot.getStatusSummary());
    }
}
