package parkinglotsystem.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    
    private final String ticketId;
    private final String licensePlate;
    private final String spotId;
    private final LocalDateTime entryTime;

    /**
     * Constructor 1: Used when a vehicle enters NOW.
     */
    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        this(vehicle, spot, LocalDateTime.now());
    }

    /**
     * Constructor 2: Used when we need to reconstruct a ticket with a specific OLD time.
     * (Required for accurate billing in ExitPanel)
     */
    public Ticket(Vehicle vehicle, ParkingSpot spot, LocalDateTime specificTime) {
        this.licensePlate = vehicle.getLicensePlate();
        this.spotId = spot.getSpotId();
        this.entryTime = specificTime;
        
        // Format: T-PLATE-TIMESTAMP (e.g., T-ABC1234-202602091200)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        this.ticketId = "T-" + licensePlate + "-" + entryTime.format(fmt);
    }

    public String getTicketId() { return ticketId; }
    public String getLicensePlate() { return licensePlate; }
    public String getSpotId() { return spotId; }
    public LocalDateTime getEntryTime() { return entryTime; }

    @Override
    public String toString() {
        return "Ticket: " + ticketId + " [Spot: " + spotId + "]";
    }
}