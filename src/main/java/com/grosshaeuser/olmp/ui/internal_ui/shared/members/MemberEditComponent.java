package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.ui.internal_ui.shared.CrudCallbacks;
import com.grosshaeuser.olmp.ui.internal_ui.shared.Structurable;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

public class MemberEditComponent extends Composite<VerticalLayout> implements Structurable, CrudCallbacks {

    @FunctionalInterface
    public interface CloseCallback {
        void close();
    }

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

    private final MemberContext memberContext;
    private final UiContext uiContext;

    private final UpdateCallback<MemberEditDTO> updateCallback;
    private final CloseCallback closeCallback;

    private MemberEditComponentProfileForm memberProfileEditForm;
    private MemberEditComponentAccountForm memberAccountEditForm;
    private MemberEditComponentContactInformationForm memberContactInformationEditForm;
    private MemberEditComponentMembershipForm membershipDetailsEditForm;

    private final Button saveButton = new Button("Save");
    private final Button closeButton = new Button("Close");

    private boolean profileDataChanged;
    private boolean accountDataChanged;
    private boolean contactInformationDataChanged;
    private boolean membershipDataChanged;


    public MemberEditComponent(
            MemberContext memberContext,
            UiContext uiContext,
            MemberEditDTO memberEditDTO,
            UpdateCallback<MemberEditDTO> updateCallback,
            CloseCallback closeCallback
    ) {
        this.memberContext = memberContext;
        this.uiContext = uiContext;

        this.updateCallback = updateCallback;
        this.closeCallback = closeCallback;

        configureActions();
        configureForms(memberEditDTO);
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

    private void configureActions() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);
        saveButton.addClickListener(_ -> save());

        closeButton.addClickListener(_ -> closeCallback.close());
    }

    private void configureForms(MemberEditDTO memberEditDTO) {
        memberProfileEditForm = new MemberEditComponentProfileForm(
                memberEditDTO,
                value -> {
                    profileDataChanged = value;
                    handleDataChange();
                });
        memberAccountEditForm = new MemberEditComponentAccountForm(
                memberContext,
                memberEditDTO.getAccount(),
                value -> {
                    accountDataChanged = value;
                    handleDataChange();
                });
        memberContactInformationEditForm = new MemberEditComponentContactInformationForm(
                memberEditDTO.getContact(),
                value -> {
                    contactInformationDataChanged = value;
                    memberEditDTO.setWithContactInformation(value);
                    memberProfileEditForm.loadMember(memberEditDTO);
                    handleDataChange();
                });
        membershipDetailsEditForm = new MemberEditComponentMembershipForm(
                memberContext,
                memberEditDTO,
                value -> {
                    membershipDataChanged = value;
                    handleDataChange();
                }
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

    public void save() {
        var memberships = membershipDetailsEditForm.getFormDataObject();
        if (memberships.isEmpty()) {
            return;
        }
        memberProfileEditForm.getFormDataObject().ifPresent(dto -> {
            try {
                dto.setAccount(memberAccountEditForm.getFormDataObject().orElse(null));
                dto.setMemberships(memberships.get());
                dto.setWithMembershipDetails(membershipDataChanged);
                dto.setContact(memberContactInformationEditForm.getFormDataObject().orElse(null));
                MemberEditDTO savedUser = updateCallback.update(dto);
                memberProfileEditForm.loadMember(savedUser);
                memberProfileEditForm.updateSnapshot();
                memberAccountEditForm.loadMemberAccount(savedUser.getAccount());
                membershipDetailsEditForm.loadMembershipDetails(savedUser.getMemberships());
                memberContactInformationEditForm.loadMembershipContactInformation(savedUser.getContact());
                profileDataChanged = false;
                handleDataChange();
            } catch (RuntimeException e) {
                uiContext.handleException(e);
            } finally {
                handleDataChange();
            }
        });
    }

    private void handleDataChange() {
        saveButton.setEnabled(profileDataChanged || accountDataChanged || contactInformationDataChanged || membershipDataChanged);
    }

}
