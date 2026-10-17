package com.grosshaeuser.olmp.ui.internal_ui.shared.employees;

import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Delegatable;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class EmployeeEditComponent extends Composite<VerticalLayout> implements Structurable, Delegatable {

    private final Set<String> assignableRoles;

    private final UpdateCallback<EmployeeEditDTO> updateCallback;
    private final DeleteCallback<EmployeeEditDTO> deleteCallback;
    private final ErrorCallback errorCallback;

    private final PasswordProperties passwordProperties;

    private final BeanValidationBinder<EmployeeEditDTO> binder = new BeanValidationBinder<>(EmployeeEditDTO.class);

    private final Button saveButton = new Button("Save");
    private final Button deleteButton = new Button("Delete");

    private final TextField userIdField = new TextField("ID");
    private final TextField firstNameField = new TextField("First Name");
    private final TextField lastNameField = new TextField("Last Name");
    private final TextField emailField = new TextField("Email");
    private final TextField createdAtField = new TextField("Created At");
    private final TextField updatedAtField = new TextField("Updated At");

    private final TextField accountIdField = new TextField("Account ID");
    private final TextField usernameField = new TextField("Username");
    private final PasswordField newPasswordField = new PasswordField("New Password");
    private final Checkbox enabledCheckBox = new Checkbox("Enabled");
    private final Checkbox lockedCheckBox = new Checkbox("Locked");
    private final TextField failedLoginAttemptsField = new TextField("Failed Login Attempts");
    private final TextField lockoutUntilField = new TextField("Lockout Until");
    private final TextField accountCreatedAtField = new TextField("Account Created At");
    private final TextField accountUpdatedAtField = new TextField("Account Updated At");
    private final TextField accountLastAccessedField = new TextField("Account Last Accessed");
    private final CheckboxGroup<String> rolesCheckBoxGroup = new CheckboxGroup<>("Roles");

    private EmployeeEditDTO snapshot;

    public EmployeeEditComponent(
            EmployeeEditDTO employeeEditDTO,
            Set<String> assignableRoles,
            UpdateCallback<EmployeeEditDTO> updateCallback,
            DeleteCallback<EmployeeEditDTO> deleteCallback,
            ErrorCallback errorCallback,
            PasswordProperties passwordProperties
    ) {
        this.assignableRoles = assignableRoles;
        this.updateCallback = updateCallback;
        this.deleteCallback = deleteCallback;
        this.errorCallback = errorCallback;
        this.passwordProperties = passwordProperties;

        configureRootLayout();
        configureFields();
        configureBinder();
        configureActions();

        loadEmployee(employeeEditDTO);

        getContent().add(
                createComponentHeader("Employee details", "Review profile information, account state and assigned roles."),
                createFormElementBase(),
                createActionBar(new Button[]{saveButton}, new Button[]{}, new Button[]{deleteButton})
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
        newPasswordField.setTooltipText(passwordProperties.getPasswordRequirementDescription());
        newPasswordField.setRevealButtonVisible(false);

        rolesCheckBoxGroup.setLabel("Roles");
        rolesCheckBoxGroup.setItems(assignableRoles);
        rolesCheckBoxGroup.addThemeVariants(CheckboxGroupVariant.AURA_HORIZONTAL);
    }

    private void configureBinder() {
        binder.forField(userIdField)
                .bindReadOnly(EmployeeEditDTO::idAsString);
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
                .bindReadOnly(EmployeeEditDTO::accountIdAsString);
        binder.forField(usernameField)
                .asRequired("Username is required")
                .bind(EmployeeEditDTO::getUsername, EmployeeEditDTO::setUsername);
        binder.forField(newPasswordField)
                .withValidator(
                        value -> value == null || value.isBlank() || value.matches(passwordProperties.getRegExPattern()),
                        passwordProperties.getPasswordRequirementDescription()
                )
                .bind(EmployeeEditDTO::getNewPassword, EmployeeEditDTO::setNewPassword);
        binder.forField(enabledCheckBox).bind(EmployeeEditDTO::isEnabled, EmployeeEditDTO::setEnabled);
        binder.forField(failedLoginAttemptsField).bindReadOnly(EmployeeEditDTO::failedLoginAttemptsAsString);
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

        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(_ -> confirmDelete());
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
        deleteButton.setEnabled(enabled);

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
                errorCallback.handleException(e);
            } finally {
                setActionsEnabled(true);
                handleDataChange();
            }
        });
    }

    private void confirmDelete() {
        getFormDataObject().ifPresent(user -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Delete user account?");
            dialog.setText("Are you sure you want to delete the account \"" + user.getUsername() + "\"? This action cannot be undone.");
            dialog.setCancelable(true);
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(_ -> delete(user));
            dialog.open();
        });
    }

    private void delete(EmployeeEditDTO user) {
        try {
            deleteCallback.delete(user);
        } catch (RuntimeException e) {
            errorCallback.handleException(e);
        }
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