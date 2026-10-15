package com.grosshaeuser.olmp.ui.internal_ui.shared.components;

import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Objects;
import java.util.Optional;

public class EmployeeSelfServiceEditComponent extends Composite<VerticalLayout> implements Structurable, Delegatable {

    private final UpdateCallback<EmployeeEditDTO> updateCallback;
    private final ErrorCallback errorCallback;
    private final PasswordProperties passwordProperties;

    private final BeanValidationBinder<EmployeeEditDTO> binder = new BeanValidationBinder<>(EmployeeEditDTO.class);

    private final TextField userIdField = new TextField("ID");
    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final TextField emailField = new TextField("Email");
    private final TextField usernameField = new TextField("Username");
    private final PasswordField newPasswordField = new PasswordField("New Password");

    private final TextField createdAtField = new TextField("Created At");
    private final TextField updatedAtField = new TextField("Updated At");
    private final TextField lastAccessedField = new TextField("Last Accessed");

    private final Button saveButton = new Button("Save");

    private EmployeeEditDTO snapshot;

    public EmployeeSelfServiceEditComponent(
            EmployeeEditDTO employeeEditDTO,
            UpdateCallback<EmployeeEditDTO> updateCallback,
            ErrorCallback errorCallback,
            PasswordProperties passwordProperties
    ) {
        this.updateCallback = updateCallback;
        this.errorCallback = errorCallback;
        this.passwordProperties = passwordProperties;

        configureRootLayout();
        configureFields();
        configureBinder();
        configureActions();

        loadEmployee(employeeEditDTO);

        getContent().add(
                createFormElementBase(),
                createActionBar(new Button[]{saveButton}, new Button[]{}, new Button[]{})
        );
    }

    private void loadEmployee(EmployeeEditDTO employee) {
        snapshot = createComparableSnapshot(employee);
        binder.readBean(snapshot);
        handleDataChange();
    }

    private void configureRootLayout() {
        VerticalLayout layout = getContent();
        layout.addClassName("employee-self-service-edit-component");
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setWidthFull();
    }

    private Div createFormElementBase() {
        FormLayout form = new FormLayout();
        form.setAutoResponsive(true);
        form.setExpandColumns(true);
        form.setExpandFields(true);

        form.addFormRow(usernameField);
        form.addFormRow(firstNameField, lastNameField);
        form.addFormRow(emailField, newPasswordField);
        form.addFormRow(createdAtField, updatedAtField, lastAccessedField);


        return createSection("Profile and account", "Update your own profile and account data.", form);
    }

    private void configureFields() {
        userIdField.setReadOnly(true);
        createdAtField.setReadOnly(true);
        updatedAtField.setReadOnly(true);
        lastAccessedField.setReadOnly(true);

        newPasswordField.setPlaceholder("Leave empty to keep current password");
        newPasswordField.setTooltipText(passwordProperties.getPasswordRequirementDescription());
        newPasswordField.setRevealButtonVisible(false);
    }

    private void configureBinder() {
        binder.forField(userIdField)
                .bindReadOnly(EmployeeEditDTO::idAsString);

        binder.forField(firstNameField)
                .asRequired("First name is required")
                .bind(EmployeeEditDTO::getFirstName, EmployeeEditDTO::setFirstName);

        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .bind(EmployeeEditDTO::getLastName, EmployeeEditDTO::setLastName);

        binder.forField(emailField)
                .asRequired("Email is required")
                .bind(EmployeeEditDTO::getEmail, EmployeeEditDTO::setEmail);

        binder.forField(usernameField)
                .asRequired("Username is required")
                .bind(EmployeeEditDTO::getUsername, EmployeeEditDTO::setUsername);

        binder.forField(newPasswordField)
                .withValidator(
                        value -> value == null || value.isBlank() || value.matches(passwordProperties.getRegExPattern()),
                        passwordProperties.getPasswordRequirementDescription()
                )
                .bind(EmployeeEditDTO::getNewPassword, EmployeeEditDTO::setNewPassword);

        binder.forField(createdAtField)
                .bindReadOnly(EmployeeEditDTO::getCreatedAt);

        binder.forField(updatedAtField)
                .bindReadOnly(EmployeeEditDTO::getUpdatedAt);

        binder.forField(lastAccessedField)
                .bindReadOnly(EmployeeEditDTO::getAccountLastAccessed);

        binder.addValueChangeListener(_ -> handleDataChange());
    }

    private void configureActions() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);
        saveButton.addClickListener(_ -> save());
    }

    private EmployeeEditDTO createComparableSnapshot(EmployeeEditDTO employee) {
        EmployeeEditDTO comparableSnapshot = employee.toBuilder().build();
        if (comparableSnapshot.getNewPassword() != null && comparableSnapshot.getNewPassword().isBlank()) {
            comparableSnapshot.setNewPassword(null);
        }
        return comparableSnapshot;
    }

    private void handleDataChange() {
        saveButton.setEnabled(getFormDataObject()
                .map(formData -> hasChanges(createComparableSnapshot(formData)))
                .orElse(false));
    }

    private void setActionsEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);

        firstNameField.setEnabled(enabled);
        lastNameField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        newPasswordField.setEnabled(enabled);
    }

    private Optional<EmployeeEditDTO> getFormDataObject() {
        EmployeeEditDTO formData = snapshot.toBuilder().build();

        try {
            binder.writeBean(formData);
            return Optional.of(createComparableSnapshot(formData));
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    private void save() {
        getFormDataObject().ifPresent(employee -> {
            try {
                setActionsEnabled(false);
                EmployeeEditDTO savedEmployee = updateCallback.update(employee);
                loadEmployee(savedEmployee);
            } catch (RuntimeException e) {
                errorCallback.handleException(e);
            } finally {
                setActionsEnabled(true);
                handleDataChange();
            }
        });
    }

    private boolean hasChanges(EmployeeEditDTO employee) {
        return !Objects.equals(employee.getFirstName(), snapshot.getFirstName())
                || !Objects.equals(employee.getLastName(), snapshot.getLastName())
                || !Objects.equals(employee.getEmail(), snapshot.getEmail())
                || !Objects.equals(employee.getUsername(), snapshot.getUsername())
                || !Objects.equals(normalizePassword(employee.getNewPassword()), normalizePassword(snapshot.getNewPassword()));
    }

    private String normalizePassword(String password) {
        return password == null || password.trim().isEmpty() ? null : password.trim();
    }
}