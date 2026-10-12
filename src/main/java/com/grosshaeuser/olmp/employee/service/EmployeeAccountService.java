package com.grosshaeuser.olmp.employee.service;

import com.grosshaeuser.olmp.employee.dto.EmployeeAccountCreateDTO;
import com.grosshaeuser.olmp.employee.dto.EmployeeAccountEditDTO;
import com.grosshaeuser.olmp.employee.entity.EmployeeAccount;

import java.util.Optional;

public interface EmployeeAccountService {
    Optional<EmployeeAccount> findById(Long employeeId);
    Optional<EmployeeAccount> createEmployeeAccount(EmployeeAccountCreateDTO employeeAccountCreateDTO);
    Optional<EmployeeAccount> updateEmployeeAccount(EmployeeAccountEditDTO dto);
}
