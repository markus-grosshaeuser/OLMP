package com.grosshaeuser.olmp.employee.service;

import com.grosshaeuser.olmp.employee.dto.EmployeeCreateDTO;
import com.grosshaeuser.olmp.employee.dto.EmployeeDTO;
import com.grosshaeuser.olmp.employee.dto.EmployeeEditDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EmployeeService {
    Page<EmployeeDTO> getEmployees(String searchTerm, Pageable pageable);
    Optional<EmployeeDTO> createEmployee(EmployeeCreateDTO dto);
    Optional<EmployeeEditDTO> getDetailedEmployeeById(Long id);
    Optional<EmployeeEditDTO> updateEmployee(EmployeeEditDTO dto);
    void deleteEmployee(Long id);
}
