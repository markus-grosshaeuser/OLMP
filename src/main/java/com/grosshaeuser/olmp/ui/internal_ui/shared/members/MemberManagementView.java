package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.grosshaeuser.olmp.memberships.service.MembershipCardPrintService;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiNotificationHandler;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MemberManagementView extends VerticalLayout {
    private final MemberService memberService;
    private final MembershipService membershipService;
    private final MembershipCardPrintService printService;
    private final PasswordProperties passwordProperties;
    private final UiExceptionHandler uiExceptionHandler;
    private final UiNotificationHandler uiNotificationHandler;
    private final TextField searchField = new TextField("Search Members");
    private final Grid<MemberDTO> memberGrid = new Grid<>(MemberDTO.class, false);

    public MemberManagementView(
            MemberService memberService,
            MembershipService membershipService,
            MembershipCardPrintService printService,
            PasswordProperties passwordProperties,
            UiExceptionHandler uiExceptionHandler,
            UiNotificationHandler uiNotificationHandler
    ) {
        this.memberService = memberService;
        this.membershipService = membershipService;
        this.printService = printService;
        this.passwordProperties = passwordProperties;
        this.uiExceptionHandler = uiExceptionHandler;
        this.uiNotificationHandler = uiNotificationHandler;

        configureSearch();
        configureGrid();

        Button create = new Button("Create Member", VaadinIcon.PLUS.create(), _ -> openCreateDialog());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout toolbar = new HorizontalLayout(searchField, create);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setFlexGrow(1, searchField);
        toolbar.setWrap(true);
        add(new H2("Member management"), toolbar, memberGrid);
        setSizeFull();
        setFlexGrow(1, memberGrid);
    }

    private void configureSearch() {
        searchField.setPlaceholder("Search ...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);
        searchField.addValueChangeListener(_ -> refreshGrid());
    }

    private void configureGrid() {
        memberGrid.addColumn(MemberDTO::lastName)
                .setHeader("Last Name")
                .setKey("lastName")
                .setSortProperty("lastName")
                .setAutoWidth(true)
                .setResizable(true);

        memberGrid.addColumn(MemberDTO::firstName)
                .setHeader("First Name")
                .setKey("firstName")
                .setSortProperty("firstName")
                .setAutoWidth(true)
                .setResizable(true);

        memberGrid.addColumn(MemberDTO::dateOfBirth)
                .setHeader("Date of Birth")
                .setKey("dateOfBirth")
                .setSortProperty("dateOfBirth")
                .setAutoWidth(true)
                .setResizable(true);

        memberGrid.addColumn(MemberDTO::creationTimestamp)
                .setHeader("Created")
                .setKey("createdAt")
                .setSortProperty("createdAt")
                .setAutoWidth(true)
                .setResizable(true);

        memberGrid.addColumn(MemberDTO::lastModifiedTimestamp)
                .setHeader("Last Modified")
                .setKey("updatedAt")
                .setSortProperty("updatedAt")
                .setAutoWidth(true)
                .setResizable(true);

        memberGrid.addComponentColumn(member -> {
            Button edit = new Button(VaadinIcon.EDIT.create(), _ -> memberGrid.setDetailsVisible(member, !memberGrid.isDetailsVisible(member)));
            edit.setAriaLabel("Edit "+ member.firstName() + " " + member.lastName());
            edit.addThemeVariants(ButtonVariant.LUMO_ICON);

            Button delete = new Button(VaadinIcon.TRASH.create(), _ -> confirmDelete(member));
            delete.setAriaLabel("Delete "+ member.firstName() + " " + member.lastName());
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Span actionElements = new Span(edit, delete);
            actionElements.setClassName("grid-action-elements");
            return actionElements;
        })
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        memberGrid.setSelectionMode(Grid.SelectionMode.NONE);
        memberGrid.setDetailsVisibleOnClick(false);
        memberGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createEditor));
        memberGrid.addThemeVariants(GridVariant.ROW_STRIPES, GridVariant.WRAP_CELL_CONTENT);
        memberGrid.setSizeFull();
        memberGrid.setItemsPageable(this::fetchMembers, _ -> countMembers()).setIdentifierProvider(MemberDTO::id);
        memberGrid.sort(List.of(new GridSortOrder<>(memberGrid.getColumnByKey("lastName"), SortDirection.ASCENDING)));
        memberGrid.setEmptyStateText("No members found");
    }

    private List<MemberDTO> fetchMembers(Pageable pageable) {
        try {
            return memberService.getMembers(searchField.getValue().strip(), pageable).getContent();
        } catch (RuntimeException exception) {
            uiExceptionHandler.handleException(exception);
            return List.of();
        }
    }

    private long countMembers() {
        try {
            return memberService.countMembers(searchField.getValue().strip());
        } catch (RuntimeException exception) {
            uiExceptionHandler.handleException(exception);
            return 0;
        }
    }

    private Component createEditor(MemberDTO member) {
        try {
            return memberService.getDetailedMember(member.id())
                    .<Component>map(dto -> new MemberEditComponent(
                            dto,
                            Arrays.stream(Membership.MembershipType.values()).map(Membership.MembershipType::name).collect(Collectors.toSet()),
                            Arrays.stream(Membership.MembershipStatus.values()).map(Membership.MembershipStatus::name).collect(Collectors.toSet()),
                            this::updateMember,
                            uiExceptionHandler::handleException,
                            () -> memberGrid.setDetailsVisible(member, false),
                            printService,
                            membershipService.getMembershipPeriodsInMonths(),
                            passwordProperties)
                    )
                    .orElseGet(() -> unavailableDetails("This member does not exist."));
        } catch (RuntimeException exception) {
            uiExceptionHandler.handleException(exception);
            return unavailableDetails("Member details could not be loaded.");
        }
    }

    private Component unavailableDetails(String message) {
        return new VerticalLayout(new Span(message), new Button("Refresh list", _ -> refreshGrid()));
    }

    private void openCreateDialog() {
        try {
            new MemberCreateDialog(
                    this::createMember,
                    uiExceptionHandler::handleException,
                    membershipService.getMembershipPeriodsInMonths(),
                    passwordProperties
            ).open();
        } catch(RuntimeException exception) {
            uiExceptionHandler.handleException(exception);
        }
    }

    private void createMember(MemberRegistrationDTO dto) {
        try {
            memberService.createMember(dto).ifPresentOrElse(member -> {
                if (searchField.isEmpty()) {
                    refreshGrid();
                } else {
                    searchField.clear();
                }
                offerMembershipCardPrint(member.id());
            },
                    () -> uiExceptionHandler.handleException(new RuntimeException("Member creation failed.")));
        } catch (Exception exception) {
            uiExceptionHandler.handleException(new RuntimeException("An unexpected error occurred while creating a new member account."));
        }
    }

    private void offerMembershipCardPrint(Long id) {
        memberService.getDetailedMember(id)
                .ifPresent(member -> {
                    MembershipEditDTO newMembership = member.getMemberships().getFirst();
                    new MembershipCardPrintDialog(printService, member, newMembership).open();
                });
    }

    private MemberEditDTO updateMember(MemberEditDTO dto) {
        return memberService.updateMember(dto).map(result -> {
            uiNotificationHandler.showSuccessNotification("Member " +
                    result.getFirstName() + " " + result.getLastName() +
                    " was updated successfully.");
            memberGrid.getDataProvider().refreshAll();
            return result;
        }).orElse(null);
    }

    private void confirmDelete(MemberDTO member) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.addClassName("confirm-delete-dialog");
        dialog.setHeader("Delete user account?");
        dialog.setText(new Html("<p>You are about to delete the user account of:<br /><em> " +
                member.firstName() + " " + member.lastName() +
                "</em><br />This action cannot be undone!</p>"));
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(_ -> delete(member));
        dialog.open();
    }

    private void delete(MemberDTO member) {
        memberService.deleteMember(member.id());
        refreshGrid();
    }

    private void refreshGrid() {
        memberGrid.getDataProvider().refreshAll();
        memberGrid.scrollToIndex(0);
    }


}