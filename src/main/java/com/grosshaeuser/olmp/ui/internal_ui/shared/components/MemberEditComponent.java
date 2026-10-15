package com.grosshaeuser.olmp.ui.internal_ui.shared.components;

import com.grosshaeuser.olmp.members.dto.MemberAccountEditDTO;
import com.grosshaeuser.olmp.members.dto.MemberContactInformationEditDTO;
import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.Set;

public class MemberEditComponent extends Composite<VerticalLayout> implements Structurable, Delegatable {

    @FunctionalInterface
    interface WithContactInformationCallback {
        void withContactInformation(boolean value);
    }

    @FunctionalInterface
    interface WithMembershipDetailsCallback {
        void withMembership(boolean value);
    }

    private final UpdateCallback<MemberEditDTO> updateCallback;
    private final DeleteCallback<MemberEditDTO> deleteCallback;
    private final ErrorCallback errorCallback;

    private final BeanValidationBinder<MemberEditDTO> primaryBinder = new BeanValidationBinder<>(MemberEditDTO.class);

    private final MemberAccountEditForm memberAccountEditForm;
    private final MemberContactInformationEditForm memberContactInformationEditForm;
    private final MembershipDetailsEditForm membershipDetailsEditForm;

    private final Button saveButton = new Button("Save");
    private final Button deleteButton = new Button("Delete");

    private final TextField userIdField = new TextField("ID");
    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final DatePicker birthDateField = new DatePicker("Birth Date");
    private final TextField createdAtField = new TextField("Created At");
    private final TextField updatedAtField = new TextField("Updated At");

    private MemberEditDTO snapshot;


    public MemberEditComponent(
            MemberEditDTO memberEditDTO,
            Set<String> membershipTypes,
            Set<String> membershipStatuses,
            UpdateCallback<MemberEditDTO> updateCallback,
            DeleteCallback<MemberEditDTO> deleteCallback,
            ErrorCallback errorCallback,
            PasswordProperties passwordProperties
    ) {
        this.updateCallback = updateCallback;
        this.deleteCallback = deleteCallback;
        this.errorCallback = errorCallback;

        this.snapshot = createComparableSnapshot(memberEditDTO);

        primaryBinder.setBean(memberEditDTO);
        configureBinder();
        configureActions();

        memberAccountEditForm = new MemberAccountEditForm(memberEditDTO.getAccount(), passwordProperties);
        memberContactInformationEditForm = new MemberContactInformationEditForm(memberEditDTO.getContact(), memberEditDTO::setWithContactInformation);
        membershipDetailsEditForm = new MembershipDetailsEditForm(memberEditDTO.getMembership(), membershipTypes, membershipStatuses, memberEditDTO::setWithMembershipDetails);

        configureRootLayout();

        getContent().add(
                createComponentHeader("Member details", "Review profile information, account state, contact information, and membership details."),
                createFormElementBase(),
                createActionBar(new Button[]{saveButton}, new Button[]{}, new Button[]{deleteButton})
        );
    }

    private void configureRootLayout() {
        VerticalLayout layout = getContent();
        layout.addClassName("member-edit-component");
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
    }

    private HorizontalLayout createFormElementBase() {
        FormLayout profile = createProfileForm();

        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Account", memberAccountEditForm);
        tabSheet.add("Contact Information", memberContactInformationEditForm);
        tabSheet.add("Membership Details", membershipDetailsEditForm);

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.add(profile, tabSheet);
        return layout;
    }

    private FormLayout createProfileForm() {
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

    private void configureActions() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        saveButton.setEnabled(false);
        saveButton.addClickListener(_ -> save());

        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(_ -> confirmDelete());
    }

    private void configureBinder() {
        primaryBinder.forField(userIdField).bindReadOnly(MemberEditDTO::idAsString);
        primaryBinder.forField(firstNameField).bind(MemberEditDTO::getFirstName, MemberEditDTO::setFirstName);
        primaryBinder.forField(lastNameField).bind(MemberEditDTO::getLastName, MemberEditDTO::setLastName);
        primaryBinder.forField(birthDateField).bind(MemberEditDTO::getBirthDate, MemberEditDTO::setBirthDate);
        primaryBinder.forField(createdAtField).bindReadOnly(MemberEditDTO::getCreatedAt);
        primaryBinder.forField(updatedAtField).bindReadOnly(MemberEditDTO::getUpdatedAt);
    }

    private MemberEditDTO createComparableSnapshot(MemberEditDTO member) {
        return member.toBuilder().build();
    }

    private void handleDataChange() {
        saveButton.setEnabled(primaryBinder.getBean() != null && !createComparableSnapshot(primaryBinder.getBean()).equals(snapshot));
    }

    public Optional<MemberEditDTO> getFormDataObject() {
        if (primaryBinder.getBean() == null) {
            throw new IllegalStateException("No member data object set");
        }
        if (!primaryBinder.validate().isOk()) {
            return Optional.empty();
        }

        try {
            primaryBinder.writeBean(primaryBinder.getBean());
            return Optional.of(primaryBinder.getBean());
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    public void save() {
        getFormDataObject().ifPresent(dto -> {
            try {
                dto.setAccount(memberAccountEditForm.getFormDataObject().orElse(null));
                dto.setMembership(membershipDetailsEditForm.getFormDataObject().orElse(null));
                dto.setContact(memberContactInformationEditForm.getFormDataObject().orElse(null));
                MemberEditDTO savedUser = updateCallback.update(dto);
                primaryBinder.setBean(savedUser);
                this.snapshot = createComparableSnapshot(savedUser);
                memberAccountEditForm.setFormDataObject(savedUser.getAccount());
                membershipDetailsEditForm.setFormDataObject(savedUser.getMembership());
                memberContactInformationEditForm.setFormDataObject(savedUser.getContact());
                handleDataChange();
            } catch (RuntimeException e) {
                errorCallback.handleException(e);
            } finally {
                handleDataChange();
            }
        });
    }

    private void confirmDelete() {
        MemberEditDTO user = primaryBinder.getBean();
        if (user == null) {
            throw new IllegalStateException("No user data object set");
        }

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete user account?");
        dialog.setText("Are you sure you want to delete the account of \"" + user.getFirstName() + " " + user.getLastName() + "\"? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(_ -> delete(user));
        dialog.open();
    }

    private void delete(MemberEditDTO user) {
        try {
            deleteCallback.delete(user);
        } catch (RuntimeException e) {
            errorCallback.handleException(e);
        }
    }





    @Getter
    @Setter
    private static class MemberAccountEditForm extends Tab {

        private TextField accountIdField = new TextField("Account ID");
        private TextField emailField = new TextField("Email");
        private PasswordField newPasswordField = new PasswordField("New Password");
        private Checkbox verifiedField = new Checkbox("Verified");
        private Checkbox enabledField = new Checkbox("Enabled");
        private Checkbox lockedField = new Checkbox("Locked");
        private TextField failedLoginAttemptsField = new TextField("Failed Login Attempts");
        private TextField lockoutUntilField = new TextField("Lockout Until");
        private TextField accountCreatedAtField = new TextField("Account Created At");
        private TextField accountUpdatedAtField = new TextField("Account Updated At");
        private TextField accountLastAccessedAtField = new TextField("Account Last Accessed At");

        private final BeanValidationBinder<MemberAccountEditDTO> binder = new BeanValidationBinder<>(MemberAccountEditDTO.class);
        private final PasswordProperties passwordProperties;
        private MemberAccountEditDTO snapshot;

        private MemberAccountEditForm(MemberAccountEditDTO dto, PasswordProperties passwordProperties) {
            binder.setBean(dto);
            this.passwordProperties = passwordProperties;
            this.snapshot = createComparableSnapshot(binder.getBean());
            configureFields();
            configureBinder();
            add(createFormLayout());
        }

        private FormLayout createFormLayout() {
            FormLayout layout = new FormLayout();
            layout.setAutoResponsive(true);
            layout.setExpandColumns(true);
            layout.setExpandFields(true);

            Div checkboxes =  new Div(enabledField, verifiedField, lockedField);
            layout.addFormRow(accountIdField, checkboxes);
            layout.addFormRow(emailField, newPasswordField);
            layout.addFormRow(failedLoginAttemptsField, lockoutUntilField, accountLastAccessedAtField);
            layout.addFormRow(accountCreatedAtField, accountUpdatedAtField);
            layout.setColspan(checkboxes, 2);
            layout.setColspan(emailField, 2);
            layout.setSizeFull();

            return layout;
        }

        private void configureFields() {
            newPasswordField.setTooltipText(passwordProperties.getPasswordRequirementDescription());
            newPasswordField.setRevealButtonVisible(true);
            newPasswordField.setWidthFull();
            newPasswordField.setRequiredIndicatorVisible(true);
        }

        private void configureBinder() {
            binder.forField(accountIdField).bindReadOnly(MemberAccountEditDTO::idAsString);
            binder.forField(emailField).bind(MemberAccountEditDTO::getEmail, MemberAccountEditDTO::setEmail);
            binder.forField(newPasswordField)
                    .withValidator(
                            value -> value == null || value.isBlank() || value.matches(passwordProperties.getRegExPattern()),
                            passwordProperties.getPasswordRequirementDescription()
                    )
                    .bind(MemberAccountEditDTO::getNewPassword, MemberAccountEditDTO::setNewPassword);
            binder.forField(verifiedField).bind(MemberAccountEditDTO::isVerified, MemberAccountEditDTO::setVerified);
            binder.forField(enabledField).bind(MemberAccountEditDTO::isEnabled, MemberAccountEditDTO::setEnabled);
            binder.forField(lockedField).bind(MemberAccountEditDTO::isLocked, MemberAccountEditDTO::setLocked);
            binder.forField(failedLoginAttemptsField).bindReadOnly(MemberAccountEditDTO::failedLoginAttemptsAsString);
            binder.forField(lockoutUntilField).bindReadOnly(MemberAccountEditDTO::getLockoutUntil);
            binder.forField(accountCreatedAtField).bindReadOnly(MemberAccountEditDTO::getAccountCreatedAt);
            binder.forField(accountUpdatedAtField).bindReadOnly(MemberAccountEditDTO::getAccountUpdatedAt);
            binder.forField(accountLastAccessedAtField).bindReadOnly(MemberAccountEditDTO::getAccountLastAccessedAt);
        }

        private MemberAccountEditDTO createComparableSnapshot(MemberAccountEditDTO member) {
            MemberAccountEditDTO snapshot = member.toBuilder().build();
            if (snapshot.getNewPassword() != null && snapshot.getNewPassword().isBlank()) {
                snapshot.setNewPassword(null);
            }
            return snapshot;
        }

        private void setFormDataObject(MemberAccountEditDTO dto) {
            binder.setBean(dto);
            this.snapshot = createComparableSnapshot(dto);
        }

        private Optional<MemberAccountEditDTO> getFormDataObject() {
            if (binder.getBean() == null || binder.getBean().equals(snapshot)) {
                return Optional.empty();
            }
            if (!binder.validate().isOk()) {
                throw new RuntimeException("Account form data is invalid");
            }
            try {
                binder.writeBean(binder.getBean());
                return Optional.of(binder.getBean());
            } catch (ValidationException e) {
                return Optional.empty();
            }
        }
    }



    @Getter
    @Setter
    private static class MemberContactInformationEditForm extends Tab {
        private TextField phoneNumberField = new TextField("Phone Number");
        private TextField streetField = new TextField("Street");
        private TextField houseNumberField = new TextField("House Number");
        private TextField postalCodeField = new TextField("Postal Code");
        private TextField cityField = new TextField("City");
        private TextField countryField = new TextField("Country");

        private final BeanValidationBinder<MemberContactInformationEditDTO> binder = new BeanValidationBinder<>(MemberContactInformationEditDTO.class);
        private final MemberContactInformationEditDTO dto;
        private MemberContactInformationEditDTO snapshot;
        private final WithContactInformationCallback callback;

        private MemberContactInformationEditForm(MemberContactInformationEditDTO dto, WithContactInformationCallback callback) {
            this.dto = dto;
            binder.setBean(dto);
            this.snapshot = createComparableSnapshot(binder.getBean());

            this.callback = callback;
            registerDirtyStateListeners();
            if (this.dto != null && this.dto.equals(snapshot)) {
                callback.withContactInformation(false);
            }

            configureBinder();
            add(createFormLayout());
        }

        private void configureBinder() {
            binder.forField(phoneNumberField).bind(MemberContactInformationEditDTO::getPhoneNumber, MemberContactInformationEditDTO::setPhoneNumber);
            binder.forField(streetField).bind(MemberContactInformationEditDTO::getStreet, MemberContactInformationEditDTO::setStreet);
            binder.forField(houseNumberField).bind(MemberContactInformationEditDTO::getHouseNumber, MemberContactInformationEditDTO::setHouseNumber);
            binder.forField(postalCodeField).bind(MemberContactInformationEditDTO::getPostalCode, MemberContactInformationEditDTO::setPostalCode);
            binder.forField(cityField).bind(MemberContactInformationEditDTO::getCity, MemberContactInformationEditDTO::setCity);
            binder.forField(countryField).bind(MemberContactInformationEditDTO::getCountry, MemberContactInformationEditDTO::setCountry);
        }

        private void registerDirtyStateListeners() {
            phoneNumberField.addValueChangeListener(_ -> handleDataChange());
            streetField.addValueChangeListener(_ -> handleDataChange());
            houseNumberField.addValueChangeListener(_ -> handleDataChange());
            postalCodeField.addValueChangeListener(_ -> handleDataChange());
            cityField.addValueChangeListener(_ -> handleDataChange());
            countryField.addValueChangeListener(_ -> handleDataChange());
        }

        private void handleDataChange() {
            callback.withContactInformation(binder.getBean() != null && !createComparableSnapshot(binder.getBean()).equals(snapshot));
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

        private void setFormDataObject(MemberContactInformationEditDTO dto) {
            binder.setBean(dto);
            this.snapshot = createComparableSnapshot(dto);
        }

        private Optional<MemberContactInformationEditDTO> getFormDataObject() {
            if (binder.getBean() == null || binder.getBean().equals(snapshot)) {
                return Optional.empty();
            }
            if (!binder.validate().isOk()) {
                throw new RuntimeException("Contact Information form data is invalid");
            }
            try {
                binder.writeBean(binder.getBean());
                return Optional.of(binder.getBean());
            } catch (ValidationException e) {
                return Optional.empty();
            }
        }
    }



    @Getter
    @Setter
    private static class MembershipDetailsEditForm extends Tab {
        private TextField membershipNumberField = new TextField("Membership Number");
        private Select<String> membershipTypeSelectField = new Select<>("Membership Type");
        private Select<String> membershipStatusSelectField = new Select<>("Membership Status");
        private TextField startDateField = new TextField("Start Date");
        private TextField expirationDateField = new TextField("Expiration Date");
        private TextField renewalDateField = new TextField("Renewal Date");
        private TextField cancellationDateField = new TextField("Cancellation Date");

        private final BeanValidationBinder<MembershipEditDTO> binder = new BeanValidationBinder<>(MembershipEditDTO.class);

        private MembershipEditDTO snapshot;
        private final WithMembershipDetailsCallback callback;

        private MembershipDetailsEditForm(MembershipEditDTO dto, Set<String> membershipTypes, Set<String> membershipStatuses, WithMembershipDetailsCallback callback) {
            this.callback = callback;

            binder.setBean(dto);
            this.snapshot = createComparableSnapshot(binder.getBean());

            this.membershipTypeSelectField.setItems(membershipTypes);
            this.membershipTypeSelectField.setEmptySelectionAllowed(false);

            this.membershipStatusSelectField.setItems(membershipStatuses);
            this.membershipStatusSelectField.setEmptySelectionAllowed(false);


            registerDirtyStateListeners();

            if (dto != null && dto.equals(snapshot)) {
                callback.withMembership(false);
            }

            configureBinder();
            add(createFormLayout());
        }

        private void configureBinder() {
            binder.forField(membershipNumberField).bindReadOnly(MembershipEditDTO::getMembershipNumber);
            binder.forField(membershipTypeSelectField).bind(MembershipEditDTO::getMembershipType, MembershipEditDTO::setMembershipType);
            binder.forField(membershipStatusSelectField).bind(MembershipEditDTO::getMembershipStatus, MembershipEditDTO::setMembershipStatus);
            binder.forField(startDateField).bindReadOnly(MembershipEditDTO::getStartDate);
            binder.forField(expirationDateField).bind(MembershipEditDTO::getExpirationDate, MembershipEditDTO::setExpirationDate);
            binder.forField(renewalDateField).bind(MembershipEditDTO::getRenewalDate, MembershipEditDTO::setRenewalDate);
            binder.forField(cancellationDateField).bind(MembershipEditDTO::getCancellationDate, MembershipEditDTO::setCancellationDate);
        }

        private void registerDirtyStateListeners() {
            membershipTypeSelectField.addValueChangeListener(_ -> handleDataChange());
            membershipStatusSelectField.addValueChangeListener(_ -> handleDataChange());
            expirationDateField.addValueChangeListener(_ -> handleDataChange());
            renewalDateField.addValueChangeListener(_ -> handleDataChange());
            cancellationDateField.addValueChangeListener(_ -> handleDataChange());
        }

        private void handleDataChange() {
            callback.withMembership(binder.getBean() != null && !createComparableSnapshot(binder.getBean()).equals(snapshot));
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

        private void setFormDataObject(MembershipEditDTO dto) {
            binder.setBean(dto);
            this.snapshot = createComparableSnapshot(dto);
        }

        private Optional<MembershipEditDTO> getFormDataObject() {
            if (binder.getBean() == null || binder.getBean().equals(snapshot)) {
                return Optional.empty();
            }
            if (!binder.validate().isOk()) {
                throw new RuntimeException("Membership form data is invalid");
            }
            try {
                binder.writeBean(binder.getBean());
                return Optional.of(binder.getBean());
            } catch (ValidationException e) {
                return Optional.empty();
            }
        }
    }
}
