package com.grosshaeuser.olmp.loans.repository;

import com.grosshaeuser.olmp.loans.entity.Loan;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.grosshaeuser.olmp.loans.LoanStatus;

import java.util.Collection;
import java.util.List;
import java.time.OffsetDateTime;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"member", "inventoryItem", "inventoryItem.media"})
    Page<Loan> findAll(@NonNull Specification<Loan> specification, @NonNull Pageable pageable);

    List<Loan> findAllByInventoryItemIdAndStatusIn(Long inventoryItemId, Collection<LoanStatus> statuses);

    boolean existsByMemberMembershipsMembershipNumberAndStatusInAndDueDateBefore(
            String membershipNumber, Collection<LoanStatus> statuses, OffsetDateTime dueDate);
}
