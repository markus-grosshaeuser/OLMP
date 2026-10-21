package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorCreateDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherCreateDto;
import com.grosshaeuser.olmp.inventory.dto.media.*;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.entity.Author;
import com.grosshaeuser.olmp.inventory.entity.MediaType;
import com.grosshaeuser.olmp.inventory.entity.Publisher;
import com.grosshaeuser.olmp.inventory.mappers.*;
import com.grosshaeuser.olmp.inventory.repository.AuthorRepository;
import com.grosshaeuser.olmp.inventory.repository.MediaRepository;
import com.grosshaeuser.olmp.inventory.repository.MediaTypeRepository;
import com.grosshaeuser.olmp.inventory.repository.PublisherRepository;
import com.grosshaeuser.olmp.inventory.service.MediaLookupService;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import com.grosshaeuser.olmp.inventory.service.impl.MediaReferenceServiceImpl;
import com.grosshaeuser.olmp.inventory.service.impl.MediaLookupServiceImpl;
import com.grosshaeuser.olmp.inventory.service.MediaManagementService;
import com.grosshaeuser.olmp.inventory.service.impl.MediaManagementServiceImpl;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(MediaServicesIntegrationTest.Config.class)
@Testcontainers
@WithMockUser(roles = "STAFF")
class MediaServicesIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    MediaManagementService service;
    @Autowired
    MediaLookupService lookupService;
    @Autowired
    MediaReferenceService referenceService;
    @Autowired
    MediaTypeRepository types;
    @Autowired
    PublisherRepository publishers;
    @Autowired
    AuthorRepository authors;
    @Autowired
    MediaRepository media;
    @Autowired JdbcTemplate jdbc;
    @Autowired SqlCapture sql;

    private MediaType book;
    private Author author;

    @BeforeEach
    void seedReferences() {
        jdbc.execute("truncate media_identifiers, media_authors, media, authors, publishers, media_types restart identity cascade");
        book = types.saveAndFlush(MediaType.builder().code("BOOK").name("Book").build());
        author = authors.saveAndFlush(Author.builder().firstName("Ada").lastName("Lovelace").build());
        sql.statements.clear();
    }

    @Test
    void createAndReadRoundTripIncludesAllFieldsAndChildIds() {
        MediaCreateDto form = form("A title");
        Publisher publisher = publishers.saveAndFlush(Publisher.builder().name("Publisher").website("https://example.org").build());
        form.setPublisherId(publisher.getId());
        form.setSubtitle("Subtitle");
        form.setEdition("Second");
        form.setPublicationYear((short) 2026);
        form.setLanguageCode("en");
        form.setCoverImageFileName("cover.png");
        form.setDescription("Full description");
        form.getAuthors().add(assignment("TRANSLATOR", (short) 1));
        form.getAuthors().add(assignment("AUTHOR", (short) 0));
        form.getIdentifiers().add(identifier("ISBN", "first"));

        MediaSummaryDto summary = service.createMedia(form);
        assertThat(summary.id()).isPositive();
        assertThat(summary.publisher().name()).isEqualTo("Publisher");
        assertThat(summary.authors()).extracting(MediaAuthorDto::role).containsExactly("AUTHOR", "TRANSLATOR");
        MediaEditDto edit = edit(summary.id());
        assertThat(edit).usingRecursiveComparison().ignoringFields("id", "identifiers")
                .ignoringCollectionOrderInFields("authors").isEqualTo(form);
        assertThat(edit.getIdentifiers().getFirst().getId()).isPositive();
        assertThat(edit.getIdentifiers().getFirst().getIdentifierValue()).isEqualTo("first");
        assertThat(jdbc.queryForObject("select created_at is not null and updated_at is not null from media where id=?",
                Boolean.class, summary.id())).isTrue();
        assertThat(service.findMediaForEdit(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    void searchMatchesTitleOrSubtitleOnceAndEscapesLiteralWildcards() {
        MediaCreateDto first = form("Mixed Case 100%_! title");
        first.setSubtitle("A Mixed Case subtitle");
        service.createMedia(first);
        service.createMedia(form("Other"));

        assertThat(service.findMedia("  MIXED CASE  ", Pageable.unpaged())).hasSize(1);
        assertThat(service.countMedia("mixed case")).isEqualTo(1);
        assertThat(service.findMedia("SUBTITLE", Pageable.unpaged())).hasSize(1);
        for (String literal : List.of("%", "_", "!", "%_!")) {
            assertThat(service.countMedia(literal)).isEqualTo(1);
            assertThat(service.findMedia(literal, Pageable.unpaged())).hasSize(1);
        }
        assertThat(service.countMedia(null)).isEqualTo(2);
        assertThat(service.findMedia("does not exist", Pageable.unpaged())).isEmpty();
    }

    @Test
    void combinesTextAuthorAndPublisherFiltersWithoutDuplicateMediaAndWithMatchingCounts() {
        Publisher firstPublisher = publishers.saveAndFlush(Publisher.builder().name("First publisher").build());
        Publisher secondPublisher = publishers.saveAndFlush(Publisher.builder().name("Second publisher").build());
        MediaCreateDto first = form("A matching title");
        first.setPublisherId(firstPublisher.getId());
        first.getAuthors().add(assignment("AUTHOR", (short) 0));
        first.getAuthors().add(assignment("EDITOR", (short) 1));
        long firstId = service.createMedia(first).id();
        MediaCreateDto second = form("B matching title");
        second.setPublisherId(secondPublisher.getId());
        second.getAuthors().add(assignment("AUTHOR", (short) 0));
        long secondId = service.createMedia(second).id();
        MediaCreateDto noAuthor = form("C matching title");
        noAuthor.setPublisherId(firstPublisher.getId());
        long noAuthorId = service.createMedia(noAuthor).id();
        MediaCreateDto otherTitle = form("D unrelated title");
        otherTitle.setPublisherId(firstPublisher.getId());
        otherTitle.getAuthors().add(assignment("AUTHOR", (short) 0));
        long otherTitleId = service.createMedia(otherTitle).id();
        MediaCreateDto noPublisher = form("E title");
        noPublisher.setSubtitle("Matching subtitle");
        noPublisher.getAuthors().add(assignment("TRANSLATOR", (short) 0));
        long noPublisherId = service.createMedia(noPublisher).id();

        assertThat(service.findMedia(" MATCHING ", author.getId(), firstPublisher.getId(), Pageable.unpaged()))
                .singleElement().satisfies(row -> {
                    assertThat(row.id()).isEqualTo(firstId);
                    assertThat(row.authors()).hasSize(2);
                });
        assertThat(service.countMedia("matching", author.getId(), firstPublisher.getId())).isEqualTo(1);
        assertThat(service.findMedia("matching", null, firstPublisher.getId(), Pageable.unpaged()))
                .extracting(MediaSummaryDto::id).containsExactly(firstId, noAuthorId);
        assertThat(service.countMedia("matching", null, firstPublisher.getId())).isEqualTo(2);
        assertThat(service.findMedia(null, author.getId(), firstPublisher.getId(), Pageable.unpaged()))
                .extracting(MediaSummaryDto::id).containsExactly(firstId, otherTitleId);
        assertThat(service.countMedia(null, author.getId(), firstPublisher.getId())).isEqualTo(2);
        assertThat(service.findMedia("matching", author.getId(), null, Pageable.unpaged()))
                .extracting(MediaSummaryDto::id).containsExactly(firstId, secondId, noPublisherId);
        assertThat(service.countMedia("matching", author.getId(), null)).isEqualTo(3);
        assertThat(service.findMedia("matching", author.getId(), null, PageRequest.of(1, 1)))
                .extracting(MediaSummaryDto::id).containsExactly(secondId);
        assertThat(service.findMedia("matching", null, null, Pageable.unpaged())).hasSize(4);
        assertThat(service.countMedia("matching", null, null)).isEqualTo(4);
        assertThat(service.findMedia("matching", Long.MAX_VALUE, null, Pageable.unpaged())).isEmpty();
        assertThat(service.countMedia("matching", Long.MAX_VALUE, null)).isZero();
        assertThat(service.findMedia("matching", null, Long.MAX_VALUE, Pageable.unpaged())).isEmpty();
        assertThat(service.countMedia("matching", null, Long.MAX_VALUE)).isZero();
    }

    @Test
    void pagingIsStableAndEveryUiSortWorksWithOptionalPublishers() {
        MediaSummaryDto first = service.createMedia(form("Same"));
        MediaSummaryDto second = service.createMedia(form("Same"));
        MediaCreateDto withPublisher = form("Z title");
        Publisher publisher = publishers.saveAndFlush(Publisher.builder().name("A publisher").build());
        withPublisher.setPublisherId(publisher.getId());
        withPublisher.setPublicationYear((short) 2026);
        MediaSummaryDto third = service.createMedia(withPublisher);

        assertThat(service.findMedia("", PageRequest.of(0, 1))).extracting(MediaSummaryDto::id).containsExactly(first.id());
        assertThat(service.findMedia("", PageRequest.of(1, 1))).extracting(MediaSummaryDto::id).containsExactly(second.id());
        for (String property : List.of("title", "publicationYear", "mediaType.name", "publisher.name")) {
            assertThat(service.findMedia("", PageRequest.of(0, 10, Sort.by(property))))
                    .extracting(MediaSummaryDto::id).containsExactlyInAnyOrder(first.id(), second.id(), third.id());
        }
        assertThat(service.findMedia("", PageRequest.of(0, 10, Sort.by("publisher.name"))).getFirst().id())
                .isEqualTo(third.id());
        assertThatIllegalArgumentException().isThrownBy(() -> service.findMedia("",
                PageRequest.of(0, 10, Sort.by("description"))));
    }

    @Test
    void overviewUsesTwoSelectsWithoutLoadingDescriptions() {
        for (int i = 0; i < 3; i++) {
            MediaCreateDto form = form("Title " + i);
            form.setDescription("Long description");
            form.getAuthors().add(assignment("AUTHOR", (short) 0));
            service.createMedia(form);
        }
        sql.statements.clear();
        List<MediaSummaryDto> results = service.findMedia("", PageRequest.of(0, 10));

        assertThat(results).hasSize(3).allSatisfy(row -> assertThat(row.authors()).hasSize(1));
        assertThat(sql.statements).hasSize(2);
        assertThat(sql.statements).noneMatch(statement -> statement.contains("description") || statement.contains("cover_image"));
    }

    @Test
    void updateRetainsIdentifierIdsRemovesChildrenAndReusesDeletedValues() {
        MediaCreateDto form = form("Original");
        form.getAuthors().add(assignment("AUTHOR", (short) 0));
        form.getIdentifiers().add(identifier("ISBN", "retain"));
        form.getIdentifiers().add(identifier("ISBN", "reuse"));
        long id = service.createMedia(form).id();
        var createdAt = jdbc.queryForObject("select created_at from media where id=?", java.time.OffsetDateTime.class, id);
        MediaEditDto edit = edit(id);
        long retainedId = edit.getIdentifiers().stream().filter(value -> value.getIdentifierValue().equals("retain"))
                .findFirst().orElseThrow().getId();
        edit.getIdentifiers().removeIf(value -> value.getIdentifierValue().equals("reuse"));
        edit.getIdentifiers().getFirst().setIdentifierValue("changed");
        edit.getIdentifiers().add(identifier("ISBN", "reuse"));
        edit.getAuthors().clear();
        edit.getAuthors().add(assignment("EDITOR", (short) 2));
        edit.setTitle("Edited");
        edit.setCoverImageFileName("edited.png");

        MediaEditDto saved = service.updateMedia(edit);
        assertThat(saved.getTitle()).isEqualTo("Edited");
        assertThat(saved.getCoverImageFileName()).isEqualTo("edited.png");
        assertThat(saved.getIdentifiers()).filteredOn(value -> value.getId().equals(retainedId))
                .singleElement().extracting(MediaIdentifierWriteDto::getIdentifierValue).isEqualTo("changed");
        assertThat(saved.getIdentifiers()).hasSize(2);
        assertThat(saved.getAuthors()).singleElement().extracting(MediaAuthorWriteDto::getRole).isEqualTo("EDITOR");
        assertThat(jdbc.queryForObject("select created_at from media where id=?", java.time.OffsetDateTime.class, id))
                .isEqualTo(createdAt);
        saved.getAuthors().clear();
        saved.getIdentifiers().clear();
        MediaEditDto cleared = service.updateMedia(saved);
        assertThat(cleared.getAuthors()).isEmpty();
        assertThat(cleared.getIdentifiers()).isEmpty();
    }

    @Test
    void retainedIdentifierValuesCanBeSwappedUnderTheImmediateUniqueConstraint() {
        MediaCreateDto form = form("Swap");
        form.getIdentifiers().add(identifier("ISBN", "one"));
        form.getIdentifiers().add(identifier("ISBN", "two"));
        MediaEditDto edit = edit(service.createMedia(form).id());
        long firstId = edit.getIdentifiers().getFirst().getId();
        long secondId = edit.getIdentifiers().getLast().getId();
        edit.getIdentifiers().getFirst().setIdentifierValue("two");
        edit.getIdentifiers().getLast().setIdentifierValue("one");

        service.updateMedia(edit);
        assertThat(jdbc.queryForObject("select identifier_value from media_identifiers where id=?", String.class, firstId))
                .isEqualTo("two");
        assertThat(jdbc.queryForObject("select identifier_value from media_identifiers where id=?", String.class, secondId))
                .isEqualTo("one");
    }

    @Test
    void retainedAuthorAssignmentCanChangeOrderWithoutReplacingItsKey() {
        MediaCreateDto form = form("Authors");
        form.getAuthors().add(assignment("AUTHOR", (short) 0));
        MediaEditDto edit = edit(service.createMedia(form).id());
        edit.getAuthors().getFirst().setSortOrder((short) 4);
        MediaEditDto updated = service.updateMedia(edit);
        assertThat(updated.getAuthors().getFirst().getSortOrder()).isEqualTo((short) 4);
        assertThat(jdbc.queryForObject("select count(*) from media_authors", Long.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select updated_at > created_at from media where id=?", Boolean.class, edit.getId()))
                .isTrue();
    }

    @Test
    void rejectsForeignIdentifierIdsAndGloballyDuplicatedValuesWithoutChangingData() {
        MediaCreateDto first = form("First");
        first.getIdentifiers().add(identifier("ISBN", "unique"));
        MediaEditDto owner = edit(service.createMedia(first).id());
        MediaEditDto other = edit(service.createMedia(form("Other")).id());
        other.setTitle("Attempted change");
        other.getIdentifiers().add(owner.getIdentifiers().getFirst());
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateMedia(other));
        assertThat(edit(other.getId()).getTitle()).isEqualTo("Other");
        other.getIdentifiers().clear();
        other.getIdentifiers().add(identifier("ISBN", "unique"));
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateMedia(other));
        MediaCreateDto duplicate = form("Duplicate");
        duplicate.getIdentifiers().add(identifier("ISBN", "unique"));
        assertThatIllegalArgumentException().isThrownBy(() -> service.createMedia(duplicate));
        assertThat(service.countMedia("")).isEqualTo(2);
    }

    @Test
    void validatesNestedFieldsDuplicatesAndReferencesBeforeInsertingMedia() {
        MediaCreateDto form = form(" ");
        assertThatIllegalArgumentException().isThrownBy(() -> service.createMedia(form));
        form.setTitle("Valid");
        form.getAuthors().add(new MediaAuthorWriteDto());
        assertThatIllegalArgumentException().isThrownBy(() -> service.createMedia(form));
        form.getAuthors().clear();
        form.getAuthors().add(assignment("AUTHOR", (short) 0));
        form.getAuthors().add(assignment("AUTHOR", (short) 0));
        assertThatIllegalArgumentException().isThrownBy(() -> service.createMedia(form));
        form.getAuthors().clear();
        form.setMediaTypeId(Long.MAX_VALUE);
        assertThatIllegalArgumentException().isThrownBy(() -> service.createMedia(form));
        form.setMediaTypeId(book.getId());
        form.setPublisherId(Long.MAX_VALUE);
        assertThatIllegalArgumentException().isThrownBy(() -> service.createMedia(form));
        form.setPublisherId(null);
        var missingAuthor = assignment("AUTHOR", (short) 0);
        missingAuthor.setAuthorId(Long.MAX_VALUE);
        form.getAuthors().add(missingAuthor);
        assertThatIllegalArgumentException().isThrownBy(() -> service.createMedia(form));
        assertThat(service.countMedia("")).isZero();
    }

    @Test
    void databaseFailureRollsBackRootChildChangesAndTemporaryIdentifierValues() {
        MediaCreateDto form = form("Original");
        form.getAuthors().add(assignment("AUTHOR", (short) 0));
        form.getIdentifiers().add(identifier("ISBN", "original"));
        MediaEditDto edit = edit(service.createMedia(form).id());
        edit.setTitle("FAIL");
        edit.getIdentifiers().getFirst().setIdentifierValue("changed");
        edit.getIdentifiers().add(identifier("DOI", "new"));
        edit.getAuthors().clear();
        jdbc.execute("""
                create function reject_test_edit() returns trigger language plpgsql as $$
                begin
                    if NEW.title = 'FAIL' then
                        raise exception 'test constraint failure' using errcode = '23514';
                    end if;
                    return NEW;
                end $$
                """);
        jdbc.execute("create trigger reject_test_edit before update on media for each row execute function reject_test_edit()");
        try {
            assertThatThrownBy(() -> service.updateMedia(edit)).isInstanceOf(DataIntegrityViolationException.class);
            MediaEditDto after = edit(edit.getId());
            assertThat(after.getTitle()).isEqualTo("Original");
            assertThat(after.getIdentifiers()).hasSize(1);
            assertThat(after.getIdentifiers().getFirst().getIdentifierValue()).isEqualTo("original");
            assertThat(after.getAuthors()).hasSize(1);
        } finally {
            jdbc.execute("drop trigger reject_test_edit on media");
            jdbc.execute("drop function reject_test_edit()");
        }
    }

    @Test
    void lookupsAreSortedAndContainSavedReferences() {
        types.saveAndFlush(MediaType.builder().code("AUDIO").name("Audio").build());
        publishers.saveAndFlush(Publisher.builder().name("Z publisher").build());
        publishers.saveAndFlush(Publisher.builder().name("A publisher").build());
        authors.saveAndFlush(Author.builder().lastName("Adams").build());
        MediaFormOptions options = lookupService.getFormOptions();
        assertThat(options.mediaTypes()).extracting(MediaTypeSummaryDto::name).containsExactly("Audio", "Book");
        assertThat(options.publishers()).extracting(PublisherSummaryDto::name).containsExactly("A publisher", "Z publisher");
        assertThat(options.authors()).extracting(AuthorSummaryDto::lastName).containsExactly("Adams", "Lovelace");
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void membersCannotUseInternalMediaServices() {
        assertThatThrownBy(() -> service.countMedia("")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> lookupService.getFormOptions()).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.createMedia(form("Forbidden"))).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithAnonymousUser
    void anonymousCallersCannotUseInternalMediaServices() {
        assertThatThrownBy(() -> service.findMedia("", Pageable.unpaged())).isInstanceOf(AccessDeniedException.class);
    }

    private MediaEditDto edit(long id) {
        return service.findMediaForEdit(id).orElseThrow();
    }

    @Test
    void inlineReferencesArePersistedAndCanBeAssignedToMediaImmediately() {
        AuthorCreateDto author = new AuthorCreateDto();
        author.setFirstName(" Grace ");
        author.setLastName(" Hopper ");
        var createdAuthor = referenceService.createAuthor(author);
        PublisherCreateDto publisher = new PublisherCreateDto();
        publisher.setName(" New publisher ");
        publisher.setWebsite(" https://example.org ");
        var createdPublisher = referenceService.createPublisher(publisher);
        assertThat(createdAuthor.id()).isPositive();
        assertThat(createdAuthor.firstName()).isEqualTo("Grace");
        assertThat(createdAuthor.lastName()).isEqualTo("Hopper");
        assertThat(createdPublisher.id()).isPositive();
        assertThat(createdPublisher.name()).isEqualTo("New publisher");
        assertThat(createdPublisher.website()).isEqualTo("https://example.org");
        assertThat(author.getFirstName()).isEqualTo(" Grace ");
        assertThat(publisher.getName()).isEqualTo(" New publisher ");
        assertThat(lookupService.getFormOptions().authors()).contains(createdAuthor);
        assertThat(lookupService.getFormOptions().publishers()).contains(createdPublisher);
        MediaCreateDto dto = form("Uses inline references");
        dto.setPublisherId(createdPublisher.id());
        MediaAuthorWriteDto assignment = new MediaAuthorWriteDto();
        assignment.setAuthorId(createdAuthor.id());
        dto.setAuthors(List.of(assignment));
        MediaEditDto saved = edit(service.createMedia(dto).id());
        assertThat(saved.getPublisherId()).isEqualTo(createdPublisher.id());
        assertThat(saved.getAuthors().getFirst().getAuthorId()).isEqualTo(createdAuthor.id());
        author.setFirstName(" ");
        assertThat(referenceService.createAuthor(author).firstName()).isNull();
        publisher.setWebsite(" ");
        assertThat(referenceService.createPublisher(publisher).website()).isNull();
    }

    @Test
    void inlineReferenceValidationRejectsInvalidInputWithoutPersistingIt() {
        AuthorCreateDto author = new AuthorCreateDto();
        author.setLastName(" ");
        assertThatIllegalArgumentException().isThrownBy(() -> referenceService.createAuthor(author));
        PublisherCreateDto publisher = new PublisherCreateDto();
        publisher.setName(" ");
        assertThatIllegalArgumentException().isThrownBy(() -> referenceService.createPublisher(publisher));
        publisher.setName("Valid name");
        publisher.setWebsite("not a URL");
        assertThatIllegalArgumentException().isThrownBy(() -> referenceService.createPublisher(publisher));
        assertThatIllegalArgumentException().isThrownBy(() -> referenceService.createAuthor(null));
        assertThatIllegalArgumentException().isThrownBy(() -> referenceService.createPublisher(null));
        assertThat(authors.count()).isEqualTo(1);
        assertThat(publishers.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void membersCannotCreateInlineReferences() {
        assertThatThrownBy(() -> referenceService.createAuthor(new AuthorCreateDto())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> referenceService.createPublisher(new PublisherCreateDto())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void inventoryCreationPreservesPreassignedUuidsAndMatchingBarcodes() {
        MediaCreateDto dto = form("Three copies");
        dto.getInventoryItems().addAll(List.of(MediaInventoryItemDto.newCopy(),
                MediaInventoryItemDto.newCopy(), MediaInventoryItemDto.newCopy()));
        long id = service.createMediaWithInventory(dto).id();
        MediaEditDto loaded = service.findMediaWithInventoryForEdit(id).orElseThrow();
        assertThat(loaded.getInventoryItems()).hasSize(3);
        assertThat(loaded.getInventoryItems()).extracting(MediaInventoryItemDto::inventoryNumber)
                .containsExactlyElementsOf(dto.getInventoryItems().stream().map(MediaInventoryItemDto::inventoryNumber).toList());
        assertThat(loaded.getInventoryItems()).extracting(MediaInventoryItemDto::inventoryNumber).doesNotHaveDuplicates();
        assertThat(loaded.getInventoryItems()).allSatisfy(copy -> {
            assertThat(copy.id()).isPositive();
            assertThat(java.util.UUID.fromString(copy.inventoryNumber()).toString()).isEqualTo(copy.inventoryNumber());
            assertThat(copy.barcode()).isEqualTo(copy.inventoryNumber());
        });
    }

    @Test
    void newInventoryCopiesRequireDistinctMatchingUuids() {
        MediaCreateDto dto = form("Invalid inventory");
        String uuid = java.util.UUID.randomUUID().toString();
        for (var copy : List.of(new MediaInventoryItemDto(null, null, null),
                new MediaInventoryItemDto(null, "not-a-uuid", "not-a-uuid"),
                new MediaInventoryItemDto(null, uuid, "different-barcode"))) {
            dto.setInventoryItems(List.of(copy));
            assertThatIllegalArgumentException().isThrownBy(() -> service.createMediaWithInventory(dto));
        }
        MediaInventoryItemDto copy = MediaInventoryItemDto.newCopy();
        dto.setInventoryItems(List.of(copy, copy));
        assertThatIllegalArgumentException().isThrownBy(() -> service.createMediaWithInventory(dto));
        assertThat(service.countMedia("Invalid inventory")).isZero();
    }

    @Test
    void inventoryUpdateRetainsExistingCopyDataAndAddsAndRemovesCopies() {
        MediaCreateDto dto = form("Copies");
        dto.getInventoryItems().addAll(List.of(MediaInventoryItemDto.newCopy(),
                MediaInventoryItemDto.newCopy()));
        long id = service.createMediaWithInventory(dto).id();
        MediaEditDto loaded = service.findMediaWithInventoryForEdit(id).orElseThrow();
        MediaInventoryItemDto keep = loaded.getInventoryItems().getFirst();
        jdbc.update("update inventory_items set condition='WORN', notes='Keep these notes', acquisition_cost=12.34 where id=?", keep.id());
        loaded.setInventoryItems(List.of(new MediaInventoryItemDto(keep.id(), "tampered", "tampered"),
                MediaInventoryItemDto.newCopy()));
        loaded.setTitle("Updated with inventory");
        MediaEditDto updated = service.updateMediaWithInventory(loaded);
        assertThat(updated.getTitle()).isEqualTo("Updated with inventory");
        assertThat(updated.getInventoryItems()).hasSize(2).contains(keep);
        assertThat(updated.getInventoryItems().getLast().inventoryNumber())
                .isEqualTo(loaded.getInventoryItems().getLast().inventoryNumber());
        assertThat(updated.getInventoryItems()).doesNotContain(loaded.getInventoryItems().getLast());
        assertThat(jdbc.queryForMap("select condition, notes, acquisition_cost from inventory_items where id=?", keep.id()))
                .containsEntry("condition", "WORN").containsEntry("notes", "Keep these notes")
                .containsEntry("acquisition_cost", new java.math.BigDecimal("12.34"));
        assertThat(service.findMediaWithInventoryForEdit(id).orElseThrow().getInventoryItems())
                .containsExactlyElementsOf(updated.getInventoryItems());
        loaded.setInventoryItems(List.of());
        assertThat(service.updateMediaWithInventory(loaded).getInventoryItems()).isEmpty();
    }

    @Test
    void inventoryRejectsForeignMissingAndDuplicateCopyIdsWithoutSavingMediaChanges() {
        MediaCreateDto dto = form("Owner");
        dto.getInventoryItems().add(MediaInventoryItemDto.newCopy());
        long owner = service.createMediaWithInventory(dto).id();
        MediaInventoryItemDto copy = service.findMediaWithInventoryForEdit(owner).orElseThrow().getInventoryItems().getFirst();
        long other = service.createMedia(form("Other")).id();
        MediaEditDto edit = service.findMediaWithInventoryForEdit(other).orElseThrow();
        edit.setTitle("Must not save");
        edit.setInventoryItems(List.of(copy));
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateMediaWithInventory(edit));
        assertThat(service.findMediaForEdit(other).orElseThrow().getTitle()).isEqualTo("Other");
        assertThat(service.findMediaWithInventoryForEdit(owner).orElseThrow().getInventoryItems()).containsExactly(copy);
        edit.setInventoryItems(List.of(new MediaInventoryItemDto(Long.MAX_VALUE, null, null)));
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateMediaWithInventory(edit));
        MediaEditDto duplicate = service.findMediaWithInventoryForEdit(owner).orElseThrow();
        duplicate.setInventoryItems(List.of(copy, copy));
        assertThatIllegalArgumentException().isThrownBy(() -> service.updateMediaWithInventory(duplicate));
        dto.setInventoryItems(List.of(copy));
        assertThatIllegalArgumentException().isThrownBy(() -> service.createMediaWithInventory(dto));
        assertThat(service.countMedia("Owner")).isEqualTo(1);
    }

    @Test
    void failedCopyInsertRollsBackMediaCreationAndInventoryReplacement() {
        MediaCreateDto dto = form("Owner");
        dto.getInventoryItems().add(MediaInventoryItemDto.newCopy());
        long id = service.createMediaWithInventory(dto).id();
        MediaEditDto loaded = service.findMediaWithInventoryForEdit(id).orElseThrow();
        List<MediaInventoryItemDto> originalCopies = loaded.getInventoryItems();
        loaded.setTitle("Must roll back");
        loaded.setInventoryItems(List.of(MediaInventoryItemDto.newCopy()));
        MediaCreateDto rejected = form("Rejected");
        rejected.getIdentifiers().add(identifier("ISBN", "must-roll-back"));
        rejected.getInventoryItems().add(MediaInventoryItemDto.newCopy());
        jdbc.execute("""
                create function reject_test_inventory_insert() returns trigger language plpgsql as $$
                begin
                    raise exception 'test inventory failure' using errcode = '23514';
                end $$
                """);
        jdbc.execute("create trigger reject_test_inventory_insert before insert on inventory_items "
                + "for each row execute function reject_test_inventory_insert()");
        try {
            assertThatThrownBy(() -> service.createMediaWithInventory(rejected)).isInstanceOf(DataIntegrityViolationException.class);
            assertThatThrownBy(() -> service.updateMediaWithInventory(loaded)).isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            jdbc.execute("drop trigger reject_test_inventory_insert on inventory_items");
            jdbc.execute("drop function reject_test_inventory_insert()");
        }
        assertThat(service.countMedia("Rejected")).isZero();
        assertThat(jdbc.queryForObject("select count(*) from media_identifiers", Long.class)).isZero();
        MediaEditDto unchanged = service.findMediaWithInventoryForEdit(id).orElseThrow();
        assertThat(unchanged.getTitle()).isEqualTo("Owner");
        assertThat(unchanged.getInventoryItems()).containsExactlyElementsOf(originalCopies);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void membersCannotUseInventoryMediaMethods() {
        assertThatThrownBy(() -> service.findMediaWithInventoryForEdit(1L)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.createMediaWithInventory(form("Forbidden"))).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.updateMediaWithInventory(new MediaEditDto())).isInstanceOf(AccessDeniedException.class);
    }

    private MediaCreateDto form(String title) {
        MediaCreateDto form = new MediaCreateDto();
        form.setMediaTypeId(book.getId());
        form.setTitle(title);
        return form;
    }

    private MediaAuthorWriteDto assignment(String role, short order) {
        MediaAuthorWriteDto dto = new MediaAuthorWriteDto();
        dto.setAuthorId(author.getId());
        dto.setRole(role);
        dto.setSortOrder(order);
        return dto;
    }

    private MediaIdentifierWriteDto identifier(String type, String value) {
        MediaIdentifierWriteDto dto = new MediaIdentifierWriteDto();
        dto.setIdentifierType(type);
        dto.setIdentifierValue(value);
        return dto;
    }

    static class SqlCapture implements StatementInspector {
        final List<String> statements = new CopyOnWriteArrayList<>();

        @Override
        public String inspect(String statement) {
            statements.add(statement);
            return statement;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableJpaRepositories(basePackageClasses = MediaRepository.class)
    @EnableTransactionManagement
    @EnableMethodSecurity
    @Import({MediaManagementServiceImpl.class, MediaLookupServiceImpl.class, MediaReferenceServiceImpl.class, MediaMapper.class,
            MediaAuthorMapper.class, MediaIdentifierMapper.class, AuthorMapper.class,
            MediaTypeMapper.class, PublisherMapper.class})
    static class Config {
        @Bean
        DataSource dataSource() {
            DriverManagerDataSource source = new DriverManagerDataSource(
                    POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
            new ResourceDatabasePopulator(new ClassPathResource(
                    "db/migration/V1_2__initial_inventory_tables_schema.sql")).execute(source);
            return source;
        }

        @Bean
        SqlCapture sqlCapture() {
            return new SqlCapture();
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, SqlCapture sqlCapture) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.grosshaeuser.olmp.inventory.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of(AvailableSettings.HBM2DDL_AUTO, "validate",
                    AvailableSettings.STATEMENT_INSPECTOR, sqlCapture));
            return factory;
        }

        @Bean
        JpaTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }
}
