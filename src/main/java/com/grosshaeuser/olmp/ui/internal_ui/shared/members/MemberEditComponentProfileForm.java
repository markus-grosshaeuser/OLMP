package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.ui.internal_ui.shared.members.MemberEditComponent.ProfileDataChangedCallback;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Objects;
import java.util.Optional;

class MemberEditComponentProfileForm extends HorizontalLayout {
    private final TextField userIdField = new TextField("ID");
    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final DatePicker birthDateField = new DatePicker("Birth Date");
    private final DateTimePicker createdAtField = new DateTimePicker("Created At");
    private final DateTimePicker updatedAtField = new DateTimePicker("Updated At");

    private final BeanValidationBinder<MemberEditDTO> primaryBinder = new BeanValidationBinder<>(MemberEditDTO.class);
    private final ProfileDataChangedCallback changeCallback;
    private MemberEditDTO snapshot;


    MemberEditComponentProfileForm(MemberEditDTO dto, ProfileDataChangedCallback changeCallback) {
        this.changeCallback = changeCallback;
        configureBinder();
        loadMember(dto);
        add(createFormLayout());
    }


    void loadMember(MemberEditDTO dto) {
        primaryBinder.setBean(dto);
        snapshot = createComparableSnapshot(dto);
    }

    void updateSnapshot() {
        getFormDataObject().map(this::createComparableSnapshot);
    }

    private void configureBinder() {
        primaryBinder.forField(userIdField).bindReadOnly(dto -> String.valueOf(dto.getId()));
        primaryBinder.forField(firstNameField).bind(MemberEditDTO::getFirstName, MemberEditDTO::setFirstName);
        primaryBinder.forField(lastNameField).bind(MemberEditDTO::getLastName, MemberEditDTO::setLastName);
        primaryBinder.forField(birthDateField).bind(MemberEditDTO::getBirthDate, MemberEditDTO::setBirthDate);
        primaryBinder.forField(createdAtField).bindReadOnly(MemberEditDTO::getCreatedAt);
        primaryBinder.forField(updatedAtField).bindReadOnly(MemberEditDTO::getUpdatedAt);

        primaryBinder.addValueChangeListener(_ -> handleDataChange());
    }

    private void handleDataChange() {
        changeCallback.profileDataChanged(getFormDataObject()
                .map(profileData -> hasChanges(createComparableSnapshot(profileData)))
                .orElse(false));
    }

    private FormLayout createFormLayout() {
        FormLayout layout = new FormLayout();
        layout.setAutoResponsive(true);
        layout.setExpandColumns(true);
        layout.setExpandFields(true);
        layout.addFormRow(userIdField);
        layout.addFormRow(firstNameField, lastNameField);
        layout.addFormRow(birthDateField);
        layout.addFormRow(createdAtField, updatedAtField);
        layout.setSizeFull();
        return layout;
    }

    private MemberEditDTO createComparableSnapshot(MemberEditDTO member) {
        return member.toBuilder().build();
    }

    Optional<MemberEditDTO> getFormDataObject() {
        MemberEditDTO formData = snapshot.toBuilder().build();
        try {
            primaryBinder.writeBean(formData);
            return Optional.of(createComparableSnapshot(formData));
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    private boolean hasChanges(MemberEditDTO dto) {
        return !Objects.equals(dto.getFirstName(), snapshot.getFirstName())
                || !Objects.equals(dto.getLastName(), snapshot.getLastName())
                || !Objects.equals(dto.getBirthDate(), snapshot.getBirthDate())
                || !Objects.equals(dto.isWithContactInformation(), snapshot.isWithContactInformation())
                || !Objects.equals(dto.isWithMembershipDetails(), snapshot.isWithMembershipDetails());
    }
}
