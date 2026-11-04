package com.grosshaeuser.olmp.ui.internal_ui.shared.loans;

import com.grosshaeuser.olmp.loans.service.LoanService;
import com.grosshaeuser.olmp.ui.internal_ui.shared.UiContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class LoanContext {
    private final LoanService loanService;
    private final UiContext uiContext;
}
