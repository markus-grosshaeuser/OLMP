package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Delegatable;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
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
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MemberCreateDialog extends Dialog implements Structurable, Delegatable {

    private final CreateCallback<MemberRegistrationDTO> createCallback;
    private final ErrorCallback errorCallback;
    private final PasswordProperties passwordProperties;

    private final BeanValidationBinder<MemberRegistrationDTO> binder = new BeanValidationBinder<>(MemberRegistrationDTO.class);

    private final TextField firstNameField = new TextField("First name");
    private final TextField lastNameField = new TextField("Last name");
    private final DatePicker birthdateField = new DatePicker("Birthdate");

    private final TextField emailField = new TextField("Email");
    private final PasswordField passwordField = new PasswordField("Password");
    private final Checkbox verifiedField = new Checkbox("Verified");
    private final Checkbox enabledField = new Checkbox("Enabled");

    private final Button saveButton = new Button("Save");

    private final MemberContactDetailsForm contactDetailsForm;
    private final MembershipDetailsForm membershipDetailsForm;

    public MemberCreateDialog(
            CreateCallback<MemberRegistrationDTO> createCallback,
            ErrorCallback errorCallback,
            HashMap<String, Integer> membershipPeriodsInMonths,
            PasswordProperties passwordProperties
    ){
        this.createCallback = createCallback;
        this.errorCallback = errorCallback;
        this.passwordProperties = passwordProperties;

        this.contactDetailsForm = new MemberContactDetailsForm(binder);
        this.membershipDetailsForm = new MembershipDetailsForm(binder, membershipPeriodsInMonths);

        configureFields();
        configureActions();
        configureBinder();

        binder.setBean(new MemberRegistrationDTO());

        getHeader().add(createDialogHeader("Create Member Account", "Create a new account by providing profile, membership and contact information.", this::close));
        add(createFormLayout());
        Button cancel = new Button("Cancel", _ -> close());
        getFooter().add(createActionBar(new Button[]{saveButton}, new Button[]{}, new Button[]{cancel}));

        setKeepInViewport(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
    }

    private HorizontalLayout createFormLayout() {
        HorizontalLayout formLayout = new HorizontalLayout();
        formLayout.setSpacing(false);
        formLayout.setPadding(false);
        formLayout.setMargin(false);
        formLayout.setWidthFull();
        VerticalLayout layout = new VerticalLayout(createBaseDataSection(), createAccountStatusSection());
        formLayout.add(layout);
        formLayout.add(createTabSheet());
        return formLayout;
    }

    private TabSheet createTabSheet() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Contact Details", contactDetailsForm);
        tabSheet.add("Membership Details", membershipDetailsForm);
        tabSheet.setSizeFull();
        return tabSheet;
    }

    private Div createBaseDataSection() {
        FormLayout form = new FormLayout();
        form.addClassName("member-creation-dialog-form");
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("40rem", 2),
                new FormLayout.ResponsiveStep("64rem", 3)
        );
        form.add(firstNameField, lastNameField, emailField, passwordField, birthdateField);
        return createSection("Profile", "Core identity and sign-in information.", form);
    }

    private Div createAccountStatusSection() {
        Div statusItems = new Div(enabledField, verifiedField);
        statusItems.addClassName("member-creation-dialog_status-list");
        return createSection("Account Status", "Account status and verification.", statusItems);
    }

    private void configureFields() {
        passwordField.setTooltipText(passwordProperties.getPasswordRequirementDescription());
        passwordField.setRevealButtonVisible(true);
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);
    }

    private void configureActions() {
        saveButton.addClickListener(_ -> {
            if (binder.getBean() == null) {
                throw new IllegalStateException("No use data object set.");
            }
            if (binder.validate().isOk()) {
                try {
                    MemberRegistrationDTO member = binder.getBean();
                    createCallback.create(member);
                    close();
                } catch (RuntimeException e) {
                    errorCallback.handleException(e);
                }
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    private void configureBinder() {
        binder.forField(firstNameField)
                .asRequired("First name is required")
                .bind(MemberRegistrationDTO::getFirstName, MemberRegistrationDTO::setFirstName);
        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .bind(MemberRegistrationDTO::getLastName, MemberRegistrationDTO::setLastName);
        binder.forField(emailField)
                .asRequired("Email is required")
                .bind(MemberRegistrationDTO::getEmail, MemberRegistrationDTO::setEmail);
        binder.forField(passwordField)
                .asRequired("Password is required")
                .withValidator(value -> value.matches(passwordProperties.getRegExPattern()),
                        passwordProperties.getPasswordRequirementDescription())
                .bind(MemberRegistrationDTO::getPassword, MemberRegistrationDTO::setPassword);
        binder.forField(birthdateField)
                .bind(MemberRegistrationDTO::getDateOfBirth, MemberRegistrationDTO::setDateOfBirth);
        binder.forField(enabledField)
                .bind(MemberRegistrationDTO::isEnabled, MemberRegistrationDTO::setEnabled);
        binder.forField(verifiedField)
                .bind(MemberRegistrationDTO::isVerified, MemberRegistrationDTO::setVerified);
    }



    @Getter
    @Setter
    private static class MemberContactDetailsForm extends Tab{
        private final Checkbox withContactDetails = new Checkbox("Save Contact Details");

        private final TextField phoneNumberField = new TextField("Phone Number");
        private final TextField streetField = new TextField("Street");
        private final TextField houseNumberField = new TextField("House Number");
        private final TextField postalCodeField = new TextField("Postal Code");
        private final TextField cityField = new TextField("City");
        private final TextField countryField = new TextField("Country");

        private final BeanValidationBinder<MemberRegistrationDTO> binder;

        public MemberContactDetailsForm(BeanValidationBinder<MemberRegistrationDTO> binder) {
            this.binder = binder;
            VerticalLayout layout = new VerticalLayout();
            HorizontalLayout streetAndHouseNumber = new HorizontalLayout(streetField, houseNumberField);
            HorizontalLayout postalCodeAndCity = new HorizontalLayout(postalCodeField, cityField);
            layout.add(withContactDetails, phoneNumberField, streetAndHouseNumber, postalCodeAndCity, countryField);
            configureBinder();
            add(layout);
        }

        private void updateContactDetailsRequired(boolean required) {
            streetField.setRequired(required);
            streetField.setRequiredIndicatorVisible(required);
            houseNumberField.setRequired(required);
            houseNumberField.setRequiredIndicatorVisible(required);
            postalCodeField.setRequired(required);
            postalCodeField.setRequiredIndicatorVisible(required);
            cityField.setRequired(required);
            cityField.setRequiredIndicatorVisible(required);
            countryField.setRequired(required);
            countryField.setRequiredIndicatorVisible(required);
        }

        private void configureBinder() {
            binder.forField(withContactDetails)
                    .bind(MemberRegistrationDTO::isWithContactDetails, MemberRegistrationDTO::setWithContactDetails);

            withContactDetails.addValueChangeListener(event ->
                    updateContactDetailsRequired(event.getValue())
            );

            binder.forField(phoneNumberField)
                    .bind(MemberRegistrationDTO::getPhoneNumber, MemberRegistrationDTO::setPhoneNumber);
            binder.forField(streetField)
                    .withValidator(value ->
                                    !getWithContactDetails().getValue() || (value != null && !value.isBlank()),
                            "Street is required")
                    .bind(MemberRegistrationDTO::getStreet, MemberRegistrationDTO::setStreet);
            binder.forField(houseNumberField)
                    .withValidator(value ->
                                    !getWithContactDetails().getValue() || (value != null && !value.isBlank()),
                            "House number is required")
                    .bind(MemberRegistrationDTO::getHouseNumber, MemberRegistrationDTO::setHouseNumber);
            binder.forField(postalCodeField)
                    .withValidator(value ->
                                    !getWithContactDetails().getValue() || (value != null && !value.isBlank()),
                            "Postal code is required")
                    .bind(MemberRegistrationDTO::getPostalCode, MemberRegistrationDTO::setPostalCode);
            binder.forField(cityField)
                    .withValidator(value ->
                                    !getWithContactDetails().getValue() || (value != null && !value.isBlank()),
                            "City is required")
                    .bind(MemberRegistrationDTO::getCity, MemberRegistrationDTO::setCity);
            binder.forField(countryField)
                    .withValidator(value ->
                                    !getWithContactDetails().getValue() || (value != null && !value.isBlank()),
                            "Country is required")
                    .bind(MemberRegistrationDTO::getCountry, MemberRegistrationDTO::setCountry);
        }
    }

    @Getter
    @Setter
    private static class MembershipDetailsForm extends Tab{
        private final Checkbox withMembershipDetails = new Checkbox("Create Membership");

        private final Select<String> membershipTypeField = new Select<>("Membership Type");
        private final Select<String> membershipStatusField = new Select<>("Membership Status");
        private final DatePicker membershipStartDateField = new DatePicker("Membership Start Date");
        private final DatePicker membershipEndDateField = new DatePicker("Membership End Date");
        private final DatePicker membershipRenewalDateField = new DatePicker("Membership Renewal Date");
        private final DatePicker membershipCancellationDateField = new DatePicker("Membership Cancellation Date");

        private final BeanValidationBinder<MemberRegistrationDTO> binder;

        private final HashMap<String, Integer> membershipPeriodsInMonths;


        public MembershipDetailsForm(BeanValidationBinder<MemberRegistrationDTO> binder, HashMap<String, Integer> membershipPeriodsInMonths) {
            this.binder = binder;
            this.membershipPeriodsInMonths = membershipPeriodsInMonths;
            VerticalLayout layout = new VerticalLayout();
            HorizontalLayout membershipTypeAndStatus = new HorizontalLayout(membershipTypeField, membershipStatusField);
            HorizontalLayout membershipStartAndEndDate = new HorizontalLayout(membershipStartDateField, membershipEndDateField);
            HorizontalLayout membershipRenewalAndCancellationDate = new HorizontalLayout(membershipRenewalDateField, membershipCancellationDateField);
            layout.add(withMembershipDetails, membershipTypeAndStatus, membershipStartAndEndDate, membershipRenewalAndCancellationDate);

            membershipTypeField.setItems(Arrays.stream(Membership.MembershipType.values()).map(Enum::name).collect(Collectors.toList()));
            membershipStatusField.setItems(Arrays.stream(Membership.MembershipStatus.values()).map(Enum::name).collect(Collectors.toList()));

            configureBinder();
            setChangeListener();
            add(layout);
        }

        private void updateMembershipDetailsRequired(boolean required) {
            membershipStartDateField.setRequired(required);
            membershipStartDateField.setRequiredIndicatorVisible(required);
            membershipTypeField.setRequiredIndicatorVisible(required);
            membershipStatusField.setRequiredIndicatorVisible(required);
            membershipEndDateField.setRequired(required);
            membershipEndDateField.setRequiredIndicatorVisible(required);
        }

        private void configureBinder() {
            binder.forField(withMembershipDetails)
                    .bind(MemberRegistrationDTO::isWithMembership, MemberRegistrationDTO::setWithMembership);

            withMembershipDetails.addValueChangeListener(event ->
                    updateMembershipDetailsRequired(event.getValue())
            );
            binder.forField(membershipTypeField)
                    .withValidator(value ->
                                    !getWithMembershipDetails().getValue() || (value != null && !value.isBlank()),
                            "Membership type is required")
                    .bind(MemberRegistrationDTO::getMembershipType, MemberRegistrationDTO::setMembershipType);
            binder.forField(membershipStatusField)
                    .withValidator(value ->
                                    !getWithMembershipDetails().getValue() || (value != null && !value.isBlank()),
                            "Membership status is required")
                    .bind(MemberRegistrationDTO::getMembershipStatus, MemberRegistrationDTO::setMembershipStatus);
            binder.forField(membershipStartDateField)
                    .withValidator(value ->
                                    !getWithMembershipDetails().getValue() || (value != null),
                            "Membership start date is required")
                    .bind(MemberRegistrationDTO::getMembershipStartDate, MemberRegistrationDTO::setMembershipStartDate);
            binder.forField(membershipEndDateField)
                    .withValidator(value ->
                                    !getWithMembershipDetails().getValue() || (value != null),
                            "Membership end date is required")
                    .bindReadOnly(MemberRegistrationDTO::getMembershipEndDate);
            binder.forField(membershipRenewalDateField)
                    .bind(MemberRegistrationDTO::getMembershipRenewalDate, MemberRegistrationDTO::setMembershipRenewalDate);
            binder.forField(membershipCancellationDateField)
                    .bind(MemberRegistrationDTO::getMembershipCancellationDate, MemberRegistrationDTO::setMembershipCancellationDate);
        }

        private void setChangeListener() {
            membershipStartDateField.addValueChangeListener(_ -> updateExpirationDate());
            membershipTypeField.addValueChangeListener(_ -> updateExpirationDate());
        }

        private void updateExpirationDate() {
            LocalDate startDate = membershipStartDateField.getValue();
            if (startDate == null)
                return;
            String membershipType = membershipTypeField.getValue();
            if (membershipType == null)
                return;
            if (membershipPeriodsInMonths.containsKey(membershipType)) {
                membershipEndDateField.setValue(startDate.plusMonths(membershipPeriodsInMonths.get(membershipType)));
            }
        }
    }
}
