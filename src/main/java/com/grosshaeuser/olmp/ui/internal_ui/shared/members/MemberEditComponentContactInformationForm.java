package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberContactInformationEditDTO;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Objects;
import java.util.Optional;

class MemberEditComponentContactInformationForm extends Tab {
    private final TextField phoneNumberField = new TextField("Phone Number");
    private final TextField streetField = new TextField("Street");
    private final TextField houseNumberField = new TextField("House Number");
    private final TextField postalCodeField = new TextField("Postal Code");
    private final TextField cityField = new TextField("City");
    private final TextField countryField = new TextField("Country");

    private final BeanValidationBinder<MemberContactInformationEditDTO> binder = new BeanValidationBinder<>(MemberContactInformationEditDTO.class);

    private MemberContactInformationEditDTO snapshot;
    private final MemberEditComponent.WithContactInformationCallback callback;

    MemberEditComponentContactInformationForm(MemberContactInformationEditDTO dto, MemberEditComponent.WithContactInformationCallback callback) {
        this.callback = callback;

        configureBinder();
        loadMembershipContactInformation(dto);
        add(createFormLayout());
    }

    void loadMembershipContactInformation(MemberContactInformationEditDTO dto) {
        binder.readBean(dto);
        snapshot = createComparableSnapshot(dto);
        handleDataChange();
    }

    private void configureBinder() {
        binder.forField(phoneNumberField).bind(MemberContactInformationEditDTO::getPhoneNumber, MemberContactInformationEditDTO::setPhoneNumber);
        binder.forField(streetField).bind(MemberContactInformationEditDTO::getStreet, MemberContactInformationEditDTO::setStreet);
        binder.forField(houseNumberField).bind(MemberContactInformationEditDTO::getHouseNumber, MemberContactInformationEditDTO::setHouseNumber);
        binder.forField(postalCodeField).bind(MemberContactInformationEditDTO::getPostalCode, MemberContactInformationEditDTO::setPostalCode);
        binder.forField(cityField).bind(MemberContactInformationEditDTO::getCity, MemberContactInformationEditDTO::setCity);
        binder.forField(countryField).bind(MemberContactInformationEditDTO::getCountry, MemberContactInformationEditDTO::setCountry);

        binder.addValueChangeListener(_ -> handleDataChange());
    }

    private void handleDataChange() {
        callback.withContactInformation(getFormDataObject()
                .map(formData -> hasChanges(createComparableSnapshot(formData)))
                .orElse(false)
        );
    }

    private FormLayout createFormLayout() {
        FormLayout layout = new FormLayout();
        layout.setAutoResponsive(true);
        layout.setExpandColumns(true);
        layout.setExpandFields(true);

        layout.addFormRow(phoneNumberField);
        layout.addFormRow(streetField, houseNumberField);
        layout.addFormRow(postalCodeField, cityField);
        layout.addFormRow(countryField);
        layout.setSizeFull();

        return layout;
    }

    private MemberContactInformationEditDTO createComparableSnapshot(MemberContactInformationEditDTO contactInformation) {
        return contactInformation.toBuilder().build();
    }

    Optional<MemberContactInformationEditDTO> getFormDataObject() {
        MemberContactInformationEditDTO formData = snapshot.toBuilder().build();
        try {
            binder.writeBean(formData);
            return Optional.of(createComparableSnapshot(formData));
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    private boolean hasChanges(MemberContactInformationEditDTO contactInformation) {
        return !Objects.equals(contactInformation.getPhoneNumber(), snapshot.getPhoneNumber())
                || !Objects.equals(contactInformation.getStreet(), snapshot.getStreet())
                || !Objects.equals(contactInformation.getHouseNumber(), snapshot.getHouseNumber())
                || !Objects.equals(contactInformation.getPostalCode(), snapshot.getPostalCode())
                || !Objects.equals(contactInformation.getCity(), snapshot.getCity())
                || !Objects.equals(contactInformation.getCountry(), snapshot.getCountry());
    }
}
