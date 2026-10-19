package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

class MemberEditComponentMembershipForm extends Tab {
    private final TextField membershipNumberField = new TextField("Membership Number");
    private final Select<String> membershipTypeSelectField = new Select<>("Membership Type");
    private final Select<String> membershipStatusSelectField = new Select<>("Membership Status");
    private final DatePicker startDateField = new DatePicker("Start Date");
    private final DatePicker expirationDateField = new DatePicker("Expiration Date");
    private final DatePicker renewalDateField = new DatePicker("Renewal Date");
    private final DatePicker cancellationDateField = new DatePicker("Cancellation Date");

    private final HashMap<String, Integer> membershipPeriodsInMonths;

    private final BeanValidationBinder<MembershipEditDTO> binder = new BeanValidationBinder<>(MembershipEditDTO.class);

    private MembershipEditDTO snapshot;
    private final MemberEditComponent.WithMembershipDetailsCallback callback;

    MemberEditComponentMembershipForm(
            MembershipEditDTO dto,
            Set<String> membershipTypes,
            Set<String> membershipStatuses,
            HashMap<String, Integer> membershipPeriodsInMonths,
            MemberEditComponent.WithMembershipDetailsCallback callback
    ) {
        this.callback = callback;
        this.membershipPeriodsInMonths = membershipPeriodsInMonths;

        this.membershipTypeSelectField.setItems(membershipTypes);
        this.membershipTypeSelectField.setEmptySelectionAllowed(false);
        this.membershipTypeSelectField.addValueChangeListener(_ -> updateExpirationDate());

        this.membershipStatusSelectField.setItems(membershipStatuses);
        this.membershipStatusSelectField.setEmptySelectionAllowed(false);

        configureBinder();
        loadMembershipDetails(dto);
        add(createFormLayout());
    }

    private void updateExpirationDate() {
        LocalDate startDate = this.startDateField.getValue();
        if (startDate == null)
            return;
        String membershipType = this.membershipTypeSelectField.getValue();
        if (membershipType == null)
            return;
        if (membershipPeriodsInMonths.containsKey(membershipType)) {
            this.expirationDateField.setValue(startDate.plusMonths(membershipPeriodsInMonths.get(membershipType)));
        }
    }

    void loadMembershipDetails(MembershipEditDTO dto) {
        binder.readBean(dto);
        snapshot = createComparableSnapshot(dto);
        handleDataChange();
    }

    private void configureBinder() {
        binder.forField(membershipNumberField).bindReadOnly(MembershipEditDTO::getMembershipNumber);
        binder.forField(membershipTypeSelectField).bind(MembershipEditDTO::getMembershipType, MembershipEditDTO::setMembershipType);
        binder.forField(membershipStatusSelectField).bind(MembershipEditDTO::getMembershipStatus, MembershipEditDTO::setMembershipStatus);
        binder.forField(startDateField).bindReadOnly(MembershipEditDTO::getStartDate);
        binder.forField(expirationDateField).bind(MembershipEditDTO::getExpirationDate, MembershipEditDTO::setExpirationDate);
        binder.forField(renewalDateField).bind(MembershipEditDTO::getRenewalDate, MembershipEditDTO::setRenewalDate);
        binder.forField(cancellationDateField).bind(MembershipEditDTO::getCancellationDate, MembershipEditDTO::setCancellationDate);

        binder.addValueChangeListener(_ -> handleDataChange());
    }

    private void handleDataChange() {
        callback.withMembership(getFormDataObject()
                .map(formData -> hasChanges(createComparableSnapshot(formData)))
                .orElse(false)
        );
    }

    private FormLayout createFormLayout() {
        FormLayout layout = new FormLayout();
        layout.setAutoResponsive(true);
        layout.setExpandColumns(true);
        layout.setExpandFields(true);

        layout.addFormRow(membershipNumberField);
        layout.addFormRow(membershipTypeSelectField, membershipStatusSelectField);
        layout.addFormRow(startDateField, expirationDateField);
        layout.addFormRow(renewalDateField, cancellationDateField);
        layout.setColspan(membershipNumberField, 2);

        layout.setSizeFull();

        return layout;
    }

    private MembershipEditDTO createComparableSnapshot(MembershipEditDTO membership) {
        return membership.toBuilder().build();
    }


    Optional<MembershipEditDTO> getFormDataObject() {
        MembershipEditDTO formData = snapshot.toBuilder().build();
        try {
            binder.writeBean(formData);
            return Optional.of(createComparableSnapshot(formData));
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    private boolean hasChanges(MembershipEditDTO membership) {
        return !Objects.equals(membership.getMembershipType(), snapshot.getMembershipType())
                || !Objects.equals(membership.getMembershipStatus(), snapshot.getMembershipStatus())
                || !Objects.equals(membership.getExpirationDate(), snapshot.getExpirationDate())
                || !Objects.equals(membership.getRenewalDate(), snapshot.getRenewalDate())
                || !Objects.equals(membership.getCancellationDate(), snapshot.getCancellationDate());
    }
}
