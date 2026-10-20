package com.grosshaeuser.olmp.inventory.service;

import com.grosshaeuser.olmp.inventory.dto.media.MediaCreateDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaEditDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaSummaryDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MediaManagementService {

    List<MediaSummaryDto> findMedia(String query, Pageable pageable);

    long countMedia(String query);

    Optional<MediaEditDto> findMediaForEdit(long id);

    MediaSummaryDto createMedia(MediaCreateDto media);

    MediaEditDto updateMedia(MediaEditDto media);

    void deleteMedium(Long id);
}
