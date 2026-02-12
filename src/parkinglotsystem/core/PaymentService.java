package parkinglotsystem.core;

import java.time.Duration;
import java.time.LocalDateTime;
import parkinglotsystem.DatabaseHandler;

public class PaymentService {

    // Default to FIXED_PENALTY as per Feature 3 requirements
    private FineType currentFineScheme = FineType.FIXED_PENALTY;
    private static final double FIXED_FINE_AMOUNT = 50.0;
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

        double rate = tempTicket.getHourlyRate();
        double parkingFee = hours * rate;

        // Calculate Fine based on the Admin's selection
        double overstayFine = 0.0;
        
        if (hours > OVERSTAY_LIMIT_HOURS) {
            switch (currentFineScheme) {
                case FIXED_PENALTY -> overstayFine = FIXED_FINE_AMOUNT;
                case OVERSTAY_HOURLY -> overstayFine = (hours - OVERSTAY_LIMIT_HOURS) * 5.0; // Example: RM 5 per extra hour
                default -> overstayFine = 0.0;
            }
        }

        double previousFines = DatabaseHandler.getPreviousUnpaidFines(plate);
        double total = parkingFee + overstayFine + previousFines;

        return new Bill(
            realTicketId, 
            plate,
            tempTicket.getSpotId(),
            duration,
            rate,
            parkingFee,
            overstayFine,
            previousFines,
            total
        );
    }

    public void processPayment(Bill bill, ParkingLot lot) {
        DatabaseHandler.processExit(bill.ticketId(), bill.spotId(), bill.parkingFee(), bill.previousFines());
        lot.releaseSpotByPlate(bill.plateNumber());
    }
}