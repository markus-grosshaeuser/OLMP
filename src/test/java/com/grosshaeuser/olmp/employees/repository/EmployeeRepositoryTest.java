package com.grosshaeuser.olmp.employees.repository;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class EmployeeRepositoryTest {

    private static final String STAFF = "QUERY_TEST_STAFF";
    private static final String ADMIN = "QUERY_TEST_ADMIN";
    private static final Set<String> BOTH_ROLES = Set.of(STAFF, ADMIN);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employeeRepository.flush();

        Role staff = role(STAFF);
        Role admin = role(ADMIN);
        employee("Doe", "Jane", "jane@example.org", staff, admin);
        employee("Smith", "Robert", "robert@example.org", staff);
        employee("Brown", "Alice", "support@example.org", admin);
        employee("Doe", "Jane", "excluded@example.org", role("QUERY_TEST_OTHER"));
        employee("Doe", "Jane", null);
        employee("White", "Charlie", null, staff);
        entityManager.flush();
        entityManager.clear();
    }

    @ParameterizedTest
    @ValueSource(strings = {"aNe", "dO", "ANE@EXAMPLE"})
    void searchWithRolesInMatchesFirstNameLastNameOrEmailIgnoringCase(String searchTerm) {
        Page<Employee> result = employeeRepository.searchWithRolesIn(searchTerm, BOTH_ROLES, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Employee::getEmail).containsExactly("jane@example.org");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void searchWithRolesInRequiresBothMatchingRoleAndMatchingSearchTerm() {
        Page<Employee> result = employeeRepository.searchWithRolesIn("support", Set.of(STAFF), PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void searchWithRolesInWithEmptySearchTermReturnsDistinctEmployeesWithAnyRequestedRole() {
        Page<Employee> result = employeeRepository.searchWithRolesIn("", BOTH_ROLES, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("Jane", "Robert", "Alice", "Charlie");
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    void searchWithRolesInSupportsSortedPaginationWithoutDuplicateEmployees() {
        Sort sort = Sort.by("lastName").ascending();

        Page<Employee> firstPage = employeeRepository.searchWithRolesIn("", BOTH_ROLES, PageRequest.of(0, 2, sort));
        Page<Employee> secondPage = employeeRepository.searchWithRolesIn("", BOTH_ROLES, PageRequest.of(1, 2, sort));

        assertThat(firstPage.getContent()).extracting(Employee::getLastName).containsExactly("Brown", "Doe");
        assertThat(secondPage.getContent()).extracting(Employee::getLastName).containsExactly("Smith", "White");
        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(secondPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(secondPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void searchWithRolesInMatchesNamesWhenEmailIsNull() {
        Page<Employee> result = employeeRepository.searchWithRolesIn("hArL", BOTH_ROLES, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Employee::getFirstName).containsExactly("Charlie");
    }

    @Test
    void searchWithRolesInReturnsEmptyPageWhenNothingMatches() {
        Page<Employee> result = employeeRepository.searchWithRolesIn("missing", BOTH_ROLES, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void customQueriesReturnNoMatchesForEmptyRoleSet() {
        Page<Employee> result = employeeRepository.searchWithRolesIn("", Set.of(), PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(employeeRepository.countSearch("", Set.of())).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"aNe", "dO", "ANE@EXAMPLE"})
    void countSearchMatchesFirstNameLastNameOrEmailIgnoringCase(String searchTerm) {
        assertThat(employeeRepository.countSearch(searchTerm, Set.of(STAFF))).isEqualTo(1);
    }

    @Test
    void countSearchRequiresBothMatchingRoleAndMatchingSearchTerm() {
        assertThat(employeeRepository.countSearch("support", Set.of(STAFF))).isZero();
    }

    @Test
    void countSearchCountsEachEmployeeOnceWhenMultipleRolesMatch() {
        assertThat(employeeRepository.countSearch("jane", BOTH_ROLES)).isEqualTo(2);
        assertThat(employeeRepository.countSearch("", BOTH_ROLES)).isEqualTo(5);
    }

    @Test
    void countSearchMatchesNamesWhenEmailIsNull() {
        assertThat(employeeRepository.countSearch("hArL", BOTH_ROLES)).isEqualTo(1);
    }

    @Test
    void countSearchReturnsZeroWhenNothingMatches() {
        assertThat(employeeRepository.countSearch("missing", BOTH_ROLES)).isZero();
    }

    private Role role(String name) {
        Role role = Role.builder().name(name).build();
        entityManager.persist(role);
        return role;
    }

    private void employee(String lastName, String firstName, String email, Role... roles) {
        entityManager.persist(Employee.builder()
                .lastName(lastName)
                .firstName(firstName)
                .email(email)
                .roles(Set.of(roles))
                .build());
    }
}
