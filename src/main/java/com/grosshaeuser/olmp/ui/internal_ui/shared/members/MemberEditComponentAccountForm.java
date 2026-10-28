package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberAccountEditDTO;
import com.grosshaeuser.olmp.ui.internal_ui.shared.members.MemberEditComponent.AccountDataChangedCallback;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Objects;
import java.util.Optional;

class MemberEditComponentAccountForm extends Tab {

    private final MemberContext memberContext;
    private final AccountDataChangedCallback accountDataChangedCallback;

    private final TextField accountIdField = new TextField("Account ID");
    private final TextField emailField = new TextField("Email");
    private final PasswordField newPasswordField = new PasswordField("New Password");
    private final Checkbox verifiedField = new Checkbox("Verified");
    private final Checkbox enabledField = new Checkbox("Enabled");
    private final Checkbox lockedField = new Checkbox("Locked");
    private final IntegerField failedLoginAttemptsField = new IntegerField("Failed Login Attempts");
    private final DateTimePicker lockoutUntilField = new DateTimePicker("Lockout Until");
    private final DateTimePicker accountCreatedAtField = new DateTimePicker("Account Created At");
    private final DateTimePicker accountUpdatedAtField = new DateTimePicker("Account Updated At");
    private final DateTimePicker accountLastAccessedAtField = new DateTimePicker("Account Last Accessed At");

    private final BeanValidationBinder<MemberAccountEditDTO> binder = new BeanValidationBinder<>(MemberAccountEditDTO.class);
    private MemberAccountEditDTO snapshot;

    MemberEditComponentAccountForm(MemberContext memberContext, MemberAccountEditDTO dto, AccountDataChangedCallback accountDataChangedCallback) {
        this.memberContext = memberContext;
        this.accountDataChangedCallback = accountDataChangedCallback;
        configureFields();
        configureBinder();
        loadMemberAccount(dto);
        add(createFormLayout());
    }

    void loadMemberAccount(MemberAccountEditDTO dto) {
        binder.readBean(dto);
        snapshot = createComparableSnapshot(dto);
        handleDataChange();
    }

    private void configureFields() {
        newPasswordField.setTooltipText(memberContext.getPasswordRequirementDescription());
        newPasswordField.setRevealButtonVisible(true);
        newPasswordField.setWidthFull();
        newPasswordField.setRequiredIndicatorVisible(true);
    }

    private void configureBinder() {
        binder.forField(accountIdField).bindReadOnly(dto -> String.valueOf(dto.getId()));
        binder.forField(emailField).bind(MemberAccountEditDTO::getEmail, MemberAccountEditDTO::setEmail);
        binder.forField(newPasswordField)
                .withValidator(
                        value -> value == null || value.isBlank() || value.matches(memberContext.getRegExPattern()),
                        memberContext.getPasswordRequirementDescription()
                )
                .bind(MemberAccountEditDTO::getNewPassword, MemberAccountEditDTO::setNewPassword);
        binder.forField(verifiedField).bind(MemberAccountEditDTO::isVerified, MemberAccountEditDTO::setVerified);
        binder.forField(enabledField).bind(MemberAccountEditDTO::isEnabled, MemberAccountEditDTO::setEnabled);
        binder.forField(lockedField).bind(MemberAccountEditDTO::isLocked, MemberAccountEditDTO::setLocked);
        binder.forField(failedLoginAttemptsField).bindReadOnly(MemberAccountEditDTO::getFailedLoginAttempts);
        binder.forField(lockoutUntilField).bindReadOnly(MemberAccountEditDTO::getLockoutUntil);
        binder.forField(accountCreatedAtField).bindReadOnly(MemberAccountEditDTO::getAccountCreatedAt);
        binder.forField(accountUpdatedAtField).bindReadOnly(MemberAccountEditDTO::getAccountUpdatedAt);
        binder.forField(accountLastAccessedAtField).bindReadOnly(MemberAccountEditDTO::getAccountLastAccessedAt);

        binder.addValueChangeListener(_ -> handleDataChange());
    }

    private void handleDataChange() {
        accountDataChangedCallback.accountDataChanged(getFormDataObject()
                .map(formData -> hasChanges(createComparableSnapshot(formData)))
                .orElse(false)
        );
    }

    private FormLayout createFormLayout() {
        FormLayout layout = new FormLayout();
        layout.setAutoResponsive(true);
        layout.setExpandColumns(true);
        layout.setExpandFields(true);

        Div checkboxes = new Div(enabledField, verifiedField, lockedField);
        layout.addFormRow(accountIdField, checkboxes);
        layout.addFormRow(emailField, newPasswordField);
        layout.addFormRow(failedLoginAttemptsField, lockoutUntilField, accountLastAccessedAtField);
        layout.addFormRow(accountCreatedAtField, accountUpdatedAtField);
        layout.setColspan(checkboxes, 2);
        layout.setColspan(emailField, 2);
        layout.setSizeFull();

        return layout;
    }

    private MemberAccountEditDTO createComparableSnapshot(MemberAccountEditDTO member) {
        MemberAccountEditDTO snapshot = member.toBuilder().build();
        if (snapshot.getNewPassword() != null && snapshot.getNewPassword().isBlank()) {
            snapshot.setNewPassword(null);
        }
        return snapshot;
    }

    Optional<MemberAccountEditDTO> getFormDataObject() {
        MemberAccountEditDTO formData = snapshot.toBuilder().build();
        try {
            binder.writeBean(formData);
            return Optional.of(createComparableSnapshot(formData));
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    private boolean hasChanges(MemberAccountEditDTO account) {
        return !Objects.equals(account.getEmail(), snapshot.getEmail())
                || !Objects.equals(account.isVerified(), snapshot.isVerified())
                || !Objects.equals(account.isEnabled(), snapshot.isEnabled())
                || !Objects.equals(account.isLocked(), snapshot.isLocked())
                || !Objects.equals(normalizePassword(account.getNewPassword()), normalizePassword(snapshot.getNewPassword()));

    }

    private String normalizePassword(String password) {
        return password == null || password.trim().isEmpty() ? null : password.trim();
    }
}
