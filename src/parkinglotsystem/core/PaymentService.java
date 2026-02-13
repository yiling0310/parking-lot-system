package parkinglotsystem.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import parkinglotsystem.DatabaseHandler;

public class PaymentService {

    // Default to FIXED_PENALTY as per Feature 3 requirements
    private FineType currentFineScheme = FineType.FIXED_PENALTY;
    private static final double FIXED_FINE_AMOUNT = 50.0;
    private static final double MISUSE_FINE_AMOUNT = 100.0; // Fine for reserved-spot misuse
    private static final long OVERSTAY_LIMIT_HOURS = 24;
    private static final double OVERSTAY_HOURLY_RATE = 20.0;

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
        LocalDateTime exit = LocalDateTime.now();
        Duration duration = Duration.between(entry, exit);
        
        long hours = (long) Math.ceil(duration.toMinutes() / 60.0);
        if (hours <= 0) hours = 1; 

        FineType appliedFineScheme = DatabaseHandler.getFineSchemeByTicketId(realTicketId);

        // 1. Calculate Standard Parking Fee
        double rate = tempTicket.getSpot().getSpotType().hourlyRateFor(tempTicket.getVehicle());
        double parkingFee = hours * rate;

        // 2. Calculate Overstay Fine
        double overstayFine = 0.0;
        if (hours > OVERSTAY_LIMIT_HOURS) {
            switch (appliedFineScheme) {
                case FIXED_PENALTY -> overstayFine = FIXED_FINE_AMOUNT;
                case PROGRESSIVE -> {
                    // Cumulative progressive tiers as specified in assignment Option B.
                    if (hours > 24) overstayFine += 50.0;
                    if (hours > 48) overstayFine += 100.0;
                    if (hours > 72) {
                        overstayFine += 150.0;
                        overstayFine += 200.0;
                    }
                }
                case OVERSTAY_HOURLY -> overstayFine = (hours - OVERSTAY_LIMIT_HOURS) * OVERSTAY_HOURLY_RATE;
                default -> overstayFine = 0.0;
            }
        }

        // 3. Calculate Misuse Fine (reserved spot without VIP reservation).
        double misuseFine = 0.0;
        boolean hasReservation = DatabaseHandler.hasReservationByTicketId(realTicketId);
        if (tempTicket.getSpot().getSpotType() == SpotType.RESERVED && !hasReservation) {
            misuseFine = MISUSE_FINE_AMOUNT;
        }

        // 4. Fetch Previous Unpaid Fines
        double previousFines = DatabaseHandler.getPreviousUnpaidFines(plate);
        
        // 5. Total Amount
        double mandatoryAmount = parkingFee;
        double total = mandatoryAmount + previousFines;
        total += overstayFine + misuseFine;

        return new Bill(
            realTicketId, 
            plate,
            tempTicket.getSpotId(),
            entry,
            exit,
            duration,
            hours,
            appliedFineScheme,
            rate,
            parkingFee,
            overstayFine,
            misuseFine,
            previousFines,
            mandatoryAmount,
            total
        );
    }

    public void processPayment(
            Bill bill,
            ParkingLot lot,
            String paymentMethod,
            double amountPaid,
            boolean includePreviousFines
    ) {
        if (amountPaid < bill.mandatoryAmount()) {
            throw new IllegalArgumentException("Amount paid must at least cover parking fee.");
        }

        // Store newly incurred fines first; they can remain unpaid or be settled during this payment.
        double totalNewFines = bill.overstayFine() + bill.misuseFine();
        if (totalNewFines > 0) {
            DatabaseHandler.addFineRecord(bill.plateNumber(), totalNewFines, "Unpaid");
        }

        double extraForFines = Math.max(0.0, amountPaid - bill.mandatoryAmount());
        if (includePreviousFines && extraForFines > 0) {
            DatabaseHandler.applyFinePayment(bill.plateNumber(), extraForFines);
        }
        double remainingUnpaidFines = DatabaseHandler.getPreviousUnpaidFines(bill.plateNumber());

        DatabaseHandler.processExit(
            bill.ticketId(), 
            bill.spotId(), 
            bill.parkingFee(),
            paymentMethod,
            amountPaid,
            remainingUnpaidFines
        );

        Optional<ParkingSpot> spotOpt = lot.findSpotByPlate(bill.plateNumber());
        if (spotOpt.isPresent()) {
            Vehicle vehicle = spotOpt.get().getVehicle();
            if (vehicle != null) {
                vehicle.setExitTime(LocalDateTime.now());
            }
        }
        lot.releaseSpotByPlate(bill.plateNumber());
    }
}
