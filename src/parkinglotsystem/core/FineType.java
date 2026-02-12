package parkinglotsystem.core;

public enum FineType {
    NONE,           // No extra fines
    OVERSTAY_HOURLY, // Charge per hour overstayed
    FIXED_PENALTY    // One-time RM 50 fine (This is what we use in logic)
}