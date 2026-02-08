package parkinglotsystem.core;

// Represents different types of parking spots
// Each type has its own base hourly parking rate
public enum SpotType {

    // Compact vehicle parking spot (low rate)
    COMPACT(2.0),

    // Regular parking spot (standard rate)
    REGULAR(5.0),

    // Handicapped parking spot (special pricing rules)
    HANDICAPPED(2.0),

    // Reserved/VIP parking spot (highest rate)
    RESERVED(10.0);

    // Base hourly rate for this spot type
    private final double baseRate;

    // Constructor to assign base rate to each spot type
    SpotType(double baseRate) {
        this.baseRate = baseRate;
    }

    // Get the base hourly rate of this spot type
    public double getBaseRate() {
        return baseRate;
    }

    // Calculate hourly rate based on vehicle information
    // Special rule:
    // Handicapped card holders park for FREE in handicapped spots
    public double hourlyRateFor(Vehicle v) {

        // Check special condition for handicapped drivers
        if (this == HANDICAPPED && v != null && v.isHandicappedCardHolder()) {
            return 0.0;
        }

        // Otherwise, return normal base rate
        return baseRate;
    }
}
