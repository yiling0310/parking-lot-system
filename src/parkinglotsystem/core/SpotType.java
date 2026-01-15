/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
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

    // FREE only if handicapped card holder parks in handicapped spot
    public double hourlyRateFor(Vehicle v) {
        if (this == HANDICAPPED && v != null && v.isHandicappedCardHolder()) {
            return 0.0;
        }
        return baseRate;
    }
}

