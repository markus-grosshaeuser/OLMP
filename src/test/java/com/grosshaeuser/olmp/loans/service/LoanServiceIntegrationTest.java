package com.grosshaeuser.olmp.loans.service;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.employees.mapper.EmployeeMapper;
import com.grosshaeuser.olmp.inventory.entity.InventoryItem;
import com.grosshaeuser.olmp.inventory.entity.Media;
import com.grosshaeuser.olmp.inventory.entity.MediaType;
import com.grosshaeuser.olmp.inventory.mappers.*;
import com.grosshaeuser.olmp.loans.LoanStatus;
import com.grosshaeuser.olmp.loans.dto.*;
import com.grosshaeuser.olmp.loans.entity.Loan;
import com.grosshaeuser.olmp.loans.mappers.LoanMapper;
import com.grosshaeuser.olmp.loans.repository.LoanRepository;
import com.grosshaeuser.olmp.loans.service.impl.LoanServiceImpl;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.mapper.MemberMapper;
import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.security.principal.EmployeePrincipal;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = "spring.flyway.locations=classpath:db/migration")
@Import({TestcontainersConfiguration.class, LoanServiceIntegrationTest.Config.class,
        LoanServiceImpl.class, LoanMapper.class, MemberMapper.class, EmployeeMapper.class,
        InventoryItemMapper.class, LocationMapper.class, MediaMapper.class, MediaTypeMapper.class,
        PublisherMapper.class, MediaAuthorMapper.class, AuthorMapper.class, MediaIdentifierMapper.class})
class LoanServiceIntegrationTest {
    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class Config {
        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    @Autowired
    private LoanService service;
    @Autowired
    private LoanRepository repository;
    @Autowired
    private EntityManager em;
    private Employee employee;
    private Member member;
    private Membership membership;
    private InventoryItem first;
    private InventoryItem second;

    @BeforeEach
    void setUp() {
        employee = Employee.builder().firstName("Library").lastName("Employee").build();
        em.persist(employee);
        signIn("ROLE_STAFF");
        member = Member.builder().firstName("Ada").lastName("Lovelace").build();
        em.persist(member);
        membership = Membership.builder().member(member).membershipNumber("CARD-123")
                .startDate(LocalDate.now().minusYears(1)).expirationDate(LocalDate.now().plusYears(1)).build();
        em.persist(membership);
        MediaType type = MediaType.builder().code("LOAN_TEST").name("Book").build();
        em.persist(type);
        Media media = Media.builder().mediaType(type).title("Loan test: 100%_complete").build();
        em.persist(media);
        first = InventoryItem.builder().media(media).inventoryNumber("COPY-1").barcode("SCAN-1").build();
        second = InventoryItem.builder().media(media).inventoryNumber("COPY-2").barcode("SCAN-2").build();
        em.persist(first);
        em.persist(second);
        em.flush();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkoutRecordsAuthenticatedEmployeeAndSupportsSearchPagingAndCopyLookup() {
        LoanBatchCreateDto dto = checkout(first, second);
        dto.setEmployeeId(-999L);
        List<LoanSummaryDto> created = service.createLoans(dto);
        em.flush();
        em.clear();
        assertThat(created).hasSize(2);
        LoanDto details = service.findLoan(created.getFirst().id()).orElseThrow();
        assertThat(details.employee().id()).isEqualTo(employee.getId());
        assertThat(details.inventoryItem().media().title()).contains("100%_complete");
        assertThat(service.findCopy(" SCAN-1 ").orElseThrow().id()).isEqualTo(first.getId());
        assertThat(service.findCopy("COPY-1")).isPresent();
        assertThat(service.findCopy("missing")).isEmpty();
        for (String query : List.of("ada love", "card-123", "COPY-", "SCAN-", "100%_complete")) {
            assertThat(service.countLoans(query, null)).as(query).isEqualTo(2);
            assertThat(service.findLoans(query, null, PageRequest.of(0, 1, Sort.by("inventoryItem.inventoryNumber"))))
                    .extracting(LoanSummaryDto::inventoryNumber).containsExactly("COPY-1");
        }
        assertThat(service.countLoans("100Xcomplete", null)).isZero();
        assertThat(service.findLoans("", null, PageRequest.of(1, 1, Sort.by("inventoryItem.inventoryNumber"))))
                .extracting(LoanSummaryDto::inventoryNumber).containsExactly("COPY-2");
    }

    @Test
    void derivesOverdueStatusAndAllowsRenewal() {
        Loan loan = persistLoan(first, LoanStatus.ACTIVE, OffsetDateTime.now().minusDays(1));
        em.flush();
        assertThat(service.findLoan(loan.getId()).orElseThrow().status()).isEqualTo(LoanStatus.OVERDUE);
        assertThat(service.countLoans("", LoanStatus.OVERDUE)).isEqualTo(1);
        assertThat(service.countLoans("", LoanStatus.ACTIVE)).isZero();
        LoanEditDto edit = edit(loan, LoanStatus.OVERDUE);
        edit.setDueDate(OffsetDateTime.now().plusDays(14));
        service.updateLoan(edit);
        em.flush();
        assertThat(service.findLoan(loan.getId()).orElseThrow().status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(service.countLoans("", LoanStatus.OVERDUE)).isZero();
        assertThat(service.countLoans("", LoanStatus.ACTIVE)).isEqualTo(1);
    }

    @Test
    void overdueCheckUsesExactMembershipAndFindsLoansAcrossMembersCards() {
        persistLoan(first, LoanStatus.ACTIVE, OffsetDateTime.now().minusDays(1));
        Membership anotherCard = Membership.builder().member(member).membershipNumber("CARD-456")
                .startDate(LocalDate.now().minusDays(1)).build();
        em.persist(anotherCard);
        Member anotherMember = Member.builder().firstName("Grace").lastName("Hopper").build();
        em.persist(anotherMember);
        em.persist(Membership.builder().member(anotherMember).membershipNumber("CARD-1234")
                .startDate(LocalDate.now().minusDays(1)).build());
        em.flush();

        assertThat(service.hasOverdueLoans(" CARD-123 ")).isTrue();
        assertThat(service.hasOverdueLoans("CARD-456")).isTrue();
        assertThat(service.hasOverdueLoans("CARD-12")).isFalse();
        assertThat(service.hasOverdueLoans("CARD-1234")).isFalse();
        assertThat(service.hasOverdueLoans("missing")).isFalse();
        assertThat(service.hasOverdueLoans("")).isFalse();
        assertThat(service.hasOverdueLoans(null)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class, names = {"RETURNED", "CANCELLED", "LOST"})
    void overdueCheckExcludesClosedLoansAndLoansWhoseDueDateWasExtended(LoanStatus closedStatus) {
        persistLoan(first, closedStatus, OffsetDateTime.now().minusDays(1));
        persistLoan(second, LoanStatus.OVERDUE, OffsetDateTime.now().plusDays(1));
        em.flush();
        assertThat(service.hasOverdueLoans(membership.getMembershipNumber())).isFalse();
    }

    @Test
    void returnKeepsHistoryAndMakesCopyAvailableAgain() {
        Long id = service.createLoans(checkout(first)).getFirst().id();
        service.returnLoans(LoanBatchReturnDto.builder().inventoryItemIds(List.of(first.getId())).build());
        em.flush();
        LoanDto returned = service.findLoan(id).orElseThrow();
        assertThat(returned.status()).isEqualTo(LoanStatus.RETURNED);
        assertThat(returned.returnDate()).isAfterOrEqualTo(returned.loanDate());
        assertThat(service.createLoans(checkout(first))).hasSize(1);
        assertThat(service.countLoans("", LoanStatus.RETURNED)).isEqualTo(1);
    }

    @Test
    void rejectsBatchContainingAlreadyBorrowedCopyBeforeCreatingAnyLoans() {
        persistLoan(second, LoanStatus.OVERDUE, OffsetDateTime.now().minusDays(1));
        em.flush();
        assertThatThrownBy(() -> service.createLoans(checkout(first, second)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("outstanding loan");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void rejectsExpiredMembershipEvenWhenItsStoredStatusIsActive() {
        membership.setExpirationDate(LocalDate.now().minusDays(1));
        em.flush();
        assertThatThrownBy(() -> service.createLoans(checkout(first)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not currently active");
        assertThat(repository.count()).isZero();
    }

    @Test
    void rejectsSuspendedMembership() {
        membership.setStatus(MembershipStatus.SUSPENDED);
        em.flush();
        assertThatThrownBy(() -> service.createLoans(checkout(first)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not currently active");
    }

    @Test
    void rejectsWithdrawnAndUnusableCopies() {
        first.setWithdrawnAt(LocalDate.now());
        em.flush();
        assertThatThrownBy(() -> service.createLoans(checkout(first)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("unavailable");
        second.setCondition(InventoryItem.Condition.UNUSABLE);
        em.flush();
        assertThatThrownBy(() -> service.createLoans(checkout(second)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("unavailable");
    }

    @Test
    void rejectsDuplicateCopiesAndNullIds() {
        assertThatThrownBy(() -> service.createLoans(checkout(first, first)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("once");
        LoanBatchCreateDto dto = checkout(first);
        dto.setInventoryItemIds(java.util.Arrays.asList((Long) null));
        assertThatThrownBy(() -> service.createLoans(dto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidReturnBatchDoesNotPartiallyReturnCopies() {
        Loan loan = persistLoan(first, LoanStatus.ACTIVE, OffsetDateTime.now().plusDays(5));
        em.flush();
        assertThatThrownBy(() -> service.returnLoans(LoanBatchReturnDto.builder()
                .inventoryItemIds(List.of(first.getId(), second.getId())).build()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("outstanding loan");
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.getReturnDate()).isNull();
    }

    @Test
    void lostCopyCanBeReturnedButRequiresInspectionBeforeNextCheckout() {
        Loan loan = persistLoan(first, LoanStatus.ACTIVE, OffsetDateTime.now().plusDays(5));
        em.flush();
        service.updateLoan(edit(loan, LoanStatus.LOST));
        em.flush();
        assertThat(first.getCondition()).isEqualTo(InventoryItem.Condition.LOST);
        service.returnLoans(LoanBatchReturnDto.builder().inventoryItemIds(List.of(first.getId())).build());
        em.flush();
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(first.getCondition()).isEqualTo(InventoryItem.Condition.UNUSABLE);
        assertThatThrownBy(() -> service.createLoans(checkout(first)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("unavailable");
    }

    @Test
    void cancelledLoanRemainsInHistoryAndCannotBeReopened() {
        Loan loan = persistLoan(first, LoanStatus.ACTIVE, OffsetDateTime.now().plusDays(5));
        em.flush();
        service.updateLoan(edit(loan, LoanStatus.CANCELLED));
        em.flush();
        assertThat(service.countLoans("", LoanStatus.CANCELLED)).isEqualTo(1);
        assertThat(service.createLoans(checkout(first))).hasSize(1);
        assertThatThrownBy(() -> service.updateLoan(edit(loan, LoanStatus.ACTIVE)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("cannot be reopened");
    }

    @Test
    void rejectsStaleEdits() {
        Loan loan = persistLoan(first, LoanStatus.ACTIVE, OffsetDateTime.now().plusDays(5));
        em.flush();
        LoanEditDto edit = edit(loan, LoanStatus.ACTIVE);
        edit.setExpectedUpdatedAt(loan.getUpdatedAt().minusMinutes(1));
        assertThatThrownBy(() -> service.updateLoan(edit)).isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void optionalEmployeeIsSupportedWhenLoadingHistoricalLoans() {
        Loan loan = persistLoan(first, LoanStatus.RETURNED, OffsetDateTime.now().plusDays(5));
        em.flush();
        assertThat(service.findLoan(loan.getId()).orElseThrow().employee()).isNull();
    }

    @Test
    void deniesNonEmployeeRoles() {
        signIn("ROLE_MEMBER");
        assertThatThrownBy(() -> service.countLoans("", null)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.createLoans(checkout(first))).isInstanceOf(AccessDeniedException.class);
    }

    private void signIn(String role) {
        var authorities = List.of(new SimpleGrantedAuthority(role));
        EmployeeAccount account = EmployeeAccount.builder().id(1L).employee(employee).username("librarian")
                .passwordHash("unused").build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new EmployeePrincipal(account, authorities), "unused", authorities));
    }

    private LoanBatchCreateDto checkout(InventoryItem... copies) {
        return LoanBatchCreateDto.builder().membershipNumber(membership.getMembershipNumber())
                .inventoryItemIds(java.util.Arrays.stream(copies).map(InventoryItem::getId).toList())
                .dueDate(OffsetDateTime.now().plusDays(28)).build();
    }

    private Loan persistLoan(InventoryItem copy, LoanStatus status, OffsetDateTime due) {
        Loan loan = Loan.builder().member(member).inventoryItem(copy).loanDate(OffsetDateTime.now().minusDays(7))
                .dueDate(due).status(status).build();
        em.persist(loan);
        return loan;
    }

    private LoanEditDto edit(Loan loan, LoanStatus status) {
        // PostgreSQL stores microseconds; use the persisted timestamp precision in submitted DTOs.
        em.refresh(loan);
        return LoanEditDto.builder().id(loan.getId()).status(status).dueDate(loan.getDueDate()).notes("Updated notes").build();
    }
}
