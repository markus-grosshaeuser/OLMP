package com.grosshaeuser.olmp.inventory.dto.publisher;

import java.time.OffsetDateTime;

public record PublisherDetailDto(
        Long id,
        String name,
        String website,
        OffsetDateTime createdAt
) {
}

