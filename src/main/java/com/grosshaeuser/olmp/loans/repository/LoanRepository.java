package com.grosshaeuser.olmp.loans.repository;

import com.grosshaeuser.olmp.loans.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
