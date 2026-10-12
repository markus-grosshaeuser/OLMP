package com.grosshaeuser.olmp.employee.service.impl;

import com.grosshaeuser.olmp.employee.entity.Role;
import com.grosshaeuser.olmp.employee.repository.RoleRepository;
import com.grosshaeuser.olmp.employee.service.RoleService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private final AuthenticationContext authenticationContext;

    @Override
    public Set<Role> getAssignableRoles() {
        Set<Role> roles = new HashSet<>();

        if (authenticationContext.hasAuthority(SecurityConstants.Privileges.ASSIGN_ADMIN_ROLE)) {
            roleRepository.findByName(SecurityConstants.Roles.ADMIN).ifPresent(roles::add);
        }

        if (authenticationContext.hasAuthority(SecurityConstants.Privileges.ASSIGN_STAFF_ROLE)) {
            roleRepository.findByName(SecurityConstants.Roles.STAFF).ifPresent(roles::add);
        }

        return roles;
    }
}
