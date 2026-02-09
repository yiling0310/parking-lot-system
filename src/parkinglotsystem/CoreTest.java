package parkinglotsystem;

import parkinglotsystem.core.*;

public class CoreTest {

    public static void main(String[] args) {

        // 1) Create parking lot
        ParkingLot parkingLot = ParkingLot.getInstance("University Parking");

        // 2) Create floors
        Floor floor1 = new Floor(1);
        Floor floor2 = new Floor(2);

        // 3) Create rows
        Row f1r1 = new Row(1);
        Row f1r2 = new Row(2);

        Row f2r1 = new Row(1);

        // 4) Add parking spots to rows (SpotId format can be anything, but keep consistent)
        f1r1.addSpot(new ParkingSpot("F1-R1-S1", SpotType.COMPACT));
        f1r1.addSpot(new ParkingSpot("F1-R1-S2", SpotType.REGULAR));
        f1r2.addSpot(new ParkingSpot("F1-R2-S1", SpotType.HANDICAPPED));

        f2r1.addSpot(new ParkingSpot("F2-R1-S1", SpotType.REGULAR));
        f2r1.addSpot(new ParkingSpot("F2-R1-S2", SpotType.RESERVED));

        // 5) Add rows to floors
        floor1.addRow(f1r1);
        floor1.addRow(f1r2);

        floor2.addRow(f2r1);

        // 6) Add floors to parking lot
        parkingLot.addFloor(floor1);
        parkingLot.addFloor(floor2);

        // 7) Create vehicles
        // 7) Create vehicles (USING NEW SUBCLASSES)
        Vehicle car = new Car("ABC123", false); 
        Vehicle handicappedCar = new HandicappedVehicle("H999");

        // 8) Allocate vehicles to spots (Entry simulation)
        // NOTE: Member2 will validate rules later. Here we just allocate.
        parkingLot.allocateSpot(floor1.getSpots().get(0), car);              // first spot
        parkingLot.allocateSpot(floor1.getSpots().get(2), handicappedCar);   // handicapped spot

        // 9) Print overall parking status
        System.out.println("=== Parking Lot Status ===");
        System.out.println(parkingLot.getStatusSummary());

        // 10) Print each floor and each spot details + hourly rate
        for (Floor f : parkingLot.getFloors()) {
            System.out.println("\nFloor " + f.getFloorNumber()
                    + " occupancy: " + String.format("%.1f", f.getOccupancyRate() * 100) + "%");

            for (ParkingSpot s : f.getSpots()) {
                System.out.println(s + " | Hourly Rate: RM " + s.getHourlyRate());
            }
        }

        // 11) Release one vehicle (Exit simulation)
        parkingLot.releaseSpotByPlate("ABC123");

        // 12) Print status after exit
        System.out.println("\n=== After Vehicle Exit ===");
        System.out.println(parkingLot.getStatusSummary());
    }
}
