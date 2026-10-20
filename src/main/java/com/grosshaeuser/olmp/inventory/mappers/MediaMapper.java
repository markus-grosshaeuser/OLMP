package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.dto.media.MediaAuthorDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaCreateDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaDetailDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaEditDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaReferenceDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaSummaryDto;
import com.grosshaeuser.olmp.inventory.entity.Media;
import com.grosshaeuser.olmp.inventory.entity.MediaAuthor;
import com.grosshaeuser.olmp.inventory.entity.MediaIdentifier;
import com.grosshaeuser.olmp.inventory.entity.MediaType;
import com.grosshaeuser.olmp.inventory.entity.Publisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MediaMapper {

    private static final Comparator<MediaAuthor> AUTHOR_ORDER = Comparator
            .comparing(MediaAuthor::getSortOrder)
            .thenComparing(assignment -> assignment.getAuthor().getId())
            .thenComparing(assignment -> assignment.getId().getRole());

    private static final Comparator<MediaIdentifier> IDENTIFIER_ORDER = Comparator
            .comparing(MediaIdentifier::getIdentifierType)
            .thenComparing(MediaIdentifier::getIdentifierValue);

    private final MediaTypeMapper mediaTypeMapper;
    private final PublisherMapper publisherMapper;
    private final MediaAuthorMapper mediaAuthorMapper;
    private final MediaIdentifierMapper mediaIdentifierMapper;

    public MediaReferenceDto toReference(Media source) {
        return source == null ? null : new MediaReferenceDto(source.getId(), source.getTitle());
    }

    public MediaSummaryDto toSummary(Media source, Collection<MediaAuthor> authors) {
        return source == null ? null : new MediaSummaryDto(
                source.getId(), source.getTitle(), mediaTypeMapper.toSummary(source.getMediaType()),
                publisherMapper.toSummary(source.getPublisher()), source.getPublicationYear(),
                authorDtos(source, authors));
    }

    public MediaDetailDto toDetail(Media source, Collection<MediaAuthor> authors,
                                   Collection<MediaIdentifier> identifiers) {
        if (source == null) {
            return null;
        }
        var authorDtos = authorDtos(source, authors);
        var identifierDtos = orderedIdentifiers(source, identifiers).stream()
                .map(mediaIdentifierMapper::toDto).toList();
        return new MediaDetailDto(
                source.getId(), mediaTypeMapper.toSummary(source.getMediaType()),
                publisherMapper.toSummary(source.getPublisher()), source.getTitle(), source.getSubtitle(),
                source.getEdition(), source.getPublicationYear(), source.getLanguageCode(), source.getCoverImageFileName(),
                source.getDescription(), authorDtos, identifierDtos, source.getCreatedAt(), source.getUpdatedAt());
    }

    public MediaEditDto toEditDto(Media source, Collection<MediaAuthor> authors,
                                  Collection<MediaIdentifier> identifiers) {
        if (source == null) {
            return null;
        }
        MediaEditDto dto = new MediaEditDto();
        dto.setId(source.getId());
        dto.setMediaTypeId(source.getMediaType().getId());
        dto.setPublisherId(source.getPublisher() == null ? null : source.getPublisher().getId());
        dto.setTitle(source.getTitle());
        dto.setSubtitle(source.getSubtitle());
        dto.setEdition(source.getEdition());
        dto.setPublicationYear(source.getPublicationYear());
        dto.setLanguageCode(source.getLanguageCode());
        dto.setCoverImageFileName(source.getCoverImageFileName());
        dto.setDescription(source.getDescription());
        dto.setAuthors(new ArrayList<>(orderedAuthors(source, authors).stream()
                .map(mediaAuthorMapper::toWriteDto).toList()));
        dto.setIdentifiers(new ArrayList<>(orderedIdentifiers(source, identifiers).stream()
                .map(mediaIdentifierMapper::toWriteDto).toList()));
        return dto;
    }

    /**
     * Maps the media row only. Map and persist the DTO's author assignments and
     * identifiers separately with MediaAuthorMapper and MediaIdentifierMapper.
     */
    public Media toEntity(MediaCreateDto source, MediaType mediaType, Publisher publisher) {
        Objects.requireNonNull(source, "source");
        checkReferences(source, mediaType, publisher);
        Media target = new Media();
        copyWritableFields(source, target, mediaType, publisher);
        return target;
    }

    /**
     * Updates the media row in place. The service must also reconcile the complete
     * author and identifier collections; they are not associations on Media.
     */
    public void update(MediaEditDto source, Media target, MediaType mediaType, Publisher publisher) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        MappingChecks.sameId(source.getId(), target.getId(), "id");
        checkReferences(source, mediaType, publisher);
        copyWritableFields(source, target, mediaType, publisher);
    }

    private void checkReferences(MediaCreateDto source, MediaType mediaType, Publisher publisher) {
        Objects.requireNonNull(mediaType, "mediaType");
        MappingChecks.sameId(source.getMediaTypeId(), mediaType.getId(), "mediaTypeId");
        MappingChecks.optionalReference(source.getPublisherId(), publisher, Publisher::getId, "publisherId");
    }

    private void copyWritableFields(MediaCreateDto source, Media target, MediaType mediaType, Publisher publisher) {
        target.setMediaType(mediaType);
        target.setPublisher(publisher);
        target.setTitle(source.getTitle());
        target.setSubtitle(source.getSubtitle());
        target.setEdition(source.getEdition());
        target.setPublicationYear(source.getPublicationYear());
        target.setLanguageCode(source.getLanguageCode());
        target.setCoverImageFileName(source.getCoverImageFileName());
        target.setDescription(source.getDescription());
    }

    private List<MediaAuthorDto> authorDtos(Media media, Collection<MediaAuthor> authors) {
        return orderedAuthors(media, authors).stream().map(mediaAuthorMapper::toDto).toList();
    }

    private List<MediaAuthor> orderedAuthors(Media media, Collection<MediaAuthor> authors) {
        Objects.requireNonNull(authors, "authors; use an empty collection when there are none");
        authors.forEach(author -> MappingChecks.sameMedia(media, Objects.requireNonNull(author).getMedia()));
        return authors.stream().sorted(AUTHOR_ORDER).toList();
    }

    private List<MediaIdentifier> orderedIdentifiers(Media media, Collection<MediaIdentifier> identifiers) {
        Objects.requireNonNull(identifiers, "identifiers; use an empty collection when there are none");
        identifiers.forEach(identifier ->
                MappingChecks.sameMedia(media, Objects.requireNonNull(identifier).getMedia()));
        return identifiers.stream().sorted(IDENTIFIER_ORDER).toList();
    }
}
