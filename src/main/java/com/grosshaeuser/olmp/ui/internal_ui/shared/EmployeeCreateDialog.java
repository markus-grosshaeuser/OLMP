package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.employee.dto.EmployeeCreateDTO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.util.HashSet;
import java.util.Set;

public class EmployeeCreateDialog extends BaseDialog{

    private static final int PASSWORD_MIN_LENGTH = 8;

    @FunctionalInterface
    public interface SaveCallback {
        void save(EmployeeCreateDTO dto);
    }

    @FunctionalInterface
    public interface ErrorCallback {
        void handleException(RuntimeException e);
    }

    private final SaveCallback saveCallback;
    private final ErrorCallback errorCallback;

    private final Set<String> roles;

    private final BeanValidationBinder<EmployeeCreateDTO> binder = new BeanValidationBinder<>(EmployeeCreateDTO.class);

    private final TextField firstNameField = new TextField("First name");
    private final TextField lastNameField = new TextField("Last name");
    private final TextField usernameField = new TextField("Username");
    private final TextField emailField = new TextField("Email");
    private final PasswordField passwordField = new PasswordField("Password");
    private final Checkbox enabledField = new Checkbox("Enabled");
    private final CheckboxGroup<String> roleCheckBoxGroup = new CheckboxGroup<>();

    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");

    public EmployeeCreateDialog(HashSet<String> roles, SaveCallback saveCallback, ErrorCallback errorCallback) {
        this.saveCallback = saveCallback;
        this.errorCallback = errorCallback;
        this.roles = roles;

        configureFields();
        configureActions();
        configureBinder();

        binder.setBean(new EmployeeCreateDTO());

        getHeader().add(createDialogHeader("Create Employee Account", "Create a new account by providing profile information, account state and assigned roles."));
        add(createFormLayout());
        getFooter().add(createActionBar(new Button[]{saveButton}, new Button[]{}, new Button[]{cancelButton}));

        setKeepInViewport(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
    }

    private VerticalLayout createFormLayout() {
        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout secondaryElements = new HorizontalLayout();
        secondaryElements.setAlignItems(FlexComponent.Alignment.START);
        secondaryElements.setWidthFull();
        secondaryElements.add(createAccountStatusSection(), createAuthorizationSection());
        layout.add(createIdentitySection(),secondaryElements);
        return layout;
    }

    private Div createIdentitySection() {
        FormLayout form = new FormLayout();
        form.addClassName("employee-creation-dialog-form");
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("40rem", 2),
                new FormLayout.ResponsiveStep("64rem", 3)
        );
        form.add(firstNameField, lastNameField, usernameField, emailField, passwordField);
        return createSection("Profile", "Core identity and sign-in information.", form);
    }

    private Div createAccountStatusSection() {
        Div statusItems = new Div(enabledField);
        statusItems.addClassName("employee-creation-dialog_status-list");
        return createSection("Account Settings", "Control whether this user can access the application.", statusItems);
    }

    private Div createAuthorizationSection() {
        return createSection("Authorization", "Assign application roles available to your administration level.", roleCheckBoxGroup);
    }

    private void configureActions() {
        saveButton.addClickListener(_ -> {
            if (binder.getBean() == null) {
                throw new IllegalStateException("No user data object set.");
            }
            if (binder.validate().isOk()) {
                try {
                    EmployeeCreateDTO employee = binder.getBean();
                    saveCallback.save(employee);
                    notifyUser("New user account for " + employee.getFirstName() + " " + employee.getLastName() + " was created successfully.", NotificationVariant.INFO);
                    close();
                } catch (RuntimeException e) {
                    errorCallback.handleException(e);
                }
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton.addClickListener(_ -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
    }

    private void configureFields() {
        passwordField.setHelperText("Use at least " + PASSWORD_MIN_LENGTH + " characters.");
        passwordField.setRevealButtonVisible(true);
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);

        roleCheckBoxGroup.setItems(roles);
        roleCheckBoxGroup.addThemeVariants(CheckboxGroupVariant.AURA_HORIZONTAL);
        roleCheckBoxGroup.addClassName("employee-creation-dialog-roles");
    }

    private void configureBinder() {
        binder.forField(firstNameField)
                .asRequired("First name is required")
                .bind(EmployeeCreateDTO::getFirstName, EmployeeCreateDTO::setFirstName);
        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .bind(EmployeeCreateDTO::getLastName, EmployeeCreateDTO::setLastName);
        binder.forField(usernameField)
                .asRequired("Username is required")
                .bind(EmployeeCreateDTO::getUsername, EmployeeCreateDTO::setUsername);
        binder.forField(emailField)
                .asRequired("Email is required")
                .bind(EmployeeCreateDTO::getEmail, EmployeeCreateDTO::setEmail);
        binder.forField(passwordField)
                .withValidator(
                        value -> value.length() >= PASSWORD_MIN_LENGTH,
                        "Password must be at least " + PASSWORD_MIN_LENGTH + " characters long"
                )
                .bind(EmployeeCreateDTO::getPassword, EmployeeCreateDTO::setPassword);
        binder.forField(enabledField).bind(EmployeeCreateDTO::isEnabled, EmployeeCreateDTO::setEnabled);
        binder.forField(roleCheckBoxGroup)
                .asRequired("At least one role must be selected")
                .bind(EmployeeCreateDTO::getRoles, EmployeeCreateDTO::setRoles);
    }
}
