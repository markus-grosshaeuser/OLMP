package com.grosshaeuser.olmp.inventory.dto.media;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;

public record MediaAuthorDto(
        AuthorSummaryDto author,
        String role,
        Short sortOrder
) {
}

