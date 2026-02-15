package parkinglotsystem.core;

import java.time.LocalDateTime;

public class Ticket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;

    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        // Generate a random ID (e.g., T-CAR123-Timestamp)
        this.ticketId = "T-" + vehicle.getLicensePlate() + "-" + System.currentTimeMillis();
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = vehicle.getEntryTime() != null ? vehicle.getEntryTime() : LocalDateTime.now();
    }

    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public String getSpotId() { return spot.getSpotId(); }
    public String getLicensePlate() { return vehicle.getLicensePlate(); }
    public LocalDateTime getEntryTime() { return entryTime; }
    public double getHourlyRate() {
        return spot.getHourlyRate();
    }
}