package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorCreateDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorEditDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.media.*;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherCreateDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherEditDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import com.grosshaeuser.olmp.inventory.service.impl.BarcodeServiceImpl;
import com.grosshaeuser.olmp.inventory.service.impl.InventoryNumberServiceImpl;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MediaReferenceFormTest {
    private static final AuthorSummaryDto AUTHOR = new AuthorSummaryDto(2L, "Ada", "Lovelace");
    private static final PublisherSummaryDto PUBLISHER = new PublisherSummaryDto(9L, "Existing", "https://example.org");
    private UI ui;
    private MediaReferenceService references;
    private MediaForm<MediaCreateDto> form;
    private MediaCreateDto source;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
        references = mock(MediaReferenceService.class);
        var book = new MediaTypeSummaryDto(1L, "BOOK", "Book");
        form = new MediaForm<>(MediaCreateDto.class, new MediaFormOptions(List.of(book), List.of(PUBLISHER), List.of(AUTHOR)), new InventoryNumberServiceImpl(),
                new BarcodeServiceImpl(), null, references);
        source = new MediaCreateDto();
        source.setMediaTypeId(book.id());
        source.setTitle("Unsaved title");
        MediaAuthorWriteDto first = new MediaAuthorWriteDto();
        first.setAuthorId(AUTHOR.id());
        MediaAuthorWriteDto second = new MediaAuthorWriteDto();
        second.setAuthorId(AUTHOR.id());
        second.setRole("EDITOR");
        source.setAuthors(List.of(first, second));
        form.read(source);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void createsAndSelectsPublisherWhilePreservingOtherInputAndLookupOnRead() {
        PublisherSummaryDto created = new PublisherSummaryDto(9L, "New publisher", "https://example.org");
        when(references.createPublisher(any())).thenReturn(created);
        assertThat(panelVisible("Save publisher")).isFalse();
        button("Create publisher").click();
        assertThat(panelVisible("Save publisher")).isTrue();
        field("Publisher name").setValue(" New publisher ");
        field("Publisher URL").setValue(" https://example.org ");
        button("Save publisher").click();
        assertThat(panelVisible("Save publisher")).isFalse();
        assertThat(combos("Publisher").getFirst().getValue()).isEqualTo(created);
        assertThat(field("Title").getValue()).isEqualTo("Unsaved title");
        ArgumentCaptor<PublisherCreateDto> submitted = ArgumentCaptor.forClass(PublisherCreateDto.class);
        verify(references).createPublisher(submitted.capture());
        assertThat(submitted.getValue().getName()).isEqualTo("New publisher");
        assertThat(submitted.getValue().getWebsite()).isEqualTo("https://example.org");
        MediaCreateDto medium = submission();
        assertThat(medium.getPublisherId()).isEqualTo(9L);
        form.read(medium);
        assertThat(submission().getPublisherId()).isEqualTo(9L);
        assertThat(source.getPublisherId()).isNull();
    }

    @Test
    void rejectsBlankPublisherNameAndInvalidUrlWithoutCallingService() {
        button("Create publisher").click();
        field("Publisher name").setValue("   ");
        button("Save publisher").click();
        assertThat(field("Publisher name").isInvalid()).isTrue();
        field("Publisher name").setValue("Valid name");
        field("Publisher URL").setValue("not a url");
        button("Save publisher").click();
        assertThat(field("Publisher URL").isInvalid()).isTrue();
        assertThat(panelVisible("Save publisher")).isTrue();
        verifyNoInteractions(references);
    }

    @Test
    void newAuthorIsSelectedInOriginRowAndAvailableInOtherAndFutureRows() {
        AuthorSummaryDto created = new AuthorSummaryDto(7L, "Grace", "Hopper");
        when(references.createAuthor(any())).thenReturn(created);
        button("Create author").click();
        field("First name").setValue(" Grace ");
        field("Last name").setValue(" Hopper ");
        button("Save author").click();
        List<ComboBox<Object>> authors = combos("Author");
        assertThat(authors.getFirst().getValue()).isEqualTo(created);
        assertThat(authors.get(1).getValue()).isEqualTo(AUTHOR);
        assertThat(authors.get(1).getDataProvider().fetch(new Query<>()).toList()).contains(created);
        ArgumentCaptor<AuthorCreateDto> submitted = ArgumentCaptor.forClass(AuthorCreateDto.class);
        verify(references).createAuthor(submitted.capture());
        assertThat(submitted.getValue().getFirstName()).isEqualTo("Grace");
        assertThat(submitted.getValue().getLastName()).isEqualTo("Hopper");
        assertThat(submission().getAuthors()).extracting(MediaAuthorWriteDto::getAuthorId).containsExactly(7L, 2L);
        assertThat(source.getAuthors().getFirst().getAuthorId()).isEqualTo(2L);
        button("Add author").click();
        assertThat(combos("Author").getLast().getDataProvider().fetch(new Query<>()).toList()).contains(created);
    }

    @Test
    void failedAuthorCreationKeepsInputForRetryAndFirstNameIsOptional() {
        when(references.createAuthor(any())).thenThrow(new IllegalArgumentException("Try again"))
                .thenReturn(new AuthorSummaryDto(8L, null, "Anonymous"));
        button("Create author").click();
        field("Last name").setValue("  ");
        button("Save author").click();
        verifyNoInteractions(references);
        field("Last name").setValue("Anonymous");
        button("Save author").click();
        assertThat(field("Last name").getValue()).isEqualTo("Anonymous");
        assertThat(panelVisible("Save author")).isTrue();
        button("Save author").click();
        assertThat(panelVisible("Save author")).isFalse();
        assertThat(combos("Author").getFirst().getValue()).isEqualTo(new AuthorSummaryDto(8L, null, "Anonymous"));
        verify(references, times(2)).createAuthor(any());
    }

    @Test
    void cancelAndResetDiscardDraftsWithoutCreatingRecords() {
        button("Create publisher").click();
        field("Publisher name").setValue("Discard");
        button("Cancel publisher").click();
        assertThat(panelVisible("Save publisher")).isFalse();
        assertThat(field("Publisher name").getValue()).isEmpty();
        button("Create author").click();
        field("Last name").setValue("Discard");
        form.read(source);
        assertThat(panelVisible("Save author")).isFalse();
        assertThat(field("Last name").getValue()).isEmpty();
        verifyNoInteractions(references);
    }

    @Test
    void editButtonsFollowSelectionAndSitBetweenComboAndCreate() {
        Button editPublisher = button("Edit publisher");
        assertThat(editPublisher.isEnabled()).isFalse();
        ComboBox<Object> publisher = combos("Publisher").getFirst();
        publisher.setValue(new PublisherSummaryDto(9L, "Existing", "https://example.org"));
        assertThat(editPublisher.isEnabled()).isTrue();
        assertThat(editPublisher.getParent().orElseThrow().getChildren().toList())
                .containsExactly(publisher, editPublisher, button("Create publisher"));
        publisher.clear();
        assertThat(editPublisher.isEnabled()).isFalse();
        assertThat(button("Edit author").isEnabled()).isTrue();
        combos("Author").getFirst().clear();
        assertThat(button("Edit author").isEnabled()).isFalse();
    }

    @Test
    void editsPublisherInPlaceAndPreservesMediaInputAndSelectionOnRead() {
        PublisherSummaryDto updated = new PublisherSummaryDto(9L, "Renamed", "https://updated.org");
        when(references.updatePublisher(any())).thenReturn(updated);
        combos("Publisher").getFirst().setValue(PUBLISHER);
        button("Edit publisher").click();
        assertThat(field("Publisher name").getValue()).isEqualTo(PUBLISHER.name());
        assertThat(field("Publisher URL").getValue()).isEqualTo(PUBLISHER.website());
        field("Publisher name").setValue(" Renamed ");
        field("Publisher URL").setValue(" https://updated.org ");
        button("Save publisher").click();
        ArgumentCaptor<PublisherEditDto> submitted = ArgumentCaptor.forClass(PublisherEditDto.class);
        verify(references).updatePublisher(submitted.capture());
        assertThat(submitted.getValue().getId()).isEqualTo(9L);
        assertThat(submitted.getValue().getName()).isEqualTo("Renamed");
        assertThat(submitted.getValue().getWebsite()).isEqualTo(updated.website());
        assertThat(combos("Publisher").getFirst().getValue()).isEqualTo(updated);
        assertThat(field("Title").getValue()).isEqualTo("Unsaved title");
        assertThat(panelVisible("Save publisher")).isFalse();
        assertThat(combos("Publisher").getFirst().getDataProvider().fetch(new Query<>()).toList()).containsExactly(updated);
        MediaCreateDto medium = submission();
        form.read(medium);
        assertThat(combos("Publisher").getFirst().getValue()).isEqualTo(updated);
        verify(references, never()).createPublisher(any());
    }

    @Test
    void editedAuthorRefreshesEverySelectedRowAndFutureRowsWithSameId() {
        AuthorSummaryDto updated = new AuthorSummaryDto(AUTHOR.id(), "Augusta Ada", "King");
        when(references.updateAuthor(any())).thenReturn(updated);
        button("Edit author").click();
        assertThat(field("First name").getValue()).isEqualTo(AUTHOR.firstName());
        assertThat(field("Last name").getValue()).isEqualTo(AUTHOR.lastName());
        field("First name").setValue(updated.firstName());
        field("Last name").setValue(updated.lastName());
        button("Save author").click();
        ArgumentCaptor<AuthorEditDto> submitted = ArgumentCaptor.forClass(AuthorEditDto.class);
        verify(references).updateAuthor(submitted.capture());
        assertThat(submitted.getValue().getId()).isEqualTo(AUTHOR.id());
        assertThat(combos("Author")).allSatisfy(combo -> {
            assertThat(combo.getValue()).isEqualTo(updated);
            assertThat(combo.getDataProvider().fetch(new Query<>()).toList()).containsExactly(updated);
        });
        MediaCreateDto medium = submission();
        assertThat(medium.getAuthors()).extracting(MediaAuthorWriteDto::getAuthorId).containsExactly(2L, 2L);
        form.read(medium);
        assertThat(combos("Author")).allSatisfy(combo -> assertThat(combo.getValue()).isEqualTo(updated));
        button("Add author").click();
        assertThat(combos("Author").getLast().getDataProvider().fetch(new Query<>()).toList()).containsExactly(updated);
        verify(references, never()).createAuthor(any());
    }

    @Test
    void editValidationAndServiceFailureKeepDraftForRetry() {
        when(references.updateAuthor(any())).thenThrow(new IllegalArgumentException("Try again"))
                .thenReturn(new AuthorSummaryDto(AUTHOR.id(), "Ada", "King"));
        button("Edit author").click();
        field("Last name").setValue("  ");
        button("Save author").click();
        verifyNoInteractions(references);
        field("Last name").setValue("King");
        button("Save author").click();
        assertThat(field("Last name").getValue()).isEqualTo("King");
        assertThat(panelVisible("Save author")).isTrue();
        assertThat(combos("Author").getFirst().getValue()).isEqualTo(AUTHOR);
        button("Save author").click();
        assertThat(panelVisible("Save author")).isFalse();
        verify(references, times(2)).updateAuthor(any());
    }

    @Test
    void cancelSelectionChangeAndCreateDiscardEditMode() {
        button("Edit author").click();
        field("Last name").setValue("Discard");
        button("Cancel author").click();
        assertThat(combos("Author").getFirst().getValue()).isEqualTo(AUTHOR);
        button("Edit author").click();
        combos("Author").getFirst().clear();
        assertThat(panelVisible("Save author")).isFalse();
        combos("Author").getFirst().setValue(AUTHOR);
        button("Edit author").click();
        button("Create author").click();
        assertThat(field("First name").getValue()).isEmpty();
        assertThat(field("Last name").getValue()).isEmpty();
        when(references.createAuthor(any())).thenReturn(new AuthorSummaryDto(7L, "Grace", "Hopper"));
        field("Last name").setValue("Hopper");
        button("Save author").click();
        verify(references).createAuthor(any());
        verify(references, never()).updateAuthor(any());
    }

    private MediaCreateDto submission() {
        MediaCreateDto dto = new MediaCreateDto();
        assertThat(form.write(dto)).isTrue();
        return dto;
    }

    private boolean panelVisible(String saveLabel) {
        return button(saveLabel).getParent().orElseThrow().getParent().orElseThrow().isVisible();
    }

    private TextField field(String label) {
        return descendants(form).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(field -> field.getLabel().equals(label)).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private List<ComboBox<Object>> combos(String label) {
        return descendants(form).filter(ComboBox.class::isInstance).map(component -> (ComboBox<Object>) component)
                .filter(combo -> combo.getLabel().equals(label)).toList();
    }

    private Button button(String label) {
        Button button = descendants(form).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(candidate -> candidate.getText().equals(label)
                        || candidate.getAriaLabel().orElse("").replace("new ", "").equals(label)
                        || label.startsWith("Cancel ") && candidate.getText().equals("Cancel")
                        && candidate.getParent().orElseThrow().getChildren().filter(Button.class::isInstance)
                        .map(Button.class::cast).anyMatch(sibling -> sibling.getText().equals(label.replace("Cancel ", "Save "))))
                .findFirst().orElseThrow();
        descendants(form).filter(TabSheet.class::isInstance).map(TabSheet.class::cast).forEach(sheet -> {
            for (int i = 0; i < sheet.getTabCount(); i++) {
                if (descendants(sheet.getComponent(sheet.getTabAt(i))).anyMatch(component -> component == button)) {
                    sheet.setSelectedIndex(i);
                    break;
                }
            }
        });
        return button;
    }

    private static Stream<Component> descendants(Component parent) {
        Stream<Component> children = parent instanceof TabSheet sheet
                ? java.util.stream.IntStream.range(0, sheet.getTabCount()).mapToObj(sheet::getTabAt).map(sheet::getComponent)
                : parent.getChildren();
        return Stream.concat(Stream.of(parent), children.flatMap(MediaReferenceFormTest::descendants));
    }
}
