package com.grosshaeuser.olmp.loans.service.impl;

import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.inventory.entity.InventoryItem;
import com.grosshaeuser.olmp.loans.LoanStatus;
import com.grosshaeuser.olmp.loans.dto.*;
import com.grosshaeuser.olmp.loans.entity.Loan;
import com.grosshaeuser.olmp.loans.mappers.LoanMapper;
import com.grosshaeuser.olmp.loans.repository.LoanRepository;
import com.grosshaeuser.olmp.loans.service.LoanService;
import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.security.principal.EmployeePrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('" + ROOT_ADMIN + "', '" + ADMIN + "', '" + STAFF + "')")
public class LoanServiceImpl implements LoanService {
    private static final Set<LoanStatus> OPEN = Set.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE);
    // Lost copies remain unavailable until physically returned.
    private static final Set<LoanStatus> OUTSTANDING = Set.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE, LoanStatus.LOST);
    private static final Set<String> SORT_PROPERTIES = Set.of("id", "member.lastName", "inventoryItem.inventoryNumber",
            "inventoryItem.media.title", "dueDate", "loanDate");

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final EntityManager entityManager;
    private final Validator validator;

    @Override
    public List<LoanSummaryDto> findLoans(String query, LoanStatus status, Pageable pageable) {
        Sort sort = Sort.by(pageable.getSort().stream()
                .filter(order -> SORT_PROPERTIES.contains(order.getProperty())).toList());
        if (sort.isUnsorted()) {
            sort = Sort.by("dueDate");
        }
        return loanRepository.findAll(filter(query, status), PageRequest.of(pageable.getPageNumber(),
                        pageable.getPageSize(), sort.and(Sort.by("id"))))
                .stream().map(this::summary).toList();
    }

    @Override
    public long countLoans(String query, LoanStatus status) {
        return loanRepository.count(filter(query, status));
    }

    @Override
    public boolean hasOverdueLoans(String membershipNumber) {
        return membershipNumber != null && !membershipNumber.isBlank()
                && loanRepository.existsByMemberMembershipsMembershipNumberAndStatusInAndDueDateBefore(
                membershipNumber.strip(), OPEN, OffsetDateTime.now());
    }

    private Specification<Loan> filter(String query, LoanStatus status) {
        OffsetDateTime now = OffsetDateTime.now();
        String text = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        String pattern = "%" + text.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return (root, criteria, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!text.isEmpty()) {
                var member = root.join("member");
                var copy = root.join("inventoryItem");
                var memberships = criteria.subquery(Long.class);
                var membership = memberships.from(Membership.class);
                memberships.select(membership.get("member").get("id"))
                        .where(cb.like(cb.lower(membership.get("membershipNumber")), pattern, '!'));
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.concat(cb.concat(member.get("firstName"), " "), member.get("lastName"))), pattern, '!'),
                        cb.like(cb.lower(copy.get("inventoryNumber")), pattern, '!'),
                        cb.like(cb.lower(copy.get("barcode")), pattern, '!'),
                        cb.like(cb.lower(copy.join("media").get("title")), pattern, '!'),
                        member.get("id").in(memberships)));
            }
            if (status == LoanStatus.OVERDUE) {
                predicates.add(root.get("status").in(OPEN));
                predicates.add(cb.lessThan(root.get("dueDate"), now));
            } else if (status == LoanStatus.ACTIVE) {
                predicates.add(root.get("status").in(OPEN));
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), now));
            } else if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Override
    public Optional<LoanDto> findLoan(long id) {
        return loanRepository.findById(id).map(loan -> {
            LoanDto dto = loanMapper.toDto(loan);
            return new LoanDto(dto.id(), dto.member(), dto.inventoryItem(), dto.employee(), dto.loanDate(),
                    dto.dueDate(), dto.returnDate(), effectiveStatus(loan), dto.notes(), dto.createdAt(), dto.updatedAt());
        });
    }

    @Override
    public Optional<LoanCopyDto> findCopy(String inventoryNumberOrBarcode) {
        if (inventoryNumberOrBarcode == null || inventoryNumberOrBarcode.isBlank()) {
            return Optional.empty();
        }
        List<InventoryItem> copies = entityManager.createQuery("""
                select i from InventoryItem i join fetch i.media
                where i.inventoryNumber = :code or i.barcode = :code
                """, InventoryItem.class).setParameter("code", inventoryNumberOrBarcode.strip()).getResultList();
        if (copies.size() > 1) {
            throw new IllegalArgumentException("This code identifies multiple copies. Use the inventory number.");
        }
        return copies.stream().findFirst().map(copy -> new LoanCopyDto(copy.getId(), copy.getInventoryNumber(), copy.getMedia().getTitle()));
    }

    @Override
    @Transactional
    public List<LoanSummaryDto> createLoans(LoanBatchCreateDto dto) {
        validate(dto);
        Employee employee = currentEmployee();
        OffsetDateTime now = OffsetDateTime.now();
        if (!dto.getDueDate().isAfter(now)) {
            throw new IllegalArgumentException("The due date must be in the future.");
        }
        Membership membership = entityManager.createQuery("select m from Membership m where m.membershipNumber = :number", Membership.class)
                .setParameter("number", dto.getMembershipNumber().strip()).getResultStream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No membership found for this number."));
        LocalDate today = LocalDate.now();
        if (membership.getStatus() != MembershipStatus.ACTIVE || membership.getStartDate().isAfter(today)
                || (membership.getExpirationDate() != null && membership.getExpirationDate().isBefore(today))
                || (membership.getCancellationDate() != null && !membership.getCancellationDate().isAfter(today))) {
            throw new IllegalArgumentException("This membership is not currently active.");
        }
        List<InventoryItem> copies = lockCopies(dto.getInventoryItemIds());
        for (InventoryItem copy : copies) {
            if (copy.getWithdrawnAt() != null || copy.getCondition() == InventoryItem.Condition.LOST
                    || copy.getCondition() == InventoryItem.Condition.UNUSABLE) {
                throw new IllegalArgumentException("Copy " + copy.getInventoryNumber() + " is unavailable for lending.");
            }
            if (!outstanding(copy).isEmpty()) {
                throw new IllegalArgumentException("Copy " + copy.getInventoryNumber() + " already has an outstanding loan.");
            }
        }
        return loanRepository.saveAll(copies.stream().map(copy -> Loan.builder().member(membership.getMember())
                        .inventoryItem(copy).employee(employee).loanDate(now).dueDate(dto.getDueDate())
                        .status(LoanStatus.ACTIVE).notes(dto.getNotes()).build()).toList())
                .stream().map(this::summary).toList();
    }

    @Override
    @Transactional
    public void returnLoans(LoanBatchReturnDto dto) {
        validate(dto);
        currentEmployee();
        List<Loan> loans = new ArrayList<>();
        for (InventoryItem copy : lockCopies(dto.getInventoryItemIds())) {
            List<Loan> outstanding = outstanding(copy);
            if (outstanding.size() != 1) {
                throw new IllegalArgumentException("Copy " + copy.getInventoryNumber() + " does not have exactly one outstanding loan.");
            }
            loans.add(outstanding.getFirst());
        }
        OffsetDateTime now = OffsetDateTime.now();
        loans.forEach(loan -> markReturned(loan, now));
    }

    @Override
    @Transactional
    public void updateLoan(LoanEditDto dto) {
        validate(dto);
        currentEmployee();
        Loan loan = loanRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("This loan no longer exists."));
        // All loan mutations lock the copy first, including returns and lost/cancelled transitions.
        lockCopies(List.of(loan.getInventoryItem().getId()));
        entityManager.refresh(loan);
        if (dto.getExpectedUpdatedAt() != null && !dto.getExpectedUpdatedAt().isEqual(loan.getUpdatedAt())) {
            throw new OptimisticLockingFailureException("This loan was changed by another employee. Reopen its details and try again.");
        }
        boolean open = OPEN.contains(loan.getStatus());
        if (!open && !dto.getDueDate().isEqual(loan.getDueDate())) {
            throw new IllegalArgumentException("Only active loans can have their due date changed.");
        }
        if (dto.getDueDate().isBefore(loan.getLoanDate())) {
            throw new IllegalArgumentException("The due date cannot be before checkout.");
        }
        LoanStatus requested = dto.getStatus();
        boolean unchanged = requested == effectiveStatus(loan) || (open && OPEN.contains(requested));
        if (!unchanged && !open && !(loan.getStatus() == LoanStatus.LOST && requested == LoanStatus.RETURNED)) {
            throw new IllegalArgumentException("This loan is closed and cannot be reopened.");
        }
        loan.setDueDate(dto.getDueDate());
        loan.setNotes(dto.getNotes());
        if (requested == LoanStatus.RETURNED && !unchanged) {
            markReturned(loan, OffsetDateTime.now());
        } else if (!unchanged) {
            loan.setStatus(requested);
            if (requested == LoanStatus.LOST) {
                loan.getInventoryItem().setCondition(InventoryItem.Condition.LOST);
            }
        } else if (open) {
            loan.setStatus(effectiveStatus(loan));
        }
    }

    private void markReturned(Loan loan, OffsetDateTime now) {
        if (loan.getInventoryItem().getCondition() == InventoryItem.Condition.LOST) {
            // Recovered copies are marked for inspection.
            loan.getInventoryItem().setCondition(InventoryItem.Condition.UNUSABLE);
        }
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(now);
    }

    private List<InventoryItem> lockCopies(List<Long> ids) {
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("A copy may only appear once in a batch.");
        }
        // Stable ordering prevents deadlocks between overlapping batches.
        return ids.stream().sorted().map(id -> {
            InventoryItem copy = entityManager.find(InventoryItem.class, id, LockModeType.PESSIMISTIC_WRITE);
            if (copy == null) {
                throw new IllegalArgumentException("An inventory copy no longer exists: " + id);
            }
            entityManager.refresh(copy, LockModeType.PESSIMISTIC_WRITE);
            return copy;
        }).toList();
    }

    private List<Loan> outstanding(InventoryItem copy) {
        return loanRepository.findAllByInventoryItemIdAndStatusIn(copy.getId(), OUTSTANDING);
    }

    private Employee currentEmployee() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof EmployeePrincipal principal)) {
            throw new AccessDeniedException("Sign in with an employee account to manage loans.");
        }
        Employee employee = entityManager.find(Employee.class, principal.getEmployeeId());
        if (employee == null) {
            throw new AccessDeniedException("Your employee account no longer exists.");
        }
        return employee;
    }

    private void validate(Object dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Loan details are required.");
        }
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            var violation = violations.iterator().next();
            throw new IllegalArgumentException(violation.getPropertyPath() + ": " + violation.getMessage());
        }
    }

    private LoanSummaryDto summary(Loan loan) {
        LoanSummaryDto summary = loanMapper.toSummary(loan);
        return new LoanSummaryDto(summary.id(), summary.memberName(), summary.inventoryNumber(), summary.mediaTitle(),
                summary.dueDate(), effectiveStatus(loan));
    }

    private LoanStatus effectiveStatus(Loan loan) {
        return OPEN.contains(loan.getStatus())
                ? (loan.getDueDate().isBefore(OffsetDateTime.now()) ? LoanStatus.OVERDUE : LoanStatus.ACTIVE)
                : loan.getStatus();
    }
}
