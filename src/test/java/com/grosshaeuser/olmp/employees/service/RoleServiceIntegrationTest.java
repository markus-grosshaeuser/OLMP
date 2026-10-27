package com.grosshaeuser.olmp.employees.service;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.employees.entity.Role;
import com.grosshaeuser.olmp.employees.repository.RoleRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class RoleServiceIntegrationTest {
    @Autowired
    private RoleService serviceUnderTest;
    @Autowired
    private RoleRepository repository;
    @Autowired
    private EntityManager entityManager;
    @MockitoBean
    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        ensureRoleExists(ADMIN);
        ensureRoleExists(STAFF);
        ensureRoleExists(ROOT_ADMIN);
        ensureRoleExists(MEMBER);
        flushAndClear();
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void getAssignableRolesReturnsPersistedRolesAllowedByPrivileges(boolean canAssignAdmin, boolean canAssignStaff) {
        when(authenticationContext.hasAuthority(ASSIGN_ADMIN_ROLE)).thenReturn(canAssignAdmin);
        when(authenticationContext.hasAuthority(ASSIGN_STAFF_ROLE)).thenReturn(canAssignStaff);
        Set<Role> expected = new HashSet<>();
        if (canAssignAdmin) {
            expected.add(repository.findByName(ADMIN).orElseThrow());
        }
        if (canAssignStaff) {
            expected.add(repository.findByName(STAFF).orElseThrow());
        }
        entityManager.clear();

        Set<Role> result = serviceUnderTest.getAssignableRoles();

        assertEquals(expected, result);
        assertTrue(result.stream().allMatch(role -> role.getId() != null));
        for (Role role : result) {
            assertEquals(repository.findByName(role.getName()).orElseThrow().getId(), role.getId());
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void getAssignableRolesSkipsRolesMissingFromDatabase(boolean adminExists, boolean staffExists) {
        when(authenticationContext.hasAuthority(ASSIGN_ADMIN_ROLE)).thenReturn(true);
        when(authenticationContext.hasAuthority(ASSIGN_STAFF_ROLE)).thenReturn(true);
        Set<Role> expected = new HashSet<>();
        for (String name : Set.of(ADMIN, STAFF)) {
            Role role = repository.findByName(name).orElseThrow();
            if (name.equals(ADMIN) ? adminExists : staffExists) {
                expected.add(role);
            } else {
                // Renaming avoids touching associations belonging to seeded employees.
                role.setName("UNASSIGNABLE_" + name);
            }
        }
        flushAndClear();

        assertEquals(expected, serviceUnderTest.getAssignableRoles());
    }

    private void ensureRoleExists(String name) {
        if (repository.findByName(name).isEmpty()) {
            repository.save(Role.builder().name(name).build());
        }
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
