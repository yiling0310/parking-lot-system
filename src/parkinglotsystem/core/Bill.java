package parkinglotsystem.core; 

import java.time.Duration;
import java.time.LocalDateTime;

public record Bill(
    String ticketId,
    String plateNumber,
    String spotId,
    LocalDateTime entryTime,
    LocalDateTime exitTime,
    Duration duration,
    long billedHours,
    FineType fineScheme,
    double hourlyRate,
    double parkingFee,
    double overstayFine,
    double misuseFine,
    double previousFines,
    double mandatoryAmount,
    double totalAmount
) {}
