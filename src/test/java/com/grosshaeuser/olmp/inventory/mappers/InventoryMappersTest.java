package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorEditDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCondition;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCreateDto;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemDetailDto;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemEditDto;
import com.grosshaeuser.olmp.inventory.dto.location.LocationEditDto;
import com.grosshaeuser.olmp.inventory.dto.location.LocationSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.media.*;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherEditDto;
import com.grosshaeuser.olmp.inventory.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class InventoryMappersTest {

    private static final OffsetDateTime CREATED = OffsetDateTime.parse("2026-01-01T12:00:00Z");
    private static final OffsetDateTime UPDATED = CREATED.plusDays(1);

    private final AuthorMapper authorMapper = new AuthorMapper();
    private final PublisherMapper publisherMapper = new PublisherMapper();
    private final MediaTypeMapper mediaTypeMapper = new MediaTypeMapper();
    private final LocationMapper locationMapper = new LocationMapper();
    private final MediaAuthorMapper mediaAuthorMapper = new MediaAuthorMapper(authorMapper);
    private final MediaIdentifierMapper identifierMapper = new MediaIdentifierMapper();
    private final MediaMapper mediaMapper = new MediaMapper(
            mediaTypeMapper, publisherMapper, mediaAuthorMapper, identifierMapper);
    private final InventoryItemMapper itemMapper = new InventoryItemMapper(mediaMapper, locationMapper);

    @Test
    void authorEditRoundTripClearsOptionalNameAndPreservesIdentityAndAudit() {
        Author author = Author.builder().id(1L).firstName("Ada").lastName("Lovelace")
                .createdAt(CREATED).build();

        assertThat(authorMapper.toSummary(author)).isEqualTo(new AuthorSummaryDto(1L, "Ada", "Lovelace"));
        assertThat(authorMapper.toDetail(author).createdAt()).isEqualTo(CREATED);

        AuthorEditDto edit = authorMapper.toEditDto(author);
        edit.setFirstName(null);
        edit.setLastName("Byron");
        authorMapper.update(edit, author);

        assertThat(author.getFirstName()).isNull();
        assertThat(author.getLastName()).isEqualTo("Byron");
        assertThat(author.getId()).isEqualTo(1L);
        assertThat(author.getCreatedAt()).isEqualTo(CREATED);

        Author copy = authorMapper.toEntity(edit);
        assertThat(copy.getId()).isNull();
        assertThat(copy.getCreatedAt()).isNull();
        assertThat(copy.getLastName()).isEqualTo("Byron");
    }

    @Test
    void publisherEditRoundTripPreservesAuditAndClearsWebsite() {
        Publisher publisher = Publisher.builder().id(2L).name("Publisher").website("https://example.org")
                .createdAt(CREATED).build();
        assertThat(publisherMapper.toSummary(publisher).website()).isEqualTo("https://example.org");
        assertThat(publisherMapper.toDetail(publisher).createdAt()).isEqualTo(CREATED);

        PublisherEditDto edit = publisherMapper.toEditDto(publisher);
        edit.setName("New name");
        edit.setWebsite(null);
        publisherMapper.update(edit, publisher);

        assertThat(publisher.getName()).isEqualTo("New name");
        assertThat(publisher.getWebsite()).isNull();
        assertThat(publisher.getId()).isEqualTo(2L);
        assertThat(publisher.getCreatedAt()).isEqualTo(CREATED);
        assertThat(publisherMapper.toEntity(edit).getId()).isNull();
    }

    @Test
    void mediaTypeEditRoundTripIncludesDescriptionAndPreservesAudit() {
        MediaType type = MediaType.builder().id(3L).code("BOOK").name("Book")
                .description("A long description").createdAt(CREATED).build();
        assertThat(mediaTypeMapper.toSummary(type)).isEqualTo(new MediaTypeSummaryDto(3L, "BOOK", "Book"));
        assertThat(mediaTypeMapper.toDetail(type).description()).isEqualTo("A long description");

        MediaTypeEditDto edit = mediaTypeMapper.toEditDto(type);
        edit.setCode("DVD");
        edit.setName("Video");
        edit.setDescription(null);
        mediaTypeMapper.update(edit, type);

        assertThat(type.getCode()).isEqualTo("DVD");
        assertThat(type.getName()).isEqualTo("Video");
        assertThat(type.getDescription()).isNull();
        assertThat(type.getCreatedAt()).isEqualTo(CREATED);
        assertThat(mediaTypeMapper.toEntity(edit).getId()).isNull();
    }

    @Test
    void locationMappingBoundsHierarchyAndCanClearParent() {
        Location parent = Location.builder().id(1L).code("ROOM").name("Room").build();
        Location location = Location.builder().id(2L).code("SHELF").name("Shelf")
                .description("Description").parent(parent).createdAt(CREATED).build();
        parent.setParent(location);

        assertThat(locationMapper.toDetail(location).parent())
                .isEqualTo(new LocationSummaryDto(1L, "ROOM", "Room"));

        LocationEditDto edit = locationMapper.toEditDto(location);
        assertThat(edit.getParentId()).isEqualTo(1L);
        assertThat(locationMapper.toEntity(edit, parent).getParent()).isSameAs(parent);
        edit.setParentId(null);
        edit.setDescription(null);
        locationMapper.update(edit, location, null);

        assertThat(location.getParent()).isNull();
        assertThat(location.getDescription()).isNull();
        assertThat(location.getCreatedAt()).isEqualTo(CREATED);
    }

    @Test
    void rejectsSelfParentingAndUnresolvedOptionalReferencesBeforeMutation() {
        Location location = Location.builder().id(1L).code("OLD").name("Old").build();
        LocationEditDto edit = locationMapper.toEditDto(location);
        edit.setCode("NEW");
        edit.setParentId(1L);

        assertThatIllegalArgumentException().isThrownBy(() -> locationMapper.update(edit, location, location));
        assertThat(location.getCode()).isEqualTo("OLD");

        edit.setParentId(null);
        assertThatIllegalArgumentException().isThrownBy(() -> locationMapper.update(edit, location, new Location()));

        edit.setParentId(2L);
        assertThatIllegalArgumentException().isThrownBy(() -> locationMapper.update(edit, location, null));
    }

    @Test
    void rejectsMismatchedTargetIdBeforeChangingWritableFields() {
        Author author = Author.builder().id(1L).lastName("Original").build();
        AuthorEditDto edit = authorMapper.toEditDto(author);
        edit.setId(2L);
        edit.setLastName("Changed");

        assertThatIllegalArgumentException().isThrownBy(() -> authorMapper.update(edit, author));
        assertThat(author.getLastName()).isEqualTo("Original");
        assertThat(author.getId()).isEqualTo(1L);
    }

    @Test
    void mediaReadMappingOrdersChildrenAndCreatesIndependentEditableCollections() {
        Media media = media();
        MediaAuthor second = assignment(media, 2L, "AUTHOR", (short) 1);
        MediaAuthor translator = assignment(media, 1L, "TRANSLATOR", (short) 0);
        MediaAuthor first = assignment(media, 1L, "AUTHOR", (short) 0);
        List<MediaAuthor> authors = new ArrayList<>(List.of(second, translator, first));
        MediaIdentifier isbn = identifier(media, 11L, "ISBN", "978-3-16-148410-0");
        MediaIdentifier doi = identifier(media, 12L, "DOI", "10.1000/182");
        List<MediaIdentifier> identifiers = new ArrayList<>(List.of(isbn, doi));

        MediaDetailDto detail = mediaMapper.toDetail(media, authors, identifiers);
        assertThat(detail.authors()).extracting(MediaAuthorDto::role)
                .containsExactly("AUTHOR", "TRANSLATOR", "AUTHOR");
        assertThat(detail.identifiers()).extracting(MediaIdentifierDto::identifierType)
                .containsExactly("DOI", "ISBN");
        assertThat(detail.description()).isEqualTo("Long description");
        assertThat(detail.createdAt()).isEqualTo(CREATED);
        assertThat(detail.updatedAt()).isEqualTo(UPDATED);
        assertThat(mediaMapper.toSummary(media, authors).authors()).isEqualTo(detail.authors());
        assertThat(authors).containsExactly(second, translator, first);

        MediaEditDto edit = mediaMapper.toEditDto(media, authors, identifiers);
        assertThat(edit.getIdentifiers()).extracting(MediaIdentifierWriteDto::getId).containsExactly(12L, 11L);
        edit.getAuthors().getFirst().setRole("EDITOR");
        edit.getIdentifiers().getFirst().setIdentifierValue("changed");
        edit.getAuthors().clear();
        edit.getIdentifiers().clear();

        assertThat(first.getId().getRole()).isEqualTo("AUTHOR");
        assertThat(doi.getIdentifierValue()).isEqualTo("10.1000/182");
        assertThat(authors).hasSize(3);
        assertThat(identifiers).hasSize(2);
        assertThat(detail.authors()).hasSize(3);
    }

    @Test
    void mediaRowMappingCopiesAllWritableFieldsAndKeepsAuditAndIdentity() {
        Media media = media();
        Publisher publisher = Publisher.builder().id(2L).name("Publisher").build();
        media.setPublisher(publisher);
        MediaEditDto edit = mediaMapper.toEditDto(media, List.of(), List.of());
        assertThat(edit.getCoverImageFileName()).isEqualTo("cover.png");
        Media created = mediaMapper.toEntity(edit, media.getMediaType(), publisher);
        assertThat(created).usingRecursiveComparison().ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(media);
        assertThat(created.getId()).isNull();
        assertThat(created.getCreatedAt()).isNull();
        assertThat(created.getUpdatedAt()).isNull();

        edit.setPublisherId(null);
        edit.setSubtitle(null);
        edit.setEdition(null);
        edit.setPublicationYear(null);
        edit.setLanguageCode(null);
        edit.setDescription(null);
        mediaMapper.update(edit, media, media.getMediaType(), null);

        assertThat(media.getId()).isEqualTo(10L);
        assertThat(media.getCreatedAt()).isEqualTo(CREATED);
        assertThat(media.getUpdatedAt()).isEqualTo(UPDATED);
        assertThat(media.getPublisher()).isNull();
        assertThat(media.getSubtitle()).isNull();
        assertThat(media.getEdition()).isNull();
        assertThat(media.getPublicationYear()).isNull();
        assertThat(media.getLanguageCode()).isNull();
        assertThat(media.getDescription()).isNull();
    }

    @Test
    void mediaReadMappingRequiresExplicitCollectionsAndRejectsForeignChildren() {
        Media media = media();
        Media foreign = Media.builder().id(99L).build();
        assertThatIllegalArgumentException().isThrownBy(() -> mediaMapper.toSummary(media,
                List.of(assignment(foreign, 1L, "AUTHOR", (short) 0))));
        assertThatIllegalArgumentException().isThrownBy(() -> mediaMapper.toDetail(media, List.of(),
                List.of(identifier(foreign, 1L, "ISBN", "value"))));
        assertThatNullPointerException().isThrownBy(() -> mediaMapper.toSummary(media, null));
        assertThat(mediaMapper.toDetail(media, List.of(), List.of()).authors()).isEmpty();
    }

    @Test
    void rejectsMismatchedResolvedMediaTypeBeforeMutatingMedia() {
        Media media = media();
        MediaEditDto edit = mediaMapper.toEditDto(media, List.of(), List.of());
        edit.setTitle("Changed");
        MediaType wrongType = MediaType.builder().id(99L).build();

        assertThatIllegalArgumentException().isThrownBy(() -> mediaMapper.update(edit, media, wrongType, null));
        assertThat(media.getTitle()).isEqualTo("Title");
    }

    @Test
    void identifierEditRetainsIdAndOwnerWhileNewIdentifierGetsNoId() {
        Media media = media();
        MediaIdentifier identifier = identifier(media, 3L, "ISBN", "original");
        assertThat(identifierMapper.toDto(identifier)).isEqualTo(new MediaIdentifierDto(3L, "ISBN", "original"));
        MediaIdentifierWriteDto edit = identifierMapper.toWriteDto(identifier);
        edit.setIdentifierType("DOI");
        edit.setIdentifierValue("10.1000/182");
        identifierMapper.update(edit, identifier, media);

        assertThat(identifier.getId()).isEqualTo(3L);
        assertThat(identifier.getMedia()).isSameAs(media);
        assertThat(identifier.getIdentifierType()).isEqualTo("DOI");
        assertThat(identifier.getIdentifierValue()).isEqualTo("10.1000/182");
        assertThatIllegalArgumentException().isThrownBy(() -> identifierMapper.toEntity(edit, media));

        edit.setId(null);
        MediaIdentifier created = identifierMapper.toEntity(edit, media);
        assertThat(created.getId()).isNull();
        assertThat(created.getMedia()).isSameAs(media);
        assertThat(created.getIdentifierValue()).isEqualTo("10.1000/182");
    }

    @Test
    void childUpdatesRejectForeignOwnersAndMismatchedIdentifierIds() {
        Media media = media();
        Media otherMedia = Media.builder().id(99L).build();
        MediaIdentifier identifier = identifier(media, 3L, "ISBN", "original");
        MediaIdentifierWriteDto edit = identifierMapper.toWriteDto(identifier);
        edit.setIdentifierValue("changed");
        assertThatIllegalArgumentException().isThrownBy(() -> identifierMapper.update(edit, identifier, otherMedia));
        edit.setId(4L);
        assertThatIllegalArgumentException().isThrownBy(() -> identifierMapper.update(edit, identifier, media));
        assertThat(identifier.getIdentifierValue()).isEqualTo("original");

        MediaAuthor assignment = assignment(media, 1L, "AUTHOR", (short) 0);
        MediaAuthorWriteDto authorEdit = mediaAuthorMapper.toWriteDto(assignment);
        authorEdit.setSortOrder((short) 1);
        assertThatIllegalArgumentException().isThrownBy(() -> mediaAuthorMapper.update(authorEdit, assignment, otherMedia));
        assertThat(assignment.getSortOrder()).isZero();
    }

    @Test
    void authorAssignmentMappingKeepsCompositeKeyInSyncAndNeverMutatesItOnUpdate() {
        Media media = media();
        Author author = Author.builder().id(2L).lastName("Author").build();
        MediaAuthorWriteDto form = new MediaAuthorWriteDto();
        form.setAuthorId(2L);
        MediaAuthor assignment = mediaAuthorMapper.toEntity(form, media, author);

        assertThat(assignment.getId()).isEqualTo(new MediaAuthorId(10L, 2L, "AUTHOR"));
        assertThat(assignment.getMedia()).isSameAs(media);
        assertThat(assignment.getAuthor()).isSameAs(author);
        form.setSortOrder((short) 3);
        mediaAuthorMapper.update(form, assignment, media);
        assertThat(assignment.getSortOrder()).isEqualTo((short) 3);

        form.setRole("EDITOR");
        assertThatIllegalArgumentException().isThrownBy(() -> mediaAuthorMapper.update(form, assignment, media));
        assertThat(assignment.getId().getRole()).isEqualTo("AUTHOR");
        form.setAuthorId(99L);
        assertThatIllegalArgumentException().isThrownBy(() -> mediaAuthorMapper.toEntity(form, media, author));
    }

    @ParameterizedTest
    @EnumSource(InventoryItemCondition.class)
    void itemMappingRoundTripsEveryConditionAndAllWritableFields(InventoryItemCondition condition) {
        Media media = media();
        Location location = Location.builder().id(5L).code("ROOM").name("Room").build();
        InventoryItemCreateDto form = new InventoryItemCreateDto();
        form.setMediaId(media.getId());
        form.setInventoryNumber("COPY-1");
        form.setBarcode("123456");
        form.setCondition(condition);
        form.setLocationId(location.getId());
        form.setAcquiredAt(LocalDate.of(2026, 1, 1));
        form.setAcquisitionCost(new BigDecimal("12.50"));
        form.setWithdrawnAt(LocalDate.of(2026, 10, 17));
        form.setNotes("Copy notes");
        InventoryItem item = itemMapper.toEntity(form, media, location);
        assertThat(item.getId()).isNull();
        assertThat(item.getCreatedAt()).isNull();
        item.setId(7L);
        item.setCreatedAt(CREATED);
        item.setUpdatedAt(UPDATED);

        InventoryItemEditDto edit = itemMapper.toEditDto(item);
        assertThat(edit).usingRecursiveComparison().ignoringFields("id").isEqualTo(form);
        InventoryItemDetailDto detail = itemMapper.toDetail(item);
        assertThat(detail.media()).isEqualTo(new MediaReferenceDto(10L, "Title"));
        assertThat(detail.condition()).isEqualTo(condition);
        assertThat(detail.acquisitionCost()).isEqualByComparingTo("12.50");
        assertThat(detail.notes()).isEqualTo("Copy notes");
        assertThat(itemMapper.toSummary(item).condition()).isEqualTo(condition);
        assertThat(itemMapper.toSummary(item).mediaTitle()).isEqualTo("Title");

        edit.setLocationId(null);
        edit.setBarcode(null);
        edit.setAcquiredAt(null);
        edit.setAcquisitionCost(null);
        edit.setWithdrawnAt(null);
        edit.setNotes(null);
        itemMapper.update(edit, item, media, null);
        assertThat(item.getId()).isEqualTo(7L);
        assertThat(item.getCreatedAt()).isEqualTo(CREATED);
        assertThat(item.getUpdatedAt()).isEqualTo(UPDATED);
        assertThat(item.getLocation()).isNull();
        assertThat(item.getBarcode()).isNull();
        assertThat(item.getAcquiredAt()).isNull();
        assertThat(item.getAcquisitionCost()).isNull();
        assertThat(item.getWithdrawnAt()).isNull();
        assertThat(item.getNotes()).isNull();
    }

    @Test
    void itemMappingRejectsMismatchedMediaAndLocationReferences() {
        InventoryItemCreateDto form = new InventoryItemCreateDto();
        form.setMediaId(10L);
        form.setInventoryNumber("COPY-1");
        assertThatIllegalArgumentException().isThrownBy(() -> itemMapper.toEntity(form,
                Media.builder().id(99L).build(), null));
        form.setLocationId(1L);
        assertThatIllegalArgumentException().isThrownBy(() -> itemMapper.toEntity(form, media(), null));
    }

    @Test
    void readMappersHandleNullEntities() {
        assertThat(authorMapper.toSummary(null)).isNull();
        assertThat(authorMapper.toDetail(null)).isNull();
        assertThat(authorMapper.toEditDto(null)).isNull();
        assertThat(publisherMapper.toSummary(null)).isNull();
        assertThat(publisherMapper.toDetail(null)).isNull();
        assertThat(publisherMapper.toEditDto(null)).isNull();
        assertThat(mediaTypeMapper.toSummary(null)).isNull();
        assertThat(mediaTypeMapper.toDetail(null)).isNull();
        assertThat(mediaTypeMapper.toEditDto(null)).isNull();
        assertThat(locationMapper.toSummary(null)).isNull();
        assertThat(locationMapper.toDetail(null)).isNull();
        assertThat(locationMapper.toEditDto(null)).isNull();
        assertThat(mediaMapper.toReference(null)).isNull();
        assertThat(mediaMapper.toSummary(null, List.of())).isNull();
        assertThat(mediaMapper.toDetail(null, List.of(), List.of())).isNull();
        assertThat(mediaMapper.toEditDto(null, List.of(), List.of())).isNull();
        assertThat(mediaAuthorMapper.toDto(null)).isNull();
        assertThat(mediaAuthorMapper.toWriteDto(null)).isNull();
        assertThat(identifierMapper.toDto(null)).isNull();
        assertThat(identifierMapper.toWriteDto(null)).isNull();
        assertThat(itemMapper.toSummary(null)).isNull();
        assertThat(itemMapper.toDetail(null)).isNull();
        assertThat(itemMapper.toEditDto(null)).isNull();
    }

    private Media media() {
        return Media.builder().id(10L).mediaType(MediaType.builder().id(1L).code("BOOK").name("Book").build())
                .title("Title").subtitle("Subtitle").edition("Second").publicationYear((short) 2026)
                .languageCode("en").coverImageFileName("cover.png").description("Long description")
                .createdAt(CREATED).updatedAt(UPDATED).build();
    }

    private MediaAuthor assignment(Media media, Long authorId, String role, Short sortOrder) {
        return MediaAuthor.builder().id(new MediaAuthorId(media.getId(), authorId, role)).media(media)
                .author(Author.builder().id(authorId).lastName("Author " + authorId).build())
                .sortOrder(sortOrder).build();
    }

    private MediaIdentifier identifier(Media media, Long id, String type, String value) {
        return MediaIdentifier.builder().id(id).media(media).identifierType(type).identifierValue(value).build();
    }
}
