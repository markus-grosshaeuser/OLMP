package com.grosshaeuser.olmp.employees.dto;

import lombok.*;

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
}
