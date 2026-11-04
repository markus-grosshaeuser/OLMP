package com.grosshaeuser.olmp.loans.service;

import com.grosshaeuser.olmp.loans.LoanStatus;
import com.grosshaeuser.olmp.loans.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoanService {
    List<LoanSummaryDto> findLoans(String query, LoanStatus status, Pageable pageable);

    long countLoans(String query, LoanStatus status);

    boolean hasOverdueLoans(String membershipNumber);

    Optional<LoanDto> findLoan(long id);

    Optional<LoanCopyDto> findCopy(String inventoryNumberOrBarcode);

    List<LoanSummaryDto> createLoans(LoanBatchCreateDto dto);

    void returnLoans(LoanBatchReturnDto dto);

    void updateLoan(LoanEditDto dto);
}
