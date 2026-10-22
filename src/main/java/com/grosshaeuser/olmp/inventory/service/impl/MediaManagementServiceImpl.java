package com.grosshaeuser.olmp.inventory.service.impl;

import com.grosshaeuser.olmp.inventory.dto.media.*;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCondition;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.entity.Author;
import com.grosshaeuser.olmp.inventory.entity.InventoryItem;
import com.grosshaeuser.olmp.inventory.entity.Location;
import com.grosshaeuser.olmp.inventory.entity.Media;
import com.grosshaeuser.olmp.inventory.entity.MediaAuthor;
import com.grosshaeuser.olmp.inventory.entity.MediaIdentifier;
import com.grosshaeuser.olmp.inventory.entity.MediaType;
import com.grosshaeuser.olmp.inventory.entity.Publisher;
import com.grosshaeuser.olmp.inventory.mappers.MediaAuthorMapper;
import com.grosshaeuser.olmp.inventory.mappers.MediaIdentifierMapper;
import com.grosshaeuser.olmp.inventory.mappers.MediaMapper;
import com.grosshaeuser.olmp.inventory.repository.*;
import com.grosshaeuser.olmp.inventory.service.MediaManagementService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.ADMIN;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.ROOT_ADMIN;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.STAFF;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('" + ROOT_ADMIN + "', '" + ADMIN + "', '" + STAFF + "')")
public class MediaManagementServiceImpl implements MediaManagementService {

    private final MediaRepository mediaRepository;
    private final MediaTypeRepository mediaTypeRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final MediaAuthorRepository mediaAuthorRepository;
    private final MediaIdentifierRepository identifierRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final LocationRepository locationRepository;
    private final MediaMapper mediaMapper;
    private final MediaAuthorMapper mediaAuthorMapper;
    private final MediaIdentifierMapper identifierMapper;
    private final Validator validator;

    @Override
    public Optional<MediaEditDto> findMediaWithInventoryForEdit(long id) {
        return findMediaForEdit(id).map(this::includeInventory);
    }

    @Override
    @Transactional
    public MediaSummaryDto createMediaWithInventory(MediaCreateDto dto) {
        validateSubmission(dto, true);
        validateInventorySelection(dto.getInventoryItems(), List.of());
        Map<Long, Location> locations = resolveInventoryLocations(dto.getInventoryItems());
        MediaSummaryDto created = createMedia(dto);
        Media media = mediaRepository.getReferenceById(created.id());
        saveNewInventoryItems(dto.getInventoryItems(), media, locations);
        inventoryItemRepository.flush();
        return created;
    }

    @Override
    @Transactional
    public MediaEditDto updateMediaWithInventory(MediaEditDto dto) {
        validateSubmission(dto, false);
        Media media = mediaRepository.findForUpdate(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Media no longer exists: " + dto.getId()));
        List<InventoryItem> existing = inventoryItemRepository.findAllByMediaIdOrderByIdAsc(media.getId());
        Set<Long> retained = validateInventorySelection(dto.getInventoryItems(), existing);
        Map<Long, Location> locations = resolveInventoryLocations(dto.getInventoryItems());
        Map<Long, InventoryItem> byId = existing.stream().collect(Collectors.toMap(InventoryItem::getId, item -> item));
        dto.getInventoryItems().stream().filter(item -> item.id() != null).forEach(item -> {
            InventoryItem current = byId.get(item.id());
            current.setCondition(InventoryItem.Condition.valueOf(item.condition().name()));
            current.setLocation(item.locationId() == null ? null : locations.get(item.locationId()));
            current.setNotes(item.notes());
        });
        MediaEditDto updated = updateMedia(dto);
        inventoryItemRepository.deleteAll(existing.stream().filter(item -> !retained.contains(item.getId())).toList());
        saveNewInventoryItems(dto.getInventoryItems(), media, locations);
        inventoryItemRepository.flush();
        return includeInventory(updated);
    }

    private MediaEditDto includeInventory(MediaEditDto dto) {
        dto.setInventoryItems(inventoryItemRepository.findAllByMediaIdOrderByIdAsc(dto.getId()).stream()
                .map(item -> new MediaInventoryItemDto(item.getId(), item.getInventoryNumber(), item.getBarcode(),
                        InventoryItemCondition.valueOf(item.getCondition().name()),
                        item.getLocation() == null ? null : item.getLocation().getId(),
                        item.getAcquiredAt(), item.getAcquisitionCost(), item.getWithdrawnAt(), item.getNotes()))
                .toList());
        return dto;
    }

    private Set<Long> validateInventorySelection(List<MediaInventoryItemDto> submitted, List<InventoryItem> existing) {
        Set<Long> available = existing.stream().map(InventoryItem::getId).collect(Collectors.toSet());
        Set<Long> retained = new HashSet<>();
        Set<String> newInventoryNumbers = new HashSet<>();
        for (var item : submitted) {
            if (item.id() != null) {
                if (!available.contains(item.id())) {
                    throw new IllegalArgumentException("A copy no longer exists or belongs to another media record");
                }
                if (!retained.add(item.id())) {
                    throw new IllegalArgumentException("A copy cannot be submitted more than once");
                }
            } else {
                if (item.inventoryNumber() == null || !item.inventoryNumber().equals(item.barcode())) {
                    throw new IllegalArgumentException("New copies require matching UUID inventory numbers and barcodes");
                }
                if (!newInventoryNumbers.add(item.inventoryNumber())) {
                    throw new IllegalArgumentException("Each new copy must have a unique inventory number");
                }
            }
        }
        return retained;
    }

    private Map<Long, Location> resolveInventoryLocations(List<MediaInventoryItemDto> submitted) {
        Set<Long> ids = submitted.stream().map(MediaInventoryItemDto::locationId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Location> locations = ids.isEmpty() ? Map.of() : locationRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Location::getId, location -> location));
        if (locations.size() != ids.size()) {
            throw new IllegalArgumentException("One or more selected locations no longer exist");
        }
        return locations;
    }

    private void saveNewInventoryItems(List<MediaInventoryItemDto> submitted, Media media, Map<Long, Location> locations) {
        inventoryItemRepository.saveAll(submitted.stream().filter(item -> item.id() == null)
                .map(item -> InventoryItem.builder().media(media)
                        .inventoryNumber(item.inventoryNumber()).barcode(item.barcode())
                        .condition(InventoryItem.Condition.valueOf(item.condition().name()))
                        .location(item.locationId() == null ? null : locations.get(item.locationId())).acquiredAt(item.acquiredAt())
                        .acquisitionCost(item.acquisitionCost()).withdrawnAt(item.withdrawnAt()).notes(item.notes()).build())
                .toList());
    }

    @Override
    public List<MediaSummaryDto> findMedia(String query, Pageable pageable) {
        return findMedia(query, null, null, pageable);
    }

    @Override
    public List<MediaSummaryDto> findMedia(String query, Long authorId, Long publisherId, Pageable pageable) {
        List<MediaRepository.SummaryRow> rows = mediaRepository.search(
                searchPattern(query), authorId, publisherId, sortedPageable(pageable));
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, List<MediaAuthorDto>> authors = mediaAuthorRepository
                .findAllWithAuthorByMediaIds(rows.stream().map(MediaRepository.SummaryRow::getId).toList())
                .stream().collect(Collectors.groupingBy(assignment -> assignment.getMedia().getId(),
                        Collectors.mapping(mediaAuthorMapper::toDto, Collectors.toList())));
        return rows.stream().map(row -> new MediaSummaryDto(
                row.getId(), row.getTitle(),
                new MediaTypeSummaryDto(row.getMediaTypeId(), row.getMediaTypeCode(), row.getMediaTypeName()),
                row.getPublisherId() == null ? null : new PublisherSummaryDto(
                        row.getPublisherId(), row.getPublisherName(), row.getPublisherWebsite()),
                row.getPublicationYear(), authors.getOrDefault(row.getId(), List.of()))).toList();
    }

    @Override
    public long countMedia(String query) {
        return countMedia(query, null, null);
    }

    @Override
    public long countMedia(String query, Long authorId, Long publisherId) {
        return mediaRepository.countSearch(searchPattern(query), authorId, publisherId);
    }

    @Override
    public Optional<MediaEditDto> findMediaForEdit(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Media ID must be positive");
        }
        return mediaRepository.findDetailedById(id).map(this::toEditDto);
    }

    @Override
    @Transactional
    public MediaSummaryDto createMedia(MediaCreateDto dto) {
        validateSubmission(dto, true);
        ResolvedReferences references = resolveReferences(dto);
        validateIdentifierAvailability(dto, null);
        try {
            Media media = mediaRepository.saveAndFlush(
                    mediaMapper.toEntity(dto, references.mediaType(), references.publisher()));
            List<MediaAuthor> authors = mediaAuthorRepository.saveAll(dto.getAuthors().stream()
                    .map(author -> mediaAuthorMapper.toEntity(author, media, references.authors().get(author.getAuthorId())))
                    .toList());
            identifierRepository.saveAll(dto.getIdentifiers().stream()
                    .map(identifier -> identifierMapper.toEntity(identifier, media)).toList());
            mediaRepository.flush();
            return mediaMapper.toSummary(media, authors);
        } catch (DataIntegrityViolationException exception) {
            throw writeConflict(exception);
        }
    }

    @Override
    @Transactional
    public MediaEditDto updateMedia(MediaEditDto dto) {
        validateSubmission(dto, false);
        Media media = mediaRepository.findForUpdate(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Media no longer exists: " + dto.getId()));
        ResolvedReferences references = resolveReferences(dto);
        List<MediaIdentifier> existingIdentifiers = identifierRepository.findAllByMediaId(media.getId());
        validateIdentifierOwnership(dto, existingIdentifiers);
        validateIdentifierAvailability(dto, media.getId());
        try {
            replaceIdentifiers(dto, media, existingIdentifiers);
            replaceAuthors(dto, media, references.authors());
            mediaMapper.update(dto, media, references.mediaType(), references.publisher());
            media.setUpdatedAt(OffsetDateTime.now());
            mediaRepository.flush();
            return toEditDto(media);
        } catch (DataIntegrityViolationException exception) {
            throw writeConflict(exception);
        }
    }

    @Override
    @Transactional
    public void deleteMedium(Long id) {
        identifierRepository.deleteByMediaId(id);
        mediaAuthorRepository.deleteByMediaId(id);
        inventoryItemRepository.deleteByMediaId(id);
        mediaRepository.deleteById(id);
    }

    private MediaEditDto toEditDto(Media media) {
        return mediaMapper.toEditDto(media,
                mediaAuthorRepository.findAllWithAuthorByMediaIds(List.of(media.getId())),
                identifierRepository.findAllByMediaId(media.getId()));
    }

    private void validateSubmission(MediaCreateDto dto, boolean creating) {
        if (dto == null) {
            throw new IllegalArgumentException("Media data is required");
        }
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .sorted().collect(Collectors.joining("; ")));
        }
        Set<AuthorRole> authorKeys = new HashSet<>();
        for (var author : dto.getAuthors()) {
            if (!authorKeys.add(new AuthorRole(author.getAuthorId(), author.getRole()))) {
                throw new IllegalArgumentException("An author can only be assigned once for the same role");
            }
        }
        Set<IdentifierValue> identifierKeys = new HashSet<>();
        Set<Long> identifierIds = new HashSet<>();
        for (var identifier : dto.getIdentifiers()) {
            if (!identifierKeys.add(identifierValue(identifier))) {
                throw new IllegalArgumentException("Each identifier type and value combination must be unique");
            }
            if (identifier.getId() != null) {
                if (creating) {
                    throw new IllegalArgumentException("New media identifiers must not have IDs");
                }
                if (!identifierIds.add(identifier.getId())) {
                    throw new IllegalArgumentException("An identifier ID cannot be submitted more than once");
                }
            }
        }
    }

    private ResolvedReferences resolveReferences(MediaCreateDto dto) {
        MediaType type = mediaTypeRepository.findById(dto.getMediaTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Media type no longer exists: " + dto.getMediaTypeId()));
        Publisher publisher = dto.getPublisherId() == null ? null : publisherRepository.findById(dto.getPublisherId())
                .orElseThrow(() -> new IllegalArgumentException("Publisher no longer exists: " + dto.getPublisherId()));
        Set<Long> authorIds = dto.getAuthors().stream().map(MediaAuthorWriteDto::getAuthorId).collect(Collectors.toSet());
        Map<Long, Author> authors = authorIds.isEmpty() ? Map.of() : authorRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Author::getId, author -> author));
        if (authors.size() != authorIds.size()) {
            throw new IllegalArgumentException("One or more selected authors no longer exist");
        }
        return new ResolvedReferences(type, publisher, authors);
    }

    private void validateIdentifierOwnership(MediaEditDto dto, List<MediaIdentifier> existing) {
        Set<Long> currentIds = existing.stream().map(MediaIdentifier::getId).collect(Collectors.toSet());
        for (var identifier : dto.getIdentifiers()) {
            if (identifier.getId() != null && !currentIds.contains(identifier.getId())) {
                throw new IllegalArgumentException("An identifier no longer exists or belongs to another media record");
            }
        }
    }

    private void validateIdentifierAvailability(MediaCreateDto dto, Long mediaId) {
        if (dto.getIdentifiers().isEmpty()) {
            return;
        }
        Set<IdentifierValue> desired = dto.getIdentifiers().stream()
                .map(MediaManagementServiceImpl::identifierValue).collect(Collectors.toSet());
        Set<String> types = desired.stream().map(IdentifierValue::type).collect(Collectors.toSet());
        Set<String> values = desired.stream().map(IdentifierValue::value).collect(Collectors.toSet());
        boolean conflict = identifierRepository.findAllByIdentifierTypeInAndIdentifierValueIn(types, values).stream()
                .anyMatch(identifier -> desired.contains(new IdentifierValue(
                        identifier.getIdentifierType(), identifier.getIdentifierValue()))
                        && !Objects.equals(mediaId, identifier.getMedia().getId()));
        if (conflict) {
            throw new IllegalArgumentException("An identifier is already assigned to another media record");
        }
    }

    private void replaceAuthors(MediaEditDto dto, Media media, Map<Long, Author> authors) {
        Map<AuthorRole, MediaAuthor> remaining = new HashMap<>();
        mediaAuthorRepository.findAllWithAuthorByMediaIds(List.of(media.getId())).forEach(assignment ->
                remaining.put(new AuthorRole(assignment.getId().getAuthorId(), assignment.getId().getRole()), assignment));
        List<MediaAuthor> additions = new java.util.ArrayList<>();
        for (var author : dto.getAuthors()) {
            MediaAuthor assignment = remaining.remove(new AuthorRole(author.getAuthorId(), author.getRole()));
            if (assignment == null) {
                additions.add(mediaAuthorMapper.toEntity(author, media, authors.get(author.getAuthorId())));
            } else {
                mediaAuthorMapper.update(author, assignment, media);
            }
        }
        mediaAuthorRepository.deleteAll(remaining.values());
        mediaAuthorRepository.saveAll(additions);
    }

    private void replaceIdentifiers(MediaEditDto dto, Media media, List<MediaIdentifier> existing) {
        Map<Long, MediaIdentifier> byId = existing.stream().collect(Collectors.toMap(MediaIdentifier::getId, row -> row));
        Set<Long> retained = dto.getIdentifiers().stream().map(MediaIdentifierWriteDto::getId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        identifierRepository.deleteAll(existing.stream().filter(row -> !retained.contains(row.getId())).toList());
        for (var identifier : dto.getIdentifiers()) {
            if (identifier.getId() != null) {
                MediaIdentifier current = byId.get(identifier.getId());
                if (!Objects.equals(current.getIdentifierType(), identifier.getIdentifierType())
                        || !Objects.equals(current.getIdentifierValue(), identifier.getIdentifierValue())) {
                    current.setIdentifierValue(UUID.randomUUID().toString());
                }
            }
        }
        identifierRepository.flush();
        for (var identifier : dto.getIdentifiers()) {
            if (identifier.getId() == null) {
                identifierRepository.save(identifierMapper.toEntity(identifier, media));
            } else {
                identifierMapper.update(identifier, byId.get(identifier.getId()), media);
            }
        }
    }

    private static IdentifierValue identifierValue(MediaIdentifierWriteDto dto) {
        return new IdentifierValue(dto.getIdentifierType(), dto.getIdentifierValue());
    }

    private static String searchPattern(String query) {
        String value = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        return "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    private static Pageable sortedPageable(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable");
        List<Sort.Order> orders = pageable.getSort().stream().map(order -> order.withProperty(switch (order.getProperty()) {
            case "title" -> "title";
            case "publicationYear" -> "publicationYear";
            case "mediaType.name" -> "mediaTypeName";
            case "publisher.name" -> "publisherName";
            case "id" -> "id";
            default -> throw new IllegalArgumentException("Unsupported media sort: " + order.getProperty());
        })).collect(Collectors.toCollection(java.util.ArrayList::new));
        if (orders.isEmpty()) {
            orders.add(Sort.Order.asc("title"));
        }
        if (orders.stream().noneMatch(order -> order.getProperty().equals("id"))) {
            orders.add(Sort.Order.asc("id"));
        }
        Sort sort = Sort.by(orders);
        return pageable.isUnpaged() ? Pageable.unpaged(sort)
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private static DataIntegrityViolationException writeConflict(DataIntegrityViolationException cause) {
        return new DataIntegrityViolationException(
                "Media could not be saved. An identifier is already in use or related data has changed.", cause);
    }

    private record ResolvedReferences(MediaType mediaType, Publisher publisher, Map<Long, Author> authors) { }
    private record AuthorRole(Long authorId, String role) { }
    private record IdentifierValue(String type, String value) { }
}
