package parkinglotsystem.core; 

import java.time.Duration;

public record Bill(
    String ticketId,
    String plateNumber,
    String spotId,
    Duration duration,
    double hourlyRate,
    double parkingFee,
    double overstayFine,
    double misuseFine,
    double previousFines,
    double totalAmount
) {}
