package com.grosshaeuser.olmp.inventory.dto.author;

import java.time.OffsetDateTime;

public record AuthorDetailDto(
        Long id,
        String firstName,
        String lastName,
        OffsetDateTime createdAt
) {
}

