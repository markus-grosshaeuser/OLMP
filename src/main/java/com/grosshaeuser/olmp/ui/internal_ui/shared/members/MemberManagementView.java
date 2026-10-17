package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiExceptionHandler;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiNotificationHandler;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MemberManagementView extends VerticalLayout {
    
    private final MemberService memberService;
    private final MembershipService membershipService;

    private final UiExceptionHandler uiExceptionHandler;
    private final UiNotificationHandler uiNotificationHandler;

    private final PasswordProperties passwordProperties;
    
    private final Grid<MemberDTO> memberGrid;
    private final TextField searchField;
    
    public MemberManagementView(
            MemberService memberService,
            MembershipService membershipService,
            PasswordProperties passwordProperties,
            UiExceptionHandler uiExceptionHandler,
            UiNotificationHandler uiNotificationHandler
    ) {
        this.memberService = memberService;
        this.membershipService = membershipService;

        this.uiExceptionHandler = uiExceptionHandler;
        this.uiNotificationHandler = uiNotificationHandler;

        this.passwordProperties = passwordProperties;
        this.memberGrid = getPreconfiguredGrid();

        this.searchField = getPreconfiguredSearchField();

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        toolbar.addToStart(searchField);
        toolbar.addToEnd(getPreconfiguredCreateMemberButton());

        this.add(toolbar);
        this.add(memberGrid);

        this.setSizeFull();
    }

    private TextField getPreconfiguredSearchField() {
        TextField textField = new TextField();
        textField.setPlaceholder("Search ...");
        textField.setPrefixComponent(VaadinIcon.SEARCH.create());
        textField.setValueChangeMode(ValueChangeMode.LAZY);
        textField.setClearButtonVisible(true);
        textField.addValueChangeListener(
                _ ->
                        this.memberGrid.getDataProvider().refreshAll()
        );
        return textField;
    }

    private Button getPreconfiguredCreateMemberButton() {
        Button button = new Button("Create Member");
        button.addClickListener(
                _ ->
                        new MemberCreateDialog(
                                this::saveNewMember,
                                uiExceptionHandler::handleException,
                                membershipService.getMembershipPeriodsInMonths(),
                                passwordProperties
                        ).open()
        );
        return button;
    }
    
    private Grid<MemberDTO> getPreconfiguredGrid() {
        Grid<MemberDTO> grid = new Grid<>();

        grid.addColumn(MemberDTO::lastName)
                .setHeader("Last Name")
                .setSortProperty("lastName")
                .setAutoWidth(true);

        grid.addColumn(MemberDTO::firstName)
                .setHeader("First Name")
                .setSortProperty("firstName")
                .setAutoWidth(true);

        grid.addColumn(MemberDTO::dateOfBirth)
                .setHeader("Date of Birth")
                .setSortProperty("dateOfBirth")
                .setAutoWidth(true);

        grid.addColumn(MemberDTO::creationTimestamp)
                .setHeader("Created")
                .setSortProperty("createdAt")
                .setKey("createdAt")
                .setAutoWidth(true);

        grid.addColumn(MemberDTO::lastModifiedTimestamp)
                .setHeader("Last Modified")
                .setSortProperty("updatedAt")
                .setAutoWidth(true);

        grid.setItemsPageable(pageable -> memberService.getMembers(searchField.getValue().trim(), pageable).getContent());
        grid.setDetailsVisibleOnClick(true);
        grid.setItemDetailsRenderer(createMemberRenderer());
        grid.setEmptyStateText("No members found");
        grid.setSizeFull();
        return grid;
    }

    private Renderer<MemberDTO> createMemberRenderer() {
        return new ComponentRenderer<>(element -> memberService
                .getDetailedMember(element.id())
                .map(memberEditDTO -> new MemberEditComponent(
                        memberEditDTO,
                        Arrays.stream(Membership.MembershipType.values()).map(Membership.MembershipType::name).collect(Collectors.toSet()),
                        Arrays.stream(Membership.MembershipStatus.values()).map(Membership.MembershipStatus::name).collect(Collectors.toSet()),
                        this::updateMember,
                        this::deleteMember,
                        uiExceptionHandler::handleException,
                        passwordProperties
                ))
                .orElse(null)
        );
    }

    private MemberEditDTO updateMember(MemberEditDTO dto) {
        return memberService.updateMember(dto).map(result -> {
            uiNotificationHandler.showSuccessNotification("Member " + result.getFirstName() + " " + result.getLastName() + " was updated successfully.");
            memberGrid.getDataProvider().refreshAll();
            memberService.getMembers(searchField.getValue().trim(), null)
                    .getContent()
                    .stream()
                    .filter(member -> member.id().equals(result.getId()))
                    .findFirst().ifPresent(refreshedRow -> memberGrid.setDetailsVisible(refreshedRow, false));
            return result;
        }).orElse(null);
    }

    private void deleteMember(MemberEditDTO dto) {
        memberService.deleteMember(dto.getId());
        memberGrid.getDataProvider().refreshAll();
    }

    private void saveNewMember(MemberRegistrationDTO dto) {
        memberService.createMember(dto).ifPresent(member -> {
            uiNotificationHandler.showSuccessNotification("New member account for " + member.firstName() + " " + member.lastName() + " was created successfully.");
            searchField.clear();
            memberGrid.getDataProvider().refreshAll();
            memberGrid.sort(GridSortOrder.desc(memberGrid.getColumnByKey("createdAt")).build());
            memberGrid.select(member);
            memberGrid.scrollToIndex(0);
        });
    }

}
