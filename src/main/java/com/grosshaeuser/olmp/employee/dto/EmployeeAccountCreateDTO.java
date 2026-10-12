package com.grosshaeuser.olmp.employee.dto;

import com.grosshaeuser.olmp.employee.entity.Employee;
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
