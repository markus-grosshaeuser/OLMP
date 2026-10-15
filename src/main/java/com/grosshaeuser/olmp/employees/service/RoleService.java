package com.grosshaeuser.olmp.employees.service;

import com.grosshaeuser.olmp.employees.entity.Role;

import java.util.Set;

public interface RoleService {
    Set<Role> getAssignableRoles();
}
