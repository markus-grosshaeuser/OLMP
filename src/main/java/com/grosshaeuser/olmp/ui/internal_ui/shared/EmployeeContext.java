package com.grosshaeuser.olmp.ui.internal_ui.shared;

import com.grosshaeuser.olmp.employees.service.EmployeeService;
import com.grosshaeuser.olmp.employees.service.RoleService;
import com.grosshaeuser.olmp.security.properties.PasswordProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class EmployeeContext {
    private final EmployeeService employeeService;
    private final RoleService roleService;
    private final PasswordProperties passwordProperties;
}
