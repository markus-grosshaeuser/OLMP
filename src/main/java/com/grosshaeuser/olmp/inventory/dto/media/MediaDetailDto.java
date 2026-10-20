package com.grosshaeuser.olmp.inventory.dto.media;

import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;

import java.time.OffsetDateTime;
import java.util.List;

public record MediaDetailDto(
        Long id,
        MediaTypeSummaryDto mediaType,
        PublisherSummaryDto publisher,
        String title,
        String subtitle,
        String edition,
        Short publicationYear,
        String languageCode,
        String coverImageFileName,
        String description,
        List<MediaAuthorDto> authors,
        List<MediaIdentifierDto> identifiers,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public MediaDetailDto {
        authors = List.copyOf(authors);
        identifiers = List.copyOf(identifiers);
    }
}

