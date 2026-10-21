package com.grosshaeuser.olmp.inventory.service.impl;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorCreateDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorEditDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherCreateDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherEditDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.mappers.AuthorMapper;
import com.grosshaeuser.olmp.inventory.mappers.PublisherMapper;
import com.grosshaeuser.olmp.inventory.repository.AuthorRepository;
import com.grosshaeuser.olmp.inventory.repository.PublisherRepository;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.ADMIN;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.ROOT_ADMIN;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.STAFF;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasAnyRole('" + ROOT_ADMIN + "', '" + ADMIN + "', '" + STAFF + "')")
public class MediaReferenceServiceImpl implements MediaReferenceService {
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorMapper authorMapper;
    private final PublisherMapper publisherMapper;
    private final Validator validator;

    @Override
    public AuthorSummaryDto createAuthor(AuthorCreateDto author) {
        if (author == null) {
            throw new IllegalArgumentException("Author data is required");
        }
        AuthorCreateDto normalized = new AuthorCreateDto();
        normalized.setFirstName(optionalText(author.getFirstName()));
        normalized.setLastName(optionalText(author.getLastName()));
        validate(normalized);
        return authorMapper.toSummary(authorRepository.saveAndFlush(authorMapper.toEntity(normalized)));
    }

    @Override
    public PublisherSummaryDto createPublisher(PublisherCreateDto publisher) {
        if (publisher == null) {
            throw new IllegalArgumentException("Publisher data is required");
        }
        PublisherCreateDto normalized = new PublisherCreateDto();
        normalized.setName(optionalText(publisher.getName()));
        normalized.setWebsite(optionalText(publisher.getWebsite()));
        validate(normalized);
        return publisherMapper.toSummary(publisherRepository.saveAndFlush(publisherMapper.toEntity(normalized)));
    }

    @Override
    public AuthorSummaryDto updateAuthor(AuthorEditDto author) {
        if (author == null) {
            throw new IllegalArgumentException("Author data is required");
        }
        AuthorEditDto normalized = new AuthorEditDto();
        normalized.setId(author.getId());
        normalized.setFirstName(optionalText(author.getFirstName()));
        normalized.setLastName(optionalText(author.getLastName()));
        validate(normalized);
        var existing = authorRepository.findById(normalized.getId())
                .orElseThrow(() -> new IllegalArgumentException("Author is no longer available. Reopen the form."));
        authorMapper.update(normalized, existing);
        return authorMapper.toSummary(authorRepository.saveAndFlush(existing));
    }

    @Override
    public PublisherSummaryDto updatePublisher(PublisherEditDto publisher) {
        if (publisher == null) {
            throw new IllegalArgumentException("Publisher data is required");
        }
        PublisherEditDto normalized = new PublisherEditDto();
        normalized.setId(publisher.getId());
        normalized.setName(optionalText(publisher.getName()));
        normalized.setWebsite(optionalText(publisher.getWebsite()));
        validate(normalized);
        var existing = publisherRepository.findById(normalized.getId())
                .orElseThrow(() -> new IllegalArgumentException("Publisher is no longer available. Reopen the form."));
        publisherMapper.update(normalized, existing);
        return publisherMapper.toSummary(publisherRepository.saveAndFlush(existing));
    }

    private void validate(Object dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .sorted().collect(Collectors.joining("; ")));
        }
    }

    private static String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
