package com.grosshaeuser.olmp.ui.internal_ui.shared.members;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.MembershipType;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.ui.internal_ui.shared.members.MemberEditComponent.WithMembershipDetailsCallback;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

class MemberEditComponentMembershipForm extends Composite<VerticalLayout> {
    private final MemberContext memberContext;

    private final VerticalLayout membershipRows = new VerticalLayout();
    private final List<MembershipRow> memberships = new ArrayList<>();
    private final HashMap<String, Integer> membershipPeriodsInMonths;
    private final WithMembershipDetailsCallback callback;
    private final MemberEditDTO memberDTO;
    private List<MembershipEditDTO> snapshot = List.of();

    MemberEditComponentMembershipForm(MemberContext memberContext, MemberEditDTO memberDTO, WithMembershipDetailsCallback callback) {
        this.memberContext = memberContext;
        this.callback = callback;
        this.memberDTO = memberDTO;
        this.membershipPeriodsInMonths = memberContext.getMembershipService().getMembershipPeriodsInMonths();

        Button addMembership = new Button("Add membership", VaadinIcon.PLUS.create(), _ -> {
            addNewMembership(MembershipEditDTO.builder()
                    .membershipType("STANDARD")
                    .membershipStatus("ACTIVE")
                    .startDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusMonths(membershipPeriodsInMonths.getOrDefault("STANDARD", 12)))
                    .build());
            handleDataChange();
        });

        Scroller scroller = new Scroller();
        scroller.addThemeName("overflow-indicators");
        scroller.setMaxHeight(400, Unit.PIXELS);
        scroller.setContent(membershipRows);

        getContent().add(addMembership, scroller);
        loadMembershipDetails(memberDTO.getMemberships());
    }

    void loadMembershipDetails(List<MembershipEditDTO> dtos) {
        memberships.clear();
        membershipRows.removeAll();
        snapshot = dtos.stream().map(dto -> dto.toBuilder().build()).toList();
        snapshot.forEach(this::addMembership);
        handleDataChange();
    }

    private void addMembership(MembershipEditDTO dto) {
        MembershipRow row = new MembershipRow(memberDTO, dto);
        memberships.add(row);
        membershipRows.add(row);
    }

    private void addNewMembership(MembershipEditDTO dto) {
        MembershipRow row = new MembershipRow(memberDTO, dto);
        memberships.addFirst(row);
        membershipRows.addComponentAsFirst(row);
    }

    private void handleDataChange() {
        callback.withMembership(getFormDataObject()
                .map(data -> !snapshot.equals(data))
                .orElse(true));
    }

    Optional<List<MembershipEditDTO>> getFormDataObject() {
        List<MembershipEditDTO> data = new ArrayList<>();
        boolean valid = true;
        for (MembershipRow row : memberships) {
            MembershipEditDTO dto = row.original.toBuilder().build();
            if (row.binder.writeBeanIfValid(dto)) {
                data.add(dto);
            } else {
                valid = false;
            }
        }
        return valid ? Optional.of(data) : Optional.empty();
    }

    private class MembershipRow extends VerticalLayout implements Printing{
        private final MembershipEditDTO original;
        private final BeanValidationBinder<MembershipEditDTO> binder = new BeanValidationBinder<>(MembershipEditDTO.class);
        private final Select<String> membershipType = new Select<>();
        private final DatePicker startDate = new DatePicker("Start date");
        private final DatePicker expirationDate = new DatePicker("Expiration date");

        MembershipRow(MemberEditDTO memberDTO, MembershipEditDTO membershipDTO) {
            original = membershipDTO.toBuilder().build();
            TextField number = new TextField("Membership number");
            number.setPlaceholder("Generated on save");
            Select<String> status = new Select<>();
            membershipType.setLabel("Type");
            membershipType.setItems(Arrays.stream(MembershipType.values()).map(MembershipType::name).collect(Collectors.toSet()));
            membershipType.setEmptySelectionAllowed(false);
            status.setLabel("Status");
            status.setItems(Arrays.stream(MembershipStatus.values()).map(MembershipStatus::name).collect(Collectors.toSet()));
            status.setEmptySelectionAllowed(false);
            DatePicker renewalDate = new DatePicker("Renewal date");
            DatePicker cancellationDate = new DatePicker("Cancellation date");

            binder.forField(number)
                    .withNullRepresentation("")
                    .bindReadOnly(MembershipEditDTO::getMembershipNumber);
            binder.forField(membershipType).asRequired("Choose a membership type")
                    .bind(MembershipEditDTO::getMembershipType, MembershipEditDTO::setMembershipType);
            binder.forField(status).asRequired("Choose a membership status")
                    .bind(MembershipEditDTO::getMembershipStatus, MembershipEditDTO::setMembershipStatus);
            if (membershipDTO.getId() == null) {
                binder.forField(startDate).asRequired("Choose a start date")
                        .bind(MembershipEditDTO::getStartDate, MembershipEditDTO::setStartDate);
            } else {
                binder.forField(startDate).bindReadOnly(MembershipEditDTO::getStartDate);
            }
            binder.forField(expirationDate).bind(MembershipEditDTO::getExpirationDate, MembershipEditDTO::setExpirationDate);
            binder.forField(renewalDate).bind(MembershipEditDTO::getRenewalDate, MembershipEditDTO::setRenewalDate);
            binder.forField(cancellationDate).bind(MembershipEditDTO::getCancellationDate, MembershipEditDTO::setCancellationDate);
            binder.readBean(membershipDTO);

            membershipType.addValueChangeListener(_ -> updateExpirationDate());
            startDate.addValueChangeListener(_ -> updateExpirationDate());
            binder.addValueChangeListener(_ -> handleDataChange());

            setWidthFull();
            addClassName("membership-row");
            setWrap(true);
            setAlignItems(FlexComponent.Alignment.END);

            HorizontalLayout baseData = new HorizontalLayout();
            baseData.setWidthFull();
            baseData.setAlignItems(FlexComponent.Alignment.BASELINE);

            Button printButton = new Button(VaadinIcon.PRINT.create(), _ -> {
                String document = memberContext.createPrintDocument(memberDTO, membershipDTO);
                UI.getCurrentOrThrow().getPage().executeJs(jsPrintExpression, document);
            });

            baseData.add(number, printButton, membershipType, status);
            HorizontalLayout dates = new HorizontalLayout();
            dates.setWidthFull();
            dates.add(startDate, expirationDate, renewalDate, cancellationDate);
            add(baseData, dates);
            baseData.setFlexGrow(1, number);
            if (membershipDTO.getId() == null) {
                Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                    memberships.remove(this);
                    membershipRows.remove(this);
                    handleDataChange();
                });
                remove.addThemeVariants(ButtonVariant.LUMO_ERROR);
                remove.setAriaLabel("Remove new membership");
                remove.setTooltipText("Remove new membership");
                add(remove);
            }
        }

        private void updateExpirationDate() {
            Integer months = membershipPeriodsInMonths.get(membershipType.getValue());
            if (startDate.getValue() != null && months != null) {
                expirationDate.setValue(startDate.getValue().plusMonths(months));
            }
        }
    }
}
