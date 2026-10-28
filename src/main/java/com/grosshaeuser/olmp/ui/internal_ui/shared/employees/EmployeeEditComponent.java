package com.grosshaeuser.olmp.ui.internal_ui.shared.employees;

import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import com.grosshaeuser.olmp.employees.mapper.EmployeeMapper;
import com.grosshaeuser.olmp.ui.internal_ui.shared.CrudCallbacks;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class EmployeeEditComponent extends Composite<VerticalLayout> implements Structurable, CrudCallbacks {

    @FunctionalInterface
    public interface CloseCallback {
        void close();
    }

    private final EmployeeContext employeeContext;
    private final UiContext uiContext;

    private final Set<String> assignableRoles;

    private final UpdateCallback<EmployeeEditDTO> updateCallback;
    private final CloseCallback closeCallback;

    private final BeanValidationBinder<EmployeeEditDTO> binder = new BeanValidationBinder<>(EmployeeEditDTO.class);

    private final Button saveButton = new Button("Save");
    private final Button closeButton = new Button("Close");

    private final TextField userIdField = new TextField("ID");
    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final TextField emailField = new TextField("Email");
    private final DateTimePicker createdAtField = new DateTimePicker("Created At");
    private final DateTimePicker updatedAtField = new DateTimePicker("Updated At");

    private final TextField accountIdField = new TextField("Account ID");
    private final TextField usernameField = new TextField("Username");
    private final PasswordField newPasswordField = new PasswordField("New Password");
    private final Checkbox enabledCheckBox = new Checkbox("Enabled");
    private final Checkbox lockedCheckBox = new Checkbox("Locked");
    private final IntegerField failedLoginAttemptsField = new IntegerField("Failed Login Attempts");
    private final DateTimePicker lockoutUntilField = new DateTimePicker("Lockout Until");
    private final DateTimePicker accountCreatedAtField = new DateTimePicker("Account Created At");
    private final DateTimePicker accountUpdatedAtField = new DateTimePicker("Account Updated At");
    private final DateTimePicker accountLastAccessedField = new DateTimePicker("Account Last Accessed");
    private final CheckboxGroup<String> rolesCheckBoxGroup = new CheckboxGroup<>("Roles");

    private EmployeeEditDTO snapshot;

    public EmployeeEditComponent(
            EmployeeContext employeeContext,
            UiContext uiContext,
            EmployeeEditDTO employeeEditDTO,
            UpdateCallback<EmployeeEditDTO> updateCallback,
            CloseCallback closeCallback
    ) {
        this.employeeContext = employeeContext;
        this.uiContext = uiContext;
        this.updateCallback = updateCallback;
        this.closeCallback = closeCallback;
        this.assignableRoles = EmployeeMapper.mapRolesToRoleNames(employeeContext.getRoleService().getAssignableRoles());

        configureRootLayout();
        configureFields();
        configureBinder();
        configureActions();

        loadEmployee(employeeEditDTO);

        getContent().add(
                createComponentHeader("Employee details", "Review profile information, account state and assigned roles."),
                createFormElementBase(),
                createActionBar(new Button[]{saveButton}, new Button[]{}, new Button[]{closeButton})
        );
    }

    private void loadEmployee(EmployeeEditDTO employeeEditDTO) {
        snapshot = createComparableSnapshot(employeeEditDTO);
        binder.readBean(employeeEditDTO);
        handleDataChange();
    }

    private void configureRootLayout() {
        VerticalLayout layout = getContent();
        layout.addClassName("employee-edit-component");
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
    }

    private HorizontalLayout createFormElementBase() {
        Div identitySection = createIdentitySection();
        Div accountSection = createAccountSection();

        HorizontalLayout editableData = new HorizontalLayout(identitySection, accountSection);
        editableData.addClassName("employee-edit-component-form-base");
        editableData.setPadding(true);
        editableData.setSpacing(true);
        editableData.setWidthFull();

        return editableData;
    }

    private Div createIdentitySection() {
        FormLayout form = new FormLayout();
        form.setAutoResponsive(true);
        form.setExpandColumns(true);
        form.setExpandFields(true);
        form.addFormRow(userIdField, rolesCheckBoxGroup);
        form.addFormRow(firstNameField, lastNameField);
        form.addFormRow(emailField);
        form.addFormRow(createdAtField, updatedAtField);
        form.setColspan(emailField, 2);
        Div section = createSection("Profile", "Core identity and sign-in information.", form);
        section.addClassName("employee-edit-component-identity-form");
        return section;
    }

    private Div createAccountSection() {
        FormLayout form = new FormLayout();
        form.setAutoResponsive(true);
        form.setExpandColumns(true);
        form.setExpandFields(true);
        form.addFormRow(accountIdField, new Div(lockedCheckBox, enabledCheckBox));
        form.addFormRow(usernameField, newPasswordField);
        form.addFormRow(accountCreatedAtField, accountUpdatedAtField);
        form.addFormRow(lockoutUntilField, accountLastAccessedField);
        Div section =  createSection("Account", "Account information.", form);
        section.addClassName("employee-edit-component-account-form");
        return section;
    }

    private void configureFields() {
        newPasswordField.setPlaceholder("Leave empty to keep current password");
        newPasswordField.setTooltipText(employeeContext.getPasswordRequirementDescription());
        newPasswordField.setRevealButtonVisible(false);

        rolesCheckBoxGroup.setLabel("Roles");
        rolesCheckBoxGroup.setItems(assignableRoles);
        rolesCheckBoxGroup.addThemeVariants(CheckboxGroupVariant.AURA_HORIZONTAL);
    }

    private void configureBinder() {
        binder.forField(userIdField)
                .bindReadOnly(dto -> String.valueOf(dto.getId()));
        binder.forField(emailField)
                .asRequired("Email is required")
                .bind(EmployeeEditDTO::getEmail, EmployeeEditDTO::setEmail);
        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .bind(EmployeeEditDTO::getLastName, EmployeeEditDTO::setLastName);
        binder.forField(firstNameField)
                .asRequired("First name is required")
                .bind(EmployeeEditDTO::getFirstName, EmployeeEditDTO::setFirstName);
        binder.forField(createdAtField)
                .bindReadOnly(EmployeeEditDTO::getCreatedAt);
        binder.forField(updatedAtField)
                .bindReadOnly(EmployeeEditDTO::getUpdatedAt);

        binder.forField(accountIdField)
                .bindReadOnly(dto -> String.valueOf(dto.getAccountId()));
        binder.forField(usernameField)
                .asRequired("Username is required")
                .bind(EmployeeEditDTO::getUsername, EmployeeEditDTO::setUsername);
        binder.forField(newPasswordField)
                .withValidator(
                        value -> value == null || value.isBlank() || value.matches(employeeContext.getRegExPattern()),
                        employeeContext.getPasswordRequirementDescription()
                )
                .bind(EmployeeEditDTO::getNewPassword, EmployeeEditDTO::setNewPassword);
        binder.forField(enabledCheckBox).bind(EmployeeEditDTO::isEnabled, EmployeeEditDTO::setEnabled);
        binder.forField(failedLoginAttemptsField).bindReadOnly(EmployeeEditDTO::getFailedLoginAttempts);
        binder.forField(lockoutUntilField).bindReadOnly(EmployeeEditDTO::getLockoutUntil);
        binder.forField(lockedCheckBox).bind(EmployeeEditDTO::isLocked, EmployeeEditDTO::setLocked);
        binder.forField(accountCreatedAtField).bind(EmployeeEditDTO::getCreatedAt, null);
        binder.forField(accountUpdatedAtField).bind(EmployeeEditDTO::getUpdatedAt, null);
        binder.forField(accountLastAccessedField).bind(EmployeeEditDTO::getAccountLastAccessed, null);
        binder.forField(rolesCheckBoxGroup).bind(EmployeeEditDTO::getRoles, EmployeeEditDTO::setRoles);

        binder.addValueChangeListener(_ -> handleDataChange());
    }

    private void handleDataChange() {
        saveButton.setEnabled(getFormDataObject()
                .map(formData -> hasChanges(createComparableSnapshot(formData)))
                .orElse(false));    }

    private void configureActions() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);
        saveButton.addClickListener(_ -> save());

        closeButton.addClickListener(_ -> closeCallback.close());
    }

    private EmployeeEditDTO createComparableSnapshot(EmployeeEditDTO employee) {
        EmployeeEditDTO snapshot = employee.toBuilder().build();
        if (snapshot.getNewPassword() != null && snapshot.getNewPassword().isBlank()) {
            snapshot.setNewPassword(null);
        }
        return snapshot;
    }

    private void setActionsEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);

        firstNameField.setEnabled(enabled);
        lastNameField.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        newPasswordField.setEnabled(enabled);

        enabledCheckBox.setEnabled(enabled);
        lockedCheckBox.setEnabled(enabled);
        rolesCheckBoxGroup.setEnabled(enabled);
    }

    public Optional<EmployeeEditDTO> getFormDataObject() {
        EmployeeEditDTO formData = snapshot.toBuilder().build();

        try {
            binder.writeBean(formData);
            return Optional.of(createComparableSnapshot(formData));
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    public void save() {
        getFormDataObject().ifPresent(user -> {
            try {
                setActionsEnabled(false);
                EmployeeEditDTO savedEmployee = updateCallback.update(user);
                loadEmployee(savedEmployee);
            } catch (RuntimeException e) {
                uiContext.handleException(e);
            } finally {
                setActionsEnabled(true);
                handleDataChange();
            }
        });
    }

    private boolean hasChanges(EmployeeEditDTO employee) {
        return !Objects.equals(employee.getUsername(), snapshot.getUsername())
                || !Objects.equals(employee.getFirstName(), snapshot.getFirstName())
                || !Objects.equals(employee.getLastName(), snapshot.getLastName())
                || !Objects.equals(employee.getEmail(), snapshot.getEmail())
                || !Objects.equals(employee.isLocked(), snapshot.isLocked())
                || !Objects.equals(employee.isEnabled(), snapshot.isEnabled())
                || !Objects.equals(employee.getRoles(), snapshot.getRoles())
                || !Objects.equals(normalizePassword(employee.getNewPassword()), normalizePassword(snapshot.getNewPassword()));
    }

    private String normalizePassword(String password) {
        return password == null || password.trim().isEmpty() ? null : password.trim();
    }
}