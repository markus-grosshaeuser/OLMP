package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaTypeSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;

import java.util.List;

public record MediaFormOptions(
        List<MediaTypeSummaryDto> mediaTypes,
        List<PublisherSummaryDto> publishers,
        List<AuthorSummaryDto> authors
) {
    public MediaFormOptions {
        mediaTypes = List.copyOf(mediaTypes);
        publishers = List.copyOf(publishers);
        authors = List.copyOf(authors);
    }
}
