package com.grosshaeuser.olmp.ui.internal_ui.shared.employees;

import com.grosshaeuser.olmp.employees.dto.EmployeeRegistrationDTO;
import com.grosshaeuser.olmp.employees.mapper.EmployeeMapper;
import com.grosshaeuser.olmp.ui.internal_ui.shared.CrudCallbacks;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.util.Set;

public class EmployeeCreateDialog extends Dialog implements Structurable, CrudCallbacks {

    private final EmployeeContext employeeContext;
    private final UiContext uiContext;

    private final CreateCallback<EmployeeRegistrationDTO> createCallback;

    private final Set<String> assignableRoles;

    private final BeanValidationBinder<EmployeeRegistrationDTO> binder = new BeanValidationBinder<>(EmployeeRegistrationDTO.class);

    private final TextField firstNameField = new TextField("First name");
    private final TextField lastNameField = new TextField("Last name");
    private final TextField usernameField = new TextField("Username");
    private final TextField emailField = new TextField("Email");
    private final PasswordField passwordField = new PasswordField("Password");
    private final Checkbox enabledField = new Checkbox("Enabled");
    private final CheckboxGroup<String> roleCheckBoxGroup = new CheckboxGroup<>("Roles");

    private final Button saveButton = new Button("Save");

    public EmployeeCreateDialog(EmployeeContext employeeContext, UiContext uiContext, CreateCallback<EmployeeRegistrationDTO> createCallback) {
        this.employeeContext = employeeContext;
        this.uiContext = uiContext;
        this.createCallback = createCallback;
        this.assignableRoles = EmployeeMapper.mapRolesToRoleNames(employeeContext.getRoleService().getAssignableRoles());

        configureFields();
        configureActions();
        configureBinder();

        binder.setBean(new EmployeeRegistrationDTO());

        getHeader().add(createDialogHeader("Create Employee Account", "Create a new account by providing profile information, account state and assigned roles.", this::close));
        add(createFormElementBase());
        Button cancelButton = new Button("Cancel", _ -> close());
        getFooter().add(createActionBar(new Button[]{saveButton}, new Button[]{}, new Button[]{cancelButton}));

        setKeepInViewport(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
    }


    private HorizontalLayout createFormElementBase() {
        Div identitySection = createIdentitySection();
        Div accountStatusSection = createAccountStatusSection();

        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassName("employee-create-dialog-form-base");
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.add(identitySection, accountStatusSection);
        return layout;
    }

    private Div createIdentitySection() {
        FormLayout form = new FormLayout();
        form.setAutoResponsive(true);
        form.setExpandColumns(true);
        form.setExpandFields(true);
        form.addFormRow(firstNameField, lastNameField);
        form.addFormRow(emailField);
        form.addFormRow(usernameField, passwordField);
        form.setColspan(emailField, 2);
        Div section = createSection("Profile", "Core identity and sign-in information.", form);
        section.addClassName("employee-edit-component-identity-form");
        return section;
    }

    private Div createAccountStatusSection() {
        FormLayout form = new FormLayout();
        form.setAutoResponsive(true);
        form.setExpandColumns(true);
        form.setExpandFields(true);
        form.addFormRow(enabledField);
        form.addFormRow(roleCheckBoxGroup);
        Div section = createSection("Account Settings", "Control whether this user can access the application.", form);
        section.addClassName("employee-edit-component-account-form");
        return section;
    }

    private void configureActions() {
        saveButton.addClickListener(_ -> {
            if (binder.getBean() == null) {
                throw new IllegalStateException("No user data object set.");
            }
            if (binder.validate().isOk()) {
                try {
                    EmployeeRegistrationDTO employee = binder.getBean();
                    createCallback.create(employee);
                    close();
                } catch (RuntimeException e) {
                    uiContext.handleException(e);
                }
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    private void configureFields() {
        passwordField.setTooltipText(employeeContext.getPasswordRequirementDescription());
        passwordField.setRevealButtonVisible(true);
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);

        roleCheckBoxGroup.setItems(assignableRoles);
        roleCheckBoxGroup.addThemeVariants(CheckboxGroupVariant.AURA_HORIZONTAL);
        roleCheckBoxGroup.addClassName("employee-creation-dialog-roles");
    }

    private void configureBinder() {
        binder.forField(firstNameField)
                .asRequired("First name is required")
                .bind(EmployeeRegistrationDTO::getFirstName, EmployeeRegistrationDTO::setFirstName);
        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .bind(EmployeeRegistrationDTO::getLastName, EmployeeRegistrationDTO::setLastName);
        binder.forField(usernameField)
                .asRequired("Username is required")
                .bind(EmployeeRegistrationDTO::getUsername, EmployeeRegistrationDTO::setUsername);
        binder.forField(emailField)
                .asRequired("Email is required")
                .bind(EmployeeRegistrationDTO::getEmail, EmployeeRegistrationDTO::setEmail);
        binder.forField(passwordField)
                .withValidator(
                        value -> value.matches(employeeContext.getRegExPattern()),
                        employeeContext.getPasswordRequirementDescription()
                )
                .bind(EmployeeRegistrationDTO::getPassword, EmployeeRegistrationDTO::setPassword);
        binder.forField(enabledField).bind(EmployeeRegistrationDTO::isEnabled, EmployeeRegistrationDTO::setEnabled);
        binder.forField(roleCheckBoxGroup)
                .asRequired("At least one role must be selected")
                .bind(EmployeeRegistrationDTO::getRoles, EmployeeRegistrationDTO::setRoles);
    }
}
