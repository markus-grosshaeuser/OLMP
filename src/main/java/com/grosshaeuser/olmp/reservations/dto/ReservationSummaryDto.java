
package com.grosshaeuser.olmp.reservations.dto;

import com.grosshaeuser.olmp.reservations.ReservationStatus;
import java.time.OffsetDateTime;

public record ReservationSummaryDto(
        Long id,
        String memberName,
        String mediaTitle,
        OffsetDateTime reservationDate,
        ReservationStatus status
) {
}