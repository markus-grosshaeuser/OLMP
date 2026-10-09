package com.grosshaeuser.olmp.employee.service;

import com.grosshaeuser.olmp.employee.repository.EmployeeRepository;
import com.grosshaeuser.olmp.security.repository.EmployeeAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
}
