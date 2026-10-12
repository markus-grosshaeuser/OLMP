package com.grosshaeuser.olmp.employee.service;

import com.grosshaeuser.olmp.employee.dto.EmployeeCreateDTO;
import com.grosshaeuser.olmp.employee.dto.EmployeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EmployeeService {
    Page<EmployeeDTO> getEmployees(String searchTerm, Pageable pageable);
    Optional<EmployeeDTO> createEmployee(EmployeeCreateDTO dto);
}
