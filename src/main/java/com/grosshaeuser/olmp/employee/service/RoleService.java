package com.grosshaeuser.olmp.employee.service;

import com.grosshaeuser.olmp.employee.entity.Role;

import java.util.Set;

public interface RoleService {
    Set<Role> getAssignableRoles();
}
