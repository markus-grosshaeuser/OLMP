package com.grosshaeuser.olmp.inventory.service;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorCreateDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherCreateDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;

public interface MediaReferenceService {
    AuthorSummaryDto createAuthor(AuthorCreateDto author);

    PublisherSummaryDto createPublisher(PublisherCreateDto publisher);
}
