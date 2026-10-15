package com.grosshaeuser.olmp.employees.dto;

import com.grosshaeuser.olmp.employees.entity.Employee;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAccountCreateDTO {
    private Employee employee;
    private String username;
    private String password;
    private boolean enabled;
}
