package com.grosshaeuser.olmp.ui.internal_ui.shared.inventory;

import com.grosshaeuser.olmp.inventory.dto.media.MediaCreateDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaTypeSummaryDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherCreateDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherEditDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.service.MediaReferenceService;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.grosshaeuser.olmp.ui.internal_ui.shared.inventory.MediaFormSupport.*;

class MediaCatalogueForm<T extends MediaCreateDto> extends Composite<Div> implements Structurable {

    private final BeanValidationBinder<T> binder;
    private final List<MediaTypeSummaryDto> mediaTypes;
    private final MediaReferenceService referenceService;
    private final List<PublisherSummaryDto> publisherOptions;
    private final ListDataProvider<PublisherSummaryDto> publisherProvider;
    private final InlineReferenceForm<PublisherCreateDto> publisherForm;
    private final ComboBox<MediaTypeSummaryDto> mediaType = new ComboBox<>("Media type");
    private final ComboBox<PublisherSummaryDto> publisher = new ComboBox<>("Publisher");
    private final TextField title = new TextField("Title");
    private final TextField subtitle = new TextField("Subtitle");
    private final TextField edition = new TextField("Edition");
    private final IntegerField publicationYear = new IntegerField("Publication year");
    private final TextField languageCode = new TextField("Language code");
    private final TextField coverImageFileName = new TextField("Cover image filename");
    private final TextArea description = new TextArea("Description");

    MediaCatalogueForm(Class<T> dtoType, List<MediaTypeSummaryDto> mediaTypes,
                       List<PublisherSummaryDto> publishers, MediaReferenceService referenceService) {
        this.mediaTypes = List.copyOf(mediaTypes);
        this.referenceService = Objects.requireNonNull(referenceService, "referenceService");
        publisherOptions = new ArrayList<>(publishers);
        publisherProvider = new ListDataProvider<>(publisherOptions);
        binder = new BeanValidationBinder<>(dtoType);
        configureFields();
        configureBindings();
        publisherForm = createPublisherForm();

        FormLayout catalogue = responsiveForm();
        Button createPublisher = new Button(VaadinIcon.PLUS.create(), _ -> publisherForm.open());
        createPublisher.setTooltipText("Create new publisher");
        createPublisher.setAriaLabel("Create new publisher");
        Button editPublisher = editButton(publisher, "publisher", this::editPublisher);
        publisher.addValueChangeListener(_ -> publisherForm.reset());
        catalogue.add(title, subtitle, mediaType, referenceSection(publisher, editPublisher, createPublisher, publisherForm),
                edition, publicationYear, languageCode, coverImageFileName, description);
        catalogue.setColspan(title, 2);
        catalogue.setColspan(subtitle, 2);
        catalogue.setColspan(description, 2);
        getContent().add(createSection("Catalogue", "Title, publication information and description.", catalogue));
        getContent().setWidthFull();
    }

    void read(T source) {
        binder.readBean(source);
        publisherForm.reset();
    }

    boolean write(T target) {
        return binder.writeBeanIfValid(target);
    }

    String title() {
        return title.getValue();
    }

    private void editPublisher(PublisherSummaryDto selected) {
        PublisherEditDto dto = new PublisherEditDto();
        dto.setId(selected.id());
        dto.setName(selected.name());
        dto.setWebsite(selected.website());
        publisherForm.openForEdit(dto);
    }

    private void configureFields() {
        mediaType.setItems(mediaTypes);
        mediaType.setItemLabelGenerator(type -> type.name() + " (" + type.code() + ")");
        publisher.setItems(publisherProvider);
        publisher.setItemLabelGenerator(PublisherSummaryDto::name);
        publisher.setClearButtonVisible(true);
        title.setMaxLength(500);
        subtitle.setMaxLength(500);
        edition.setMaxLength(100);
        languageCode.setMaxLength(10);
        languageCode.setHelperText("For example: en or de");
        coverImageFileName.setMaxLength(100);
        publicationYear.setMin(1);
        publicationYear.setMax(9999);
        description.setMinHeight("8rem");
        List.<HasSize>of(mediaType, publisher, title, subtitle, edition, publicationYear, languageCode, coverImageFileName, description).forEach(HasSize::setWidthFull);
    }

    private void configureBindings() {
        binder.forField(title).asRequired("Title is required").bind("title");
        binder.forField(subtitle).withNullRepresentation("").bind("subtitle");
        binder.forField(edition).withNullRepresentation("").bind("edition");
        binder.forField(languageCode).withNullRepresentation("").bind("languageCode");
        binder.forField(coverImageFileName).withNullRepresentation("").bind("coverImageFileName");
        binder.forField(description).withNullRepresentation("").bind("description");
        binder.forField(mediaType).asRequired("Media type is required")
                .withConverter(value -> value == null ? null : value.id(),
                        id -> lookup(mediaTypes, MediaTypeSummaryDto::id, id, "Media type"))
                .bind("mediaTypeId");
        binder.forField(publisher)
                .withConverter(value -> value == null ? null : value.id(),
                        id -> lookup(publisherOptions, PublisherSummaryDto::id, id, "Publisher"))
                .bind("publisherId");
        binder.forField(publicationYear)
                .withValidator(value -> value == null || value >= 1 && value <= 9999,
                        "Publication year must be between 1 and 9999")
                .withConverter(value -> value == null ? null : value.shortValue(),
                        value -> value == null ? null : value.intValue())
                .bind("publicationYear");
    }

    private InlineReferenceForm<PublisherCreateDto> createPublisherForm() {
        TextField name = new TextField("Publisher name");
        name.setMaxLength(255);
        TextField website = new TextField("Publisher URL");
        website.setMaxLength(500);
        website.setPlaceholder("https://example.org");
        var form = new InlineReferenceForm<>(PublisherCreateDto.class, PublisherCreateDto::new, dto -> {
            PublisherSummaryDto saved;
            if (dto instanceof PublisherEditDto edit) {
                saved = Objects.requireNonNull(referenceService.updatePublisher(edit), "Updated publisher");
                publisherOptions.replaceAll(option -> option.id().equals(saved.id()) ? saved : option);
            } else {
                saved = Objects.requireNonNull(referenceService.createPublisher(dto), "Created publisher");
                publisherOptions.add(saved);
            }
            publisherProvider.refreshAll();
            publisher.setValue(saved);
        }, "publisher", name, website);
        form.binder.forField(name).withConverter(String::strip, text -> text == null ? "" : text)
                .asRequired("Publisher name is required").bind("name");
        form.binder.forField(website).withConverter(String::strip, text -> text == null ? "" : text).bind("website");
        form.reset();
        return form;
    }
}
