package com.grosshaeuser.olmp.loans.mappers;

import com.grosshaeuser.olmp.employees.mapper.EmployeeMapper;
import com.grosshaeuser.olmp.inventory.mappers.InventoryItemMapper;
import com.grosshaeuser.olmp.shared.MappingChecks;
import com.grosshaeuser.olmp.loans.dto.LoanDto;
import com.grosshaeuser.olmp.loans.dto.LoanEditDto;
import com.grosshaeuser.olmp.loans.dto.LoanSummaryDto;
import com.grosshaeuser.olmp.loans.entity.Loan;
import com.grosshaeuser.olmp.members.mapper.MemberMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class LoanMapper {

    private final MemberMapper memberMapper;
    private final InventoryItemMapper inventoryItemMapper;
    private final EmployeeMapper employeeMapper;

    public LoanMapper(MemberMapper memberMapper, InventoryItemMapper inventoryItemMapper, EmployeeMapper employeeMapper) {
        this.memberMapper = memberMapper;
        this.inventoryItemMapper = inventoryItemMapper;
        this.employeeMapper = employeeMapper;
    }

    public LoanDto toDto(Loan source) {
        return source == null ? null : new LoanDto(
                source.getId(),
                memberMapper.mapMemberToMemberDTO(source.getMember()),
                inventoryItemMapper.toDetail(source.getInventoryItem()),
                source.getEmployee() == null ? null : employeeMapper.mapEmployeeToEmployeeDto(source.getEmployee()),
                source.getLoanDate(),
                source.getDueDate(),
                source.getReturnDate(),
                source.getStatus(),
                source.getNotes(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }

    public LoanSummaryDto toSummary(Loan source) {
        if (source == null) {
            return null;
        }
        return new LoanSummaryDto(
                source.getId(),
                source.getMember().getFirstName() + " " + source.getMember().getLastName(),
                source.getInventoryItem().getInventoryNumber(),
                source.getInventoryItem().getMedia().getTitle(),
                source.getDueDate(),
                source.getStatus()
        );
    }

    public LoanEditDto toEditDto(Loan source) {
        if (source == null) {
            return null;
        }
        return LoanEditDto.builder()
                .id(source.getId())
                .status(source.getStatus())
                .dueDate(source.getDueDate())
                .returnDate(source.getReturnDate())
                .notes(source.getNotes())
                .expectedUpdatedAt(source.getUpdatedAt())
                .build();
    }

    public void update(LoanEditDto source, Loan target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");

        MappingChecks.sameId(source.getId(), target.getId(), "id");

        target.setStatus(source.getStatus());
        target.setDueDate(source.getDueDate());
        target.setReturnDate(source.getReturnDate());
        target.setNotes(source.getNotes());
    }
}
