package com.grosshaeuser.olmp.inventory.dto.media;

import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;

import java.util.List;

public record MediaSummaryDto(
        Long id,
        String title,
        MediaTypeSummaryDto mediaType,
        PublisherSummaryDto publisher,
        Short publicationYear,
        List<MediaAuthorDto> authors
) {

    public MediaSummaryDto {
        authors = List.copyOf(authors);
    }
}

