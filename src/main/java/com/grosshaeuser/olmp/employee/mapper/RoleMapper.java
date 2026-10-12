package com.grosshaeuser.olmp.employee.mapper;

import com.grosshaeuser.olmp.employee.entity.Role;

import java.util.HashSet;
import java.util.Set;

public class RoleMapper {
    public static HashSet<String> mapToStringHashSet(Set<Role> roles) {
        return new HashSet<>(roles.stream().map(Role::getName).toList());
    }
}
