package com.grosshaeuser.olmp.employee.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class EmployeeCreateDTO {
    private String email;
    private String lastName;
    private String firstName;

    private String username;
    private String password;
    private boolean enabled = true;

    private Set<String> roles = Set.of();
}
