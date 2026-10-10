package com.grosshaeuser.olmp.employee.service;

import com.grosshaeuser.olmp.employee.dto.EmployeeDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface EmployeeService {
    Slice<EmployeeDTO> getEmployees(String searchTerm, Pageable pageable);
}
