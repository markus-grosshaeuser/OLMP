package com.grosshaeuser.olmp.inventory.service.impl;

import com.grosshaeuser.olmp.inventory.dto.location.LocationSummaryDto;
import com.grosshaeuser.olmp.inventory.repository.LocationRepository;
import com.grosshaeuser.olmp.inventory.mappers.AuthorMapper;
import com.grosshaeuser.olmp.inventory.mappers.MediaTypeMapper;
import com.grosshaeuser.olmp.inventory.mappers.PublisherMapper;
import com.grosshaeuser.olmp.inventory.service.MediaLookupService;
import com.grosshaeuser.olmp.inventory.repository.AuthorRepository;
import com.grosshaeuser.olmp.ui.internal_ui.shared.inventory.MediaFormOptions;
import com.grosshaeuser.olmp.inventory.repository.MediaTypeRepository;
import com.grosshaeuser.olmp.inventory.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.ADMIN;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.ROOT_ADMIN;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.STAFF;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('" + ROOT_ADMIN + "', '" + ADMIN + "', '" + STAFF + "')")
public class MediaLookupServiceImpl implements MediaLookupService {

    private final MediaTypeRepository mediaTypeRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final LocationRepository locationRepository;
    private final MediaTypeMapper mediaTypeMapper;
    private final PublisherMapper publisherMapper;
    private final AuthorMapper authorMapper;

    @Override
    public MediaFormOptions getFormOptions() {
        return new MediaFormOptions(
                mediaTypeRepository.findAll(Sort.by("name", "id")).stream().map(mediaTypeMapper::toSummary).toList(),
                publisherRepository.findAll(Sort.by("name", "id")).stream().map(publisherMapper::toSummary).toList(),
                authorRepository.findAll(Sort.by("lastName", "firstName", "id")).stream()
                        .map(authorMapper::toSummary).toList(),
                locationRepository.findAll(Sort.by("name", "code", "id")).stream()
                        .map(location -> new LocationSummaryDto(location.getId(), location.getCode(), location.getName())).toList());
    }
}
