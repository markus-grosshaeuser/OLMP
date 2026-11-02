
package com.grosshaeuser.olmp.reservations.dto;

import com.grosshaeuser.olmp.inventory.dto.media.MediaSummaryDto;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.reservations.ReservationStatus;

import java.time.OffsetDateTime;

public record ReservationDto(
        Long id,
        MemberDTO member,
        MediaSummaryDto media,
        OffsetDateTime reservationDate,
        ReservationStatus status,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}