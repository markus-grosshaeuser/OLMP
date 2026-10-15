package com.grosshaeuser.olmp.employees.dto;

import com.grosshaeuser.olmp.employees.entity.Role;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EmployeeAccountEditDTO {
    private Long id;
    private String username;
    @Builder.Default
    private String newPassword = "";
    private boolean enabled;
    private int failedLoginAttempts;
    private String lockoutUntil;
    private boolean locked;

    @Builder.Default
    private Set<Role> roles = Set.of();
}
