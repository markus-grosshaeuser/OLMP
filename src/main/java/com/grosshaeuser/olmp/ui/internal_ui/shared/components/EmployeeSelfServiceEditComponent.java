package com.grosshaeuser.olmp.ui.internal_ui.shared.components;

import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Optional;

public class EmployeeSelfServiceEditComponent extends Composite<VerticalLayout> implements Structurable, Delegatable {
    private static final int PASSWORD_MIN_LENGTH = 8;

    private final UpdateCallback<EmployeeEditDTO> updateCallback;
    private final ErrorCallback errorCallback;

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
            ErrorCallback errorCallback
    ) {
        this.updateCallback = updateCallback;
        this.errorCallback = errorCallback;

        configureRootLayout();
        configureFields();
        configureBinder();
        configureActions();

        this.snapshot = createComparableSnapshot(employeeEditDTO);
        binder.setBean(employeeEditDTO);
        registerDirtyStateListeners();

        getContent().add(
                createFormElementBase(),
                createActionBar(new Button[]{saveButton}, new Button[]{}, new Button[]{})
        );
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
        newPasswordField.setTooltipText("Use at least " + PASSWORD_MIN_LENGTH + " characters when setting a new password.");
        newPasswordField.setRevealButtonVisible(false);
        newPasswordField.setWidthFull();
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
                        value -> value == null || value.isBlank() || value.length() >= PASSWORD_MIN_LENGTH,
                        "Password must be at least " + PASSWORD_MIN_LENGTH + " characters long"
                )
                .bind(EmployeeEditDTO::getNewPassword, EmployeeEditDTO::setNewPassword);

        binder.forField(createdAtField)
                .bindReadOnly(EmployeeEditDTO::getCreatedAt);

        binder.forField(updatedAtField)
                .bindReadOnly(EmployeeEditDTO::getUpdatedAt);

        binder.forField(lastAccessedField)
                .bindReadOnly(EmployeeEditDTO::getAccountLastAccessed);
    }

    private void registerDirtyStateListeners() {
        firstNameField.addValueChangeListener(_ -> handleDataChange());
        lastNameField.addValueChangeListener(_ -> handleDataChange());
        emailField.addValueChangeListener(_ -> handleDataChange());
        usernameField.addValueChangeListener(_ -> handleDataChange());
        newPasswordField.addValueChangeListener(_ -> handleDataChange());
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
        saveButton.setEnabled(binder.getBean() != null && !createComparableSnapshot(binder.getBean()).equals(snapshot));
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
        if (binder.getBean() == null) {
            throw new IllegalStateException("No employee data object set");
        }

        if (!binder.validate().isOk()) {
            return Optional.empty();
        }

        try {
            binder.writeBean(binder.getBean());
            return Optional.of(binder.getBean());
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    private void save() {
        getFormDataObject().ifPresent(employee -> {
            try {
                setActionsEnabled(false);

                EmployeeEditDTO savedEmployee = updateCallback.update(employee);
                binder.setBean(savedEmployee);
                snapshot = createComparableSnapshot(savedEmployee);

                Notification notification = new Notification("Your profile was updated.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.SUCCESS);
                notification.setDuration(3000);
                notification.open();
            } catch (RuntimeException e) {
                errorCallback.handleException(e);
            } finally {
                setActionsEnabled(true);
                handleDataChange();
            }
        });
    }
}