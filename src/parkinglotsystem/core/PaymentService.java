package parkinglotsystem.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PaymentService {

    // Simulating a database of unpaid fines (License Plate -> Amount Owed)
    private static final Map<String, Double> outstandingFinesDB = new HashMap<>();
    
    // Total Revenue collected (Parking Fees + Fines)
    private double totalRevenue = 0.0;
    
    // Current fine scheme selected by Admin (Default to FIXED)
    private FineType currentFineScheme = FineType.FIXED;

    /**
     * Admin can change the fine scheme here.
     */
    public void setFineScheme(FineType fineType) {
        this.currentFineScheme = fineType;
    }

    public FineType getCurrentFineScheme() {
        return currentFineScheme;
    }

    /**
     * Get total revenue for Admin Dashboard.
     */
    public double getTotalRevenue() {
        return totalRevenue;
    }

    /**
     * Core Method: Calculates the total bill for a vehicle exiting NOW.
     */
    public Bill calculateBill(Ticket ticket, ParkingSpot spot) {
        LocalDateTime entry = ticket.getEntryTime();
        LocalDateTime exit = LocalDateTime.now();
        
        // 1. Calculate Duration (Rounded UP to nearest hour)
        long hours = calculateHours(entry, exit);
        
        // 2. Calculate Parking Fee (Rate * Hours)
        double hourlyRate = spot.getHourlyRate();
        double parkingFee = hourlyRate * hours;
        
        // 3. Calculate Fine (if overstaying > 24 hours)
        double currentFine = calculateOverstayFine(hours);
        
        // 4. Check for previous unpaid fines
        double previousFines = outstandingFinesDB.getOrDefault(ticket.getLicensePlate(), 0.0);
        
        return new Bill(ticket.getLicensePlate(), hours, parkingFee, currentFine, previousFines);
    }
    
    /**
     * Process Payment: Clears fines, saves transaction, and updates Revenue.
     * UPDATED to accept amountPaid.
     */
    public void processPayment(String licensePlate, double amountPaid) {
        // Add to total revenue
        this.totalRevenue += amountPaid;
        
        // Remove any outstanding fines since they are now paid
        outstandingFinesDB.remove(licensePlate);
    }
    
    /**
     * Helper: Ceil Rounding for hours (e.g., 1.1 hours -> 2 hours)
     */
    private long calculateHours(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        long minutes = duration.toMinutes();
        if (minutes <= 0) return 0;
        
        // Ceiling division: (minutes + 59) / 60
        return (minutes + 59) / 60;
    }

    /**
     * Helper: Calculates fine based on the selected Admin Scheme.
     * Rules from Assignment PDF Section 4.
     */
    private double calculateOverstayFine(long hours) {
        if (hours <= 24) return 0.0; // No fine if within 24 hours
        
        long overstayHours = hours - 24;
        
        switch (currentFineScheme) {
            case FIXED:
                // Option A: Flat RM 50
                return 50.0;
                
            case HOURLY:
                // Option C: RM 20 per overstay hour
                return overstayHours * 20.0;
                
            case PROGRESSIVE:
                // Option B: Progressive tiers
                // First 24h overstay (hrs 24-48): RM 50
                // Next 24h overstay (hrs 48-72): + RM 100
                // Beyond 72h: + RM 150
                
                double fine = 50.0; // Base fine for breaking 24h limit
                
                if (hours > 48) {
                    fine += 100.0; // Add RM 100
                }
                if (hours > 72) {
                    fine += 150.0; // Add RM 150
                }
                return fine;
                
            default:
                return 0.0;
        }
    }

    /**
     * Inner class to hold the Billing Result
     */
    public static class Bill {
        public final String plate;
        public final long hours;
        public final double parkingFee;
        public final double currentFine;
        public final double previousFines;
        public final double totalAmount;

        public Bill(String plate, long hours, double parkingFee, double currentFine, double previousFines) {
            this.plate = plate;
            this.hours = hours;
            this.parkingFee = parkingFee;
            this.currentFine = currentFine;
            this.previousFines = previousFines;
            this.totalAmount = parkingFee + currentFine + previousFines;
        }
    }
}