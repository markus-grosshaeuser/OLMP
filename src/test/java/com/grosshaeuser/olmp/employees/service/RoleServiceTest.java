package com.grosshaeuser.olmp.employees.service;

import com.grosshaeuser.olmp.employees.entity.Role;
import com.grosshaeuser.olmp.employees.repository.RoleRepository;
import com.grosshaeuser.olmp.employees.service.impl.RoleServiceImpl;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static com.grosshaeuser.olmp.security.SecurityConstants.Roles.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private RoleRepository repository;
    @Mock
    private AuthenticationContext authenticationContext;

    private RoleService serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new RoleServiceImpl(repository, authenticationContext);
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void getAssignableRolesReturnsOnlyRolesWithAssignmentPermission(boolean canAssignAdmin, boolean canAssignStaff) {
        when(authenticationContext.hasAuthority(ASSIGN_ADMIN_ROLE)).thenReturn(canAssignAdmin);
        when(authenticationContext.hasAuthority(ASSIGN_STAFF_ROLE)).thenReturn(canAssignStaff);
        Set<Role> expected = new HashSet<>();
        if (canAssignAdmin) {
            Role admin = Role.builder().id(1L).name(ADMIN).build();
            when(repository.findByName(ADMIN)).thenReturn(Optional.of(admin));
            expected.add(admin);
        }
        if (canAssignStaff) {
            Role staff = Role.builder().id(2L).name(STAFF).build();
            when(repository.findByName(STAFF)).thenReturn(Optional.of(staff));
            expected.add(staff);
        }

        assertEquals(expected, serviceUnderTest.getAssignableRoles());

        verify(repository, times(canAssignAdmin ? 1 : 0)).findByName(ADMIN);
        verify(repository, times(canAssignStaff ? 1 : 0)).findByName(STAFF);
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void getAssignableRolesSkipsMissingRoles(boolean adminExists, boolean staffExists) {
        when(authenticationContext.hasAuthority(ASSIGN_ADMIN_ROLE)).thenReturn(true);
        when(authenticationContext.hasAuthority(ASSIGN_STAFF_ROLE)).thenReturn(true);
        Role admin = Role.builder().id(1L).name(ADMIN).build();
        Role staff = Role.builder().id(2L).name(STAFF).build();
        when(repository.findByName(ADMIN)).thenReturn(adminExists ? Optional.of(admin) : Optional.empty());
        when(repository.findByName(STAFF)).thenReturn(staffExists ? Optional.of(staff) : Optional.empty());
        Set<Role> expected = new HashSet<>();
        if (adminExists) {
            expected.add(admin);
        }
        if (staffExists) {
            expected.add(staff);
        }

        assertEquals(expected, serviceUnderTest.getAssignableRoles());
    }
}
