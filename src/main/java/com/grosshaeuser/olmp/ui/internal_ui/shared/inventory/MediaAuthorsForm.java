package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorCreateDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorEditDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaAuthorWriteDto;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.grosshaeuser.olmp.ui.internal_ui.shared.inventory.MediaFormSupport.*;

class MediaAuthorsForm extends Composite<Div> implements Structurable {

    private final MediaReferenceService referenceService;
    private final List<AuthorSummaryDto> authorOptions;
    private final ListDataProvider<AuthorSummaryDto> authorProvider;
    private final VerticalLayout authorRows = new VerticalLayout();
    private final List<AuthorRow> authors = new ArrayList<>();
    private final Span validationMessage = new Span();

    MediaAuthorsForm(List<AuthorSummaryDto> authorOptions, MediaReferenceService referenceService) {
        this.referenceService = Objects.requireNonNull(referenceService, "referenceService");
        this.authorOptions = new ArrayList<>(authorOptions);
        authorProvider = new ListDataProvider<>(this.authorOptions);
        authorRows.setPadding(false);
        Button addAuthor = new Button("Add author", _ -> addAuthor(new MediaAuthorWriteDto()));
        validationMessage.getElement().setAttribute("role", "alert");
        validationMessage.getStyle().set("color", "var(--vaadin-text-color-error, #b71c1c)");
        validationMessage.setVisible(false);
        getContent().add(createSection("Authors and contributors", "Assign authors, roles and display order.",
                authorRows, addAuthor, validationMessage));
        getContent().setWidthFull();
    }

    void read(List<MediaAuthorWriteDto> source) {
        authors.clear();
        authorRows.removeAll();
        source.forEach(this::addAuthor);
        validationMessage.setVisible(false);
    }

    boolean write(List<MediaAuthorWriteDto> target) {
        validationMessage.setVisible(false);
        List<MediaAuthorWriteDto> values = new ArrayList<>();
        boolean valid = true;
        for (AuthorRow row : authors) {
            MediaAuthorWriteDto dto = new MediaAuthorWriteDto();
            valid = row.binder.writeBeanIfValid(dto) && valid;
            values.add(dto);
        }
        if (!valid) {
            return false;
        }
        if (hasDuplicateAuthors(values)) {
            showValidationMessage("An author can only be assigned once for the same role.");
            return false;
        }
        target.addAll(values);
        return true;
    }

    private void addAuthor(MediaAuthorWriteDto dto) {
        AuthorRow row = new AuthorRow(dto);
        authors.add(row);
        authorRows.add(row.layout);
    }

    private InlineReferenceForm<AuthorCreateDto> createAuthorForm(ComboBox<AuthorSummaryDto> author) {
        TextField firstName = new TextField("First name");
        firstName.setMaxLength(255);
        TextField lastName = new TextField("Last name");
        lastName.setMaxLength(255);
        var form = new InlineReferenceForm<>(AuthorCreateDto.class, AuthorCreateDto::new, dto -> {
            AuthorSummaryDto saved;
            List<AuthorRow> selectedRows = List.of();
            if (dto instanceof AuthorEditDto edit) {
                saved = Objects.requireNonNull(referenceService.updateAuthor(edit), "Updated author");
                selectedRows = authors.stream().filter(row -> row.author.getValue() != null
                        && row.author.getValue().id().equals(saved.id())).toList();
                authorOptions.replaceAll(option -> option.id().equals(saved.id()) ? saved : option);
            } else {
                saved = Objects.requireNonNull(referenceService.createAuthor(dto), "Created author");
                authorOptions.add(saved);
            }
            authorProvider.refreshAll();
            selectedRows.forEach(row -> row.author.setValue(saved));
            author.setValue(saved);
        }, "author", firstName, lastName);
        form.binder.forField(firstName).withConverter(String::strip, text -> text == null ? "" : text).bind("firstName");
        form.binder.forField(lastName).withConverter(String::strip, text -> text == null ? "" : text)
                .asRequired("Last name is required").bind("lastName");
        form.reset();
        return form;
    }

    private void showValidationMessage(String message) {
        validationMessage.setText(message);
        validationMessage.setVisible(true);
    }

    private static boolean hasDuplicateAuthors(List<MediaAuthorWriteDto> rows) {
        record Key(Long authorId, String role) {
        }
        var seen = new HashSet<Key>();
        return rows.stream().anyMatch(row -> !seen.add(new Key(row.getAuthorId(), row.getRole())));
    }

    static String authorName(AuthorSummaryDto author) {
        return author.firstName() == null || author.firstName().isBlank() ? author.lastName()
                : author.firstName() + " " + author.lastName();
    }

    private class AuthorRow {
        private final BeanValidationBinder<MediaAuthorWriteDto> binder = new BeanValidationBinder<>(MediaAuthorWriteDto.class);
        private final HorizontalLayout layout = new HorizontalLayout();
        private final ComboBox<AuthorSummaryDto> author = new ComboBox<>("Author");

        AuthorRow(MediaAuthorWriteDto dto) {
            author.setItems(authorProvider);
            author.setItemLabelGenerator(MediaAuthorsForm::authorName);
            var creation = createAuthorForm(author);
            Button createAuthor = new Button(VaadinIcon.PLUS.create(), _ -> creation.open());
            createAuthor.setTooltipText("Create new author");
            createAuthor.setAriaLabel("Create new author");
            Button editAuthor = editButton(author, "author", selected -> {
                AuthorEditDto edit = new AuthorEditDto();
                edit.setId(selected.id());
                edit.setFirstName(selected.firstName());
                edit.setLastName(selected.lastName());
                creation.openForEdit(edit);
            });
            author.addValueChangeListener(_ -> creation.reset());
            VerticalLayout authorSection = referenceSection(author, editAuthor, createAuthor, creation);
            authorSection.getStyle().set("margin-inline-end", "var(--lumo-space-l)");
            author.setMinWidth("0");
            TextField role = new TextField("Role");
            role.setMaxLength(50);
            role.setHelperText("For example: AUTHOR, EDITOR or TRANSLATOR");
            IntegerField order = new IntegerField("Display order");
            order.setMin(0);
            order.setMax(Short.MAX_VALUE);
            order.setStepButtonsVisible(true);
            Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                authors.remove(this);
                authorRows.remove(layout);
            });
            remove.addThemeVariants(ButtonVariant.LUMO_ERROR);
            remove.setAriaLabel("Remove author");
            binder.forField(author).asRequired("Choose an author")
                    .withConverter(value -> value == null ? null : value.id(),
                            id -> lookup(authorOptions, AuthorSummaryDto::id, id, "Author"))
                    .bind("authorId");
            binder.forField(role).asRequired("Role is required")
                    .withConverter(String::strip, value -> value == null ? "" : value).bind("role");
            binder.forField(order).asRequired("Display order is required")
                    .withValidator(value -> value >= 0 && value <= Short.MAX_VALUE,
                            "Display order must be between 0 and " + Short.MAX_VALUE)
                    .withConverter(Integer::shortValue, Short::intValue).bind("sortOrder");
            List.<HasSize>of(author).forEach(HasSize::setWidthFull);
            layout.setSizeFull();
            layout.setAlignItems(FlexComponent.Alignment.BASELINE);
            layout.add(authorSection, role, order, remove);
            layout.setFlexGrow(1, authorSection);
            layout.setFlexGrow(1, role);
            binder.readBean(dto);
        }
    }
}
