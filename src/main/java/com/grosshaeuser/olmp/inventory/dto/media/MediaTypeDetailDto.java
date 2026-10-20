package com.grosshaeuser.olmp.inventory.dto.media;

import java.time.OffsetDateTime;

public record MediaTypeDetailDto(
        Long id,
        String code,
        String name,
        String description,
        OffsetDateTime createdAt
) {
}

