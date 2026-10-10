package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.members.service.MemberService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class MemberManagementView extends VerticalLayout {
    
    private final MemberService memberService;
    
    private final Grid<MemberDTO> memberGrid;
    private final TextField searchField;
    
    public MemberManagementView(MemberService memberService) {
        this.memberService = memberService;
        this.memberGrid = getPreconfiguredGrid();

        this.searchField = getPreconfiguredSearchField();

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        toolbar.addToStart(searchField);

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
                .setAutoWidth(true);

        grid.addColumn(MemberDTO::lastModifiedTimestamp)
                .setHeader("Last Modified")
                .setSortProperty("updatedAt")
                .setAutoWidth(true);

        grid.setItemsPageable(pageable -> memberService.getMembers(searchField.getValue().trim(), pageable).getContent());
        grid.setSizeFull();
        return grid;
    }

}
