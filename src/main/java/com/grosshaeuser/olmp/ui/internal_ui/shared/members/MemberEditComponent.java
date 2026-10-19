package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Delegatable;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import java.util.HashMap;
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

    @FunctionalInterface
    interface AccountDataChangedCallback {
        void accountDataChanged(boolean value);
    }

    @FunctionalInterface
    interface ProfileDataChangedCallback {
        void profileDataChanged(boolean value);
    }

    private final UpdateCallback<MemberEditDTO> updateCallback;
    private final ErrorCallback errorCallback;
    private final Runnable closeCallback;

    private final MemberEditComponentProfileForm memberProfileEditForm;
    private final MemberEditComponentAccountForm memberAccountEditForm;
    private final MemberEditComponentContactInformationForm memberContactInformationEditForm;
    private final MemberEditComponentMembershipForm membershipDetailsEditForm;

    private final Button saveButton = new Button("Save");
    private final Button closeButton = new Button("Close");

    private boolean profileDataChanged;
    private boolean accountDataChanged;
    private boolean contactInformationDataChanged;
    private boolean membershipDataChanged;


    public MemberEditComponent(
            MemberEditDTO memberEditDTO,
            Set<String> membershipTypes,
            Set<String> membershipStatuses,
            UpdateCallback<MemberEditDTO> updateCallback,
            ErrorCallback errorCallback,
            Runnable closeCallback,
            HashMap<String, Integer> membershipPeriodsInMonths,
            PasswordProperties passwordProperties
    ) {
        this.updateCallback = updateCallback;
        this.errorCallback = errorCallback;
        this.closeCallback = closeCallback;

        configureActions();

        memberProfileEditForm = new MemberEditComponentProfileForm(
                memberEditDTO,
                value -> {
                    profileDataChanged = value;
                    handleDataChange();
                }
        );
        memberAccountEditForm = new MemberEditComponentAccountForm(
                memberEditDTO.getAccount(),
                passwordProperties,
                value -> {
                    accountDataChanged = value;
                    handleDataChange();
                }
        );
        memberContactInformationEditForm = new MemberEditComponentContactInformationForm(
                memberEditDTO.getContact(), value -> {
                    contactInformationDataChanged = value;
                    memberEditDTO.setWithContactInformation(value);
                    memberProfileEditForm.loadMember(memberEditDTO);
                    handleDataChange();
                });
        membershipDetailsEditForm = new MemberEditComponentMembershipForm(
                memberEditDTO.getMembership(),
                membershipTypes,
                membershipStatuses,
                membershipPeriodsInMonths,
                value -> {
                    membershipDataChanged = value;
                    memberEditDTO.setWithMembershipDetails(value);
                    memberProfileEditForm.loadMember(memberEditDTO);
                    handleDataChange();
                }
        );

        configureRootLayout();

        getContent().add(
                createComponentHeader("Member details", "Review profile information, account state, contact information, and membership details."),
                createFormElementBase(),
                createActionBar(new Button[]{saveButton}, new Button[]{}, new Button[]{closeButton})
        );

        accountDataChanged = false;
        contactInformationDataChanged = false;
        membershipDataChanged = false;
        handleDataChange();
    }

    private void configureRootLayout() {
        VerticalLayout layout = getContent();
        layout.addClassName("member-edit-component");
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
    }

    private HorizontalLayout createFormElementBase() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.add(memberProfileEditForm);

        TabSheet tabSheet = new TabSheet();
        tabSheet.add("Account", memberAccountEditForm);
        tabSheet.add("Contact Information", memberContactInformationEditForm);
        tabSheet.add("Membership Details", membershipDetailsEditForm);

        layout.add(tabSheet);
        return layout;
    }



    private void configureActions() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);
        saveButton.addClickListener(_ -> save());

        closeButton.addClickListener(_ -> closeCallback.run());
    }

    public void save() {
        memberProfileEditForm.getFormDataObject().ifPresent(dto -> {
            try {
                dto.setAccount(memberAccountEditForm.getFormDataObject().orElse(null));
                dto.setMembership(membershipDetailsEditForm.getFormDataObject().orElse(null));
                dto.setContact(memberContactInformationEditForm.getFormDataObject().orElse(null));
                MemberEditDTO savedUser = updateCallback.update(dto);
                memberProfileEditForm.loadMember(savedUser);
                memberProfileEditForm.updateSnapshot();
                memberAccountEditForm.loadMemberAccount(savedUser.getAccount());
                membershipDetailsEditForm.loadMembershipDetails(savedUser.getMembership());
                memberContactInformationEditForm.loadMembershipContactInformation(savedUser.getContact());
                handleDataChange();
            } catch (RuntimeException e) {
                errorCallback.handleException(e);
            } finally {
                handleDataChange();
            }
        });
    }

    private void handleDataChange() {
        saveButton.setEnabled(profileDataChanged || accountDataChanged || contactInformationDataChanged || membershipDataChanged);
    }

}
