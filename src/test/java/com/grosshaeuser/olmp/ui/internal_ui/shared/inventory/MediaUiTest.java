package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.media.*;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import com.grosshaeuser.olmp.inventory.service.MediaManagementService;
import com.grosshaeuser.olmp.inventory.service.impl.BarcodeServiceImpl;
import com.grosshaeuser.olmp.inventory.service.impl.BarcodePrintServiceImpl;
import com.grosshaeuser.olmp.inventory.service.impl.InventoryNumberServiceImpl;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiNotificationHandler;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MediaUiTest {

    private static final MediaTypeSummaryDto BOOK = new MediaTypeSummaryDto(1L, "BOOK", "Book");
    private static final AuthorSummaryDto AUTHOR = new AuthorSummaryDto(2L, "Ada", "Lovelace");
    private static final PublisherSummaryDto PUBLISHER = new PublisherSummaryDto(3L, "Publisher", "https://example.org");
    private static final MediaFormOptions OPTIONS = new MediaFormOptions(List.of(BOOK), List.of(PUBLISHER), List.of(AUTHOR));
    private UI ui;

    @BeforeEach
    void setCurrentUi() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void clearCurrentUi() {
        UI.setCurrent(null);
    }

    @Test
    void createFormRequiresTitleAndTypeAndRejectsWhitespaceTitle() {
        BarcodeServiceImpl barcodeService = new BarcodeServiceImpl();
        MediaForm<MediaCreateDto> form = new MediaForm<>(MediaCreateDto.class, OPTIONS, new InventoryNumberServiceImpl(), barcodeService, new BarcodePrintServiceImpl(barcodeService), mock(MediaReferenceService.class));
        form.read(new MediaCreateDto());
        assertThat(form.write(new MediaCreateDto())).isFalse();
        text(form, "Title").setValue(" ");
        combo(form, "Media type").setValue(BOOK);
        assertThat(form.write(new MediaCreateDto())).isFalse();
        text(form, "Title").setValue("A title");
        assertThat(form.write(new MediaCreateDto())).isTrue();
    }

    @Test
    void publicationYearIsOptionalAndRejectsValuesBeforeShortConversion() {
        MediaForm<MediaCreateDto> form = populatedForm();
        IntegerField year = integer(form, "Publication year");
        year.setValue(40000);
        assertThat(form.write(new MediaCreateDto())).isFalse();
        year.setValue(2026);
        MediaCreateDto dto = new MediaCreateDto();
        assertThat(form.write(dto)).isTrue();
        assertThat(dto.getPublicationYear()).isEqualTo((short) 2026);
        year.clear();
        assertThat(form.write(dto)).isTrue();
        assertThat(dto.getPublicationYear()).isNull();
    }

    @Test
    void createFormAddsAuthorsAndNormalizesIdentifiers() {
        MediaForm<MediaCreateDto> form = populatedForm();
        button(form, "Add author").click();
        combo(form, "Author").setValue(AUTHOR);
        button(form, "Add identifier").click();
        text(form, "Identifier type").setValue(" isbn ");
        text(form, "Identifier value").setValue(" 978-3-16-148410-0 ");
        text(form, "Cover image filename").setValue("cover.png");

        MediaCreateDto dto = new MediaCreateDto();
        assertThat(form.write(dto)).isTrue();
        assertThat(dto.getAuthors()).hasSize(1);
        assertThat(dto.getAuthors().getFirst().getAuthorId()).isEqualTo(2L);
        assertThat(dto.getAuthors().getFirst().getRole()).isEqualTo("AUTHOR");
        assertThat(dto.getAuthors().getFirst().getSortOrder()).isZero();
        assertThat(dto.getIdentifiers().getFirst().getId()).isNull();
        assertThat(dto.getIdentifiers().getFirst().getIdentifierType()).isEqualTo("ISBN");
        assertThat(dto.getIdentifiers().getFirst().getIdentifierValue()).isEqualTo("978-3-16-148410-0");
        assertThat(dto.getCoverImageFileName()).isEqualTo("cover.png");
    }

    @Test
    void invalidNestedRowsPreventSavingAndCanBeRemoved() {
        MediaForm<MediaCreateDto> form = populatedForm();
        button(form, "Add author").click();
        button(form, "Add identifier").click();
        assertThat(form.write(new MediaCreateDto())).isFalse();
        button(form, "Remove author").click();
        button(form, "Remove identifier").click();
        assertThat(form.write(new MediaCreateDto())).isTrue();
    }

    @Test
    void duplicateAuthorRolesAndIdentifierPairsAreRejected() {
        MediaEditDto existing = editableMedia();
        MediaAuthorWriteDto duplicateAuthor = new MediaAuthorWriteDto();
        duplicateAuthor.setAuthorId(2L);
        existing.getAuthors().add(duplicateAuthor);
        BarcodeServiceImpl barcodeService = new BarcodeServiceImpl();
        MediaForm<MediaEditDto> form = new MediaForm<>(MediaEditDto.class, OPTIONS, new InventoryNumberServiceImpl(), barcodeService, new BarcodePrintServiceImpl(barcodeService), mock(MediaReferenceService.class));
        form.read(existing);
        assertThat(form.write(new MediaEditDto())).isFalse();

        existing.getAuthors().removeLast();
        MediaIdentifierWriteDto duplicateId = new MediaIdentifierWriteDto();
        duplicateId.setIdentifierType("ISBN");
        duplicateId.setIdentifierValue("original");
        existing.getIdentifiers().add(duplicateId);
        form.read(existing);
        assertThat(form.write(new MediaEditDto())).isFalse();
    }

    @Test
    void bufferedEditingRetainsIdentifierIdsAndDoesNotMutateLoadedDto() {
        MediaEditDto source = editableMedia();
        BarcodeServiceImpl barcodeService = new BarcodeServiceImpl();
        MediaForm<MediaEditDto> form = new MediaForm<>(MediaEditDto.class, OPTIONS, new InventoryNumberServiceImpl(), barcodeService, new BarcodePrintServiceImpl(barcodeService), mock(MediaReferenceService.class));
        form.read(source);
        text(form, "Title").setValue("Changed");
        text(form, "Role").setValue("EDITOR");
        text(form, "Identifier value").setValue("changed");
        MediaEditDto submission = new MediaEditDto();
        submission.setId(source.getId());
        assertThat(form.write(submission)).isTrue();
        assertThat(submission.getIdentifiers().getFirst().getId()).isEqualTo(20L);
        assertThat(submission.getIdentifiers().getFirst().getIdentifierValue()).isEqualTo("changed");
        assertThat(source.getTitle()).isEqualTo("Original");
        assertThat(source.getAuthors().getFirst().getRole()).isEqualTo("AUTHOR");
        assertThat(source.getIdentifiers().getFirst().getIdentifierValue()).isEqualTo("original");
    }

    @Test
    void editComponentResetsFieldsAndChildrenAndSubmitsTheOriginalId() {
        MediaEditDto source = editableMedia();
        AtomicReference<MediaEditDto> saved = new AtomicReference<>();
        AtomicInteger closed = new AtomicInteger();
        BarcodeServiceImpl barcodeService = new BarcodeServiceImpl();
        MediaEditComponent editor = new MediaEditComponent(source, OPTIONS, new InventoryNumberServiceImpl(), barcodeService, new BarcodePrintServiceImpl(barcodeService), mock(MediaReferenceService.class), dto -> {
            saved.set(dto);
            return dto;
        }, closed::incrementAndGet, exception -> { throw exception; });

        text(editor, "Title").setValue("Discard this");
        button(editor, "Remove identifier").click();
        button(editor, "Reset").click();
        assertThat(text(editor, "Title").getValue()).isEqualTo("Original");
        assertThat(text(editor, "Identifier value").getValue()).isEqualTo("original");
        text(editor, "Title").setValue("Saved");
        button(editor, "Save changes").click();
        assertThat(saved.get().getId()).isEqualTo(10L);
        assertThat(saved.get().getTitle()).isEqualTo("Saved");
        assertThat(source.getTitle()).isEqualTo("Original");
        text(editor, "Title").setValue("Discard after save");
        button(editor, "Reset").click();
        assertThat(text(editor, "Title").getValue()).isEqualTo("Saved");
        button(editor, "Close").click();
        assertThat(closed).hasValue(1);
    }

    @Test
    void failedEditKeepsInputAvailableForRetryAndRoutesException() {
        List<RuntimeException> errors = new ArrayList<>();
        AtomicInteger attempts = new AtomicInteger();
        BarcodeServiceImpl barcodeService = new BarcodeServiceImpl();
        MediaEditComponent editor = new MediaEditComponent(editableMedia(), OPTIONS, new InventoryNumberServiceImpl(), barcodeService, new BarcodePrintServiceImpl(barcodeService), mock(MediaReferenceService.class), dto -> {
            if (attempts.getAndIncrement() == 0) {
                throw new IllegalArgumentException("Already exists");
            }
            return dto;
        }, () -> { }, errors::add);
        text(editor, "Title").setValue("Try again");
        button(editor, "Save changes").click();
        assertThat(errors).hasSize(1);
        assertThat(text(editor, "Title").getValue()).isEqualTo("Try again");
        button(editor, "Save changes").click();
        assertThat(attempts).hasValue(2);
    }

    @Test
    void createDialogStaysOpenOnSaveFailureAndClosesAfterRetry() {
        List<RuntimeException> errors = new ArrayList<>();
        AtomicInteger attempts = new AtomicInteger();
        MediaCreateDialog dialog = new MediaCreateDialog(OPTIONS, new InventoryNumberServiceImpl(), new BarcodeServiceImpl(), mock(MediaReferenceService.class), dto -> {
            if (attempts.getAndIncrement() == 0) {
                throw new IllegalArgumentException("Already exists");
            }
        }, errors::add);
        dialog.open();
        text(dialog, "Title").setValue("New media");
        combo(dialog, "Media type").setValue(BOOK);
        Component actions = dialog.getFooter().getElement().getChild(0).getComponent().orElseThrow();
        button(actions, "Save").click();
        assertThat(dialog.isOpened()).isTrue();
        assertThat(errors).hasSize(1);
        button(actions, "Save").click();
        assertThat(dialog.isOpened()).isFalse();
        assertThat(attempts).hasValue(2);
    }

    @Test
    void searchUsesTrimmedFilterAndBackendPagingSortingAndCount() {
        FakeMediaService service = new FakeMediaService();
        MediaManagementView view = new MediaManagementView(service, () -> OPTIONS, new InventoryNumberServiceImpl(), new BarcodeServiceImpl(),
                new BarcodePrintServiceImpl(new BarcodeServiceImpl()), mock(MediaReferenceService.class),
                new UiExceptionHandler(), new UiNotificationHandler());
        Grid<MediaSummaryDto> grid = grid(view);
        AtomicInteger refreshes = new AtomicInteger();
        grid.getDataProvider().addDataProviderListener(_ -> refreshes.incrementAndGet());
        text(view, "Search media").setValue("  Subtitle fragment  ");
        assertThat(grid.getDataProvider().fetch(new Query<>(50, 50,
                List.of(new QuerySortOrder("title", SortDirection.DESCENDING)), null, null)).toList()).hasSize(1);
        assertThat(service.lastQuery).isEqualTo("Subtitle fragment");
        assertThat(service.lastPageable.getOffset()).isEqualTo(50);
        assertThat(service.lastPageable.getPageSize()).isEqualTo(50);
        assertThat(service.lastPageable.getSort().getOrderFor("title").isDescending()).isTrue();
        assertThat(grid.getDataProvider().size(new Query<>())).isEqualTo(123);
        assertThat(service.lastCountQuery).isEqualTo("Subtitle fragment");
        assertThat(refreshes).hasValue(1);
    }

    @Test
    void authorAndPublisherFiltersCombineWithSearchAndCanBeClearedIndependently() {
        FakeMediaService service = new FakeMediaService();
        MediaManagementView view = new MediaManagementView(service, () -> OPTIONS, new InventoryNumberServiceImpl(), new BarcodeServiceImpl(),
                new BarcodePrintServiceImpl(new BarcodeServiceImpl()), mock(MediaReferenceService.class),
                new UiExceptionHandler(), new UiNotificationHandler());
        Grid<MediaSummaryDto> grid = grid(view);
        ComboBox<Object> author = combo(view, "Author");
        ComboBox<Object> publisher = combo(view, "Publisher");
        assertThat(author.getDataProvider().fetch(new Query<>()).toList()).containsExactly(AUTHOR);
        assertThat(publisher.getDataProvider().fetch(new Query<>()).toList()).containsExactly(PUBLISHER);
        assertThat(author.isClearButtonVisible()).isTrue();
        assertThat(publisher.isClearButtonVisible()).isTrue();
        assertThat(author.getParent().orElseThrow().getChildren().toList())
                .containsExactly(text(view, "Search media"), author, publisher, button(view, "Create media"));
        AtomicInteger refreshes = new AtomicInteger();
        grid.getDataProvider().addDataProviderListener(_ -> refreshes.incrementAndGet());
        text(view, "Search media").setValue("  fragment  ");
        author.setValue(AUTHOR);
        publisher.setValue(PUBLISHER);
        grid.getDataProvider().fetch(new Query<>()).toList();
        assertThat(service.lastQuery).isEqualTo("fragment");
        assertThat(service.lastAuthorId).isEqualTo(AUTHOR.id());
        assertThat(service.lastPublisherId).isEqualTo(PUBLISHER.id());
        assertThat(grid.getDataProvider().size(new Query<>())).isEqualTo(123);
        assertThat(service.lastCountQuery).isEqualTo("fragment");
        assertThat(service.lastCountAuthorId).isEqualTo(AUTHOR.id());
        assertThat(service.lastCountPublisherId).isEqualTo(PUBLISHER.id());
        author.clear();
        grid.getDataProvider().fetch(new Query<>()).toList();
        assertThat(service.lastAuthorId).isNull();
        assertThat(service.lastPublisherId).isEqualTo(PUBLISHER.id());
        publisher.clear();
        grid.getDataProvider().size(new Query<>());
        assertThat(service.lastCountAuthorId).isNull();
        assertThat(service.lastCountPublisherId).isNull();
        assertThat(service.lastCountQuery).isEqualTo("fragment");
        assertThat(refreshes).hasValue(5);
    }

    private MediaForm<MediaCreateDto> populatedForm() {
        BarcodeServiceImpl barcodeService = new BarcodeServiceImpl();
        MediaForm<MediaCreateDto> form = new MediaForm<>(MediaCreateDto.class, OPTIONS, new InventoryNumberServiceImpl(), barcodeService, new BarcodePrintServiceImpl(barcodeService), mock(MediaReferenceService.class));
        form.read(new MediaCreateDto());
        text(form, "Title").setValue("A title");
        combo(form, "Media type").setValue(BOOK);
        return form;
    }

    private MediaEditDto editableMedia() {
        MediaEditDto media = new MediaEditDto();
        media.setId(10L);
        media.setMediaTypeId(1L);
        media.setTitle("Original");
        MediaAuthorWriteDto author = new MediaAuthorWriteDto();
        author.setAuthorId(2L);
        media.getAuthors().add(author);
        MediaIdentifierWriteDto identifier = new MediaIdentifierWriteDto();
        identifier.setId(20L);
        identifier.setIdentifierType("ISBN");
        identifier.setIdentifierValue("original");
        media.getIdentifiers().add(identifier);
        return media;
    }

    private static Stream<Component> descendants(Component parent) {
        Stream<Component> children = parent instanceof com.vaadin.flow.component.tabs.TabSheet sheet
                ? java.util.stream.IntStream.range(0, sheet.getTabCount()).mapToObj(sheet::getTabAt).map(sheet::getComponent)
                : parent.getChildren();
        return Stream.concat(Stream.of(parent), children.flatMap(MediaUiTest::descendants));
    }

    private static TextField text(Component root, String label) {
        return descendants(root).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(field -> field.getLabel().equals(label)).findFirst().orElseThrow();
    }

    private static IntegerField integer(Component root, String label) {
        return descendants(root).filter(IntegerField.class::isInstance).map(IntegerField.class::cast)
                .filter(field -> field.getLabel().equals(label)).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static ComboBox<Object> combo(Component root, String label) {
        return (ComboBox<Object>) descendants(root).filter(ComboBox.class::isInstance).map(ComboBox.class::cast)
                .filter(field -> field.getLabel().equals(label)).findFirst().orElseThrow();
    }

    private static Button button(Component root, String text) {
        Button result = descendants(root).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getText().equals(text) || text.equals(button.getElement().getAttribute("aria-label"))).findFirst().orElseThrow();
        descendants(root).filter(com.vaadin.flow.component.tabs.TabSheet.class::isInstance)
                .map(com.vaadin.flow.component.tabs.TabSheet.class::cast).forEach(sheet -> {
                    for (int index = 0; index < sheet.getTabCount(); index++) {
                        if (descendants(sheet.getComponent(sheet.getTabAt(index))).anyMatch(component -> component == result)) {
                            sheet.setSelectedIndex(index);
                            break;
                        }
                    }
                });
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Grid<MediaSummaryDto> grid(Component root) {
        return (Grid<MediaSummaryDto>) descendants(root).filter(Grid.class::isInstance).findFirst().orElseThrow();
    }

    private static class FakeMediaService implements MediaManagementService {
        private String lastQuery;
        private String lastCountQuery;
        private Pageable lastPageable;
        private Long lastAuthorId;
        private Long lastPublisherId;
        private Long lastCountAuthorId;
        private Long lastCountPublisherId;

        @Override
        public Optional<MediaEditDto> findMediaWithInventoryForEdit(long id) {
            return findMediaForEdit(id);
        }

        @Override
        public MediaSummaryDto createMediaWithInventory(MediaCreateDto media) {
            return createMedia(media);
        }

        @Override
        public MediaEditDto updateMediaWithInventory(MediaEditDto media) {
            return updateMedia(media);
        }

        @Override
        public List<MediaSummaryDto> findMedia(String query, Pageable pageable) {
            return findMedia(query, null, null, pageable);
        }

        @Override
        public List<MediaSummaryDto> findMedia(String query, Long authorId, Long publisherId, Pageable pageable) {
            lastQuery = query;
            lastAuthorId = authorId;
            lastPublisherId = publisherId;
            lastPageable = pageable;
            return List.of(new MediaSummaryDto(10L, "Title", BOOK, null, (short) 2026, List.of()));
        }

        @Override
        public long countMedia(String query) {
            return countMedia(query, null, null);
        }

        @Override
        public long countMedia(String query, Long authorId, Long publisherId) {
            lastCountQuery = query;
            lastCountAuthorId = authorId;
            lastCountPublisherId = publisherId;
            return 123;
        }

        @Override
        public Optional<MediaEditDto> findMediaForEdit(long id) {
            return Optional.empty();
        }

        @Override
        public MediaSummaryDto createMedia(MediaCreateDto media) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MediaEditDto updateMedia(MediaEditDto media) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteMedium(Long id) {

        }
    }
}
