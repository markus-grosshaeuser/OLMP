package com.grosshaeuser.olmp.inventory.service;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorCreateDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorEditDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherCreateDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherEditDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;

public interface MediaReferenceService {
    AuthorSummaryDto createAuthor(AuthorCreateDto author);

    AuthorSummaryDto updateAuthor(AuthorEditDto author);

    PublisherSummaryDto createPublisher(PublisherCreateDto publisher);

    PublisherSummaryDto updatePublisher(PublisherEditDto publisher);
}
