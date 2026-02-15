package parkinglotsystem.core;

public enum SpotType {

    COMPACT(2.0),
    REGULAR(5.0),
    HANDICAPPED(2.0),
    RESERVED(10.0);

    private final double baseRate;

    SpotType(double baseRate) {
        this.baseRate = baseRate;
    }

    public double getBaseRate() {
        return baseRate;
    }

    public double hourlyRateFor(Vehicle v) {

        boolean isHandicappedVehicle = (v instanceof HandicappedVehicle);
        boolean hasCard = v.isHandicappedCardHolder();
        //Check special condition for handicapped drivers
        if (hasCard) {
            if (this == HANDICAPPED) {
                return 0.0; 
            }
                return 2.0; 
        }
        //If a handicapped vehicle parks in REGULAR or COMPACT without a card
        if (isHandicappedVehicle) {
            if (this == HANDICAPPED) {
                return 2.0;
            }
            return this.baseRate; 
        }

        //Otherwise, return normal base rate
        return baseRate;
    }
}
