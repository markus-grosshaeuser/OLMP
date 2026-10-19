package com.grosshaeuser.olmp.employees.service;

import com.grosshaeuser.olmp.employees.dto.EmployeeRegistrationDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employees.dto.EmployeeEditDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EmployeeService {
    Page<EmployeeDTO> getEmployees(String searchTerm, Pageable pageable);
    Optional<EmployeeDTO> createEmployee(EmployeeRegistrationDTO dto);
    Optional<EmployeeEditDTO> getDetailedEmployeeById(Long id);
    Optional<EmployeeEditDTO> updateEmployee(EmployeeEditDTO dto);
    void deleteEmployee(Long id);

    Optional<EmployeeEditDTO> getCurrentEmployee();
    Optional<EmployeeEditDTO> updateCurrentEmployee(EmployeeEditDTO dto);

    long countEmployees(String query);
}
