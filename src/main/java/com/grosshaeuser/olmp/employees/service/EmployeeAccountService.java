package com.grosshaeuser.olmp.employees.service;

import com.grosshaeuser.olmp.employees.dto.EmployeeAccountCreateDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeAccountEditDTO;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;

import java.util.Optional;

public interface EmployeeAccountService {
    Optional<EmployeeAccount> findById(Long employeeId);
    Optional<EmployeeAccount> findByEmployeeId(Long employeeId);
    Optional<EmployeeAccount> findByUsername(String username);
    Optional<EmployeeAccount> createEmployeeAccount(EmployeeAccountCreateDTO employeeAccountCreateDTO);
    Optional<EmployeeAccount> updateEmployeeAccount(EmployeeAccountEditDTO dto);
}
