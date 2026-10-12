package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.members.dto.MemberCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

public class MemberManagementView extends VerticalLayout {
    
    private final MemberService memberService;
    private final MembershipService membershipService;
    
    private final Grid<MemberDTO> memberGrid;
    private final TextField searchField;
    
    public MemberManagementView(MemberService memberService, MembershipService membershipService) {
        this.memberService = memberService;
        this.membershipService = membershipService;
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
                                this::handleException,
                                membershipService.getMembershipPeriodsInMonths()
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
        grid.setSizeFull();
        return grid;
    }

    private void saveNewMember(MemberCreateDTO dto) {
        memberService.createMember(dto).ifPresent(member -> {
            searchField.clear();
            memberGrid.getDataProvider().refreshItem(member);
            memberGrid.sort(GridSortOrder.desc(memberGrid.getColumnByKey("createdAt")).build());
            memberGrid.select(member);
            memberGrid.scrollToIndex(0);
        });
    }

    private void handleException(RuntimeException e) {
        switch (e) {
            case OptimisticLockingFailureException _ -> {
                var notification = new Notification("Someone has edited the same Employee. Please refresh and try again.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.WARNING);
                notification.setDuration(3000);
                notification.open();
            }
            case AccessDeniedException _, IllegalArgumentException _ -> {
                var notification = new Notification(e.getMessage());
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.ERROR);
                notification.setDuration(4000);
                notification.open();
            }
            case null -> {}
            default -> throw e;
        }
    }

}
