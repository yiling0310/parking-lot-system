package parkinglotsystem.core;

public enum FineType {
    NONE,            // No extra fines
    FIXED_PENALTY,   // Option A: Flat RM 50
    PROGRESSIVE,     // Option B: Tiered fines by overstay window
    OVERSTAY_HOURLY  // Option C: RM 20 per overstay hour
}
