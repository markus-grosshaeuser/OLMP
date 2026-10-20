package com.grosshaeuser.olmp.inventory.dto.location;

import java.time.OffsetDateTime;

public record LocationDetailDto(
        Long id,
        String code,
        String name,
        String description,
        LocationSummaryDto parent,
        OffsetDateTime createdAt
) {
}

