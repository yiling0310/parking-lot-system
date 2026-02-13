package parkinglotsystem.core;

import java.time.Duration;
import java.time.LocalDateTime;
import parkinglotsystem.DatabaseHandler;

public class PaymentService {

    // Default to FIXED_PENALTY as per Feature 3 requirements
    private FineType currentFineScheme = FineType.FIXED_PENALTY;
    private static final double FIXED_FINE_AMOUNT = 50.0;
    private static final double MISUSE_FINE_AMOUNT = 100.0; // Fine for reserved-spot misuse
    private static final long OVERSTAY_LIMIT_HOURS = 24;

    // --- METHODS FOR ADMIN PANEL UI ---
    public void setFineScheme(FineType type) {
        this.currentFineScheme = type;
        System.out.println("Fine Scheme updated to: " + type);
    }

    public FineType getCurrentFineScheme() {
        return currentFineScheme;
    }

    public double getTotalRevenue() {
        return DatabaseHandler.getTotalRevenue();
    }
    // ----------------------------------

    public Bill generateBill(Ticket tempTicket) {
        if (tempTicket == null) throw new IllegalArgumentException("Ticket cannot be null");

        String plate = tempTicket.getVehicle().getLicensePlate();
        String realTicketId = DatabaseHandler.getActiveTicketId(plate);
        
        if (realTicketId == null) {
            throw new IllegalStateException("No active ticket found in database for plate: " + plate);
        }

        LocalDateTime entry = tempTicket.getEntryTime();
        Duration duration = Duration.between(entry, LocalDateTime.now());
        
        long hours = (long) Math.ceil(duration.toMinutes() / 60.0);
        if (hours <= 0) hours = 1; 

        // 1. Calculate Standard Parking Fee
        double rate = tempTicket.getSpot().getSpotType().hourlyRateFor(tempTicket.getVehicle());
        double parkingFee = hours * rate;

        // 2. Calculate Overstay Fine
        double overstayFine = 0.0;
        if (hours > OVERSTAY_LIMIT_HOURS) {
            switch (currentFineScheme) {
                case FIXED_PENALTY -> overstayFine = FIXED_FINE_AMOUNT;
                case OVERSTAY_HOURLY -> overstayFine = (hours - OVERSTAY_LIMIT_HOURS) * 5.0; 
                default -> overstayFine = 0.0;
            }
        }

        // 3. Calculate Misuse Fine (Requirement #3)
        // If a vehicle is in a Reserved spot, but it is NOT a Reserved Vehicle (conceptually), fine them.
        // Since we don't have a 'RESERVED' VehicleType, we treat any standard vehicle in a RESERVED spot as misuse.
        double misuseFine = 0.0;
        if (tempTicket.getSpot().getSpotType() == SpotType.RESERVED) {
             // You can add logic here: e.g., if vehicle is not VIP. 
             // For now, any standard car in a reserved spot gets fined.
             misuseFine = MISUSE_FINE_AMOUNT;
        }

        // 4. Fetch Previous Unpaid Fines
        double previousFines = DatabaseHandler.getPreviousUnpaidFines(plate);
        
        // 5. Total Amount
        double total = parkingFee + overstayFine + misuseFine + previousFines;

        return new Bill(
            realTicketId, 
            plate,
            tempTicket.getSpotId(),
            duration,
            rate,
            parkingFee,
            overstayFine,
            misuseFine,
            previousFines,
            total
        );
    }

    public void processPayment(Bill bill, ParkingLot lot) {
        // Ticket stores parking fee only. Fines are stored in parking_fines for clean reporting.
        double totalNewFines = bill.overstayFine() + bill.misuseFine();
        if (totalNewFines > 0) {
            DatabaseHandler.addFineRecord(bill.plateNumber(), totalNewFines, "Paid");
        }
        DatabaseHandler.processExit(
            bill.ticketId(), 
            bill.spotId(), 
            bill.parkingFee(),
            bill.previousFines()
        );
        
        lot.releaseSpotByPlate(bill.plateNumber());
    }
}
