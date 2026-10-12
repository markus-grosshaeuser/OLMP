package com.grosshaeuser.olmp.employee.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EmployeeEditDTO {
    private Long id;
    private String email;
    private String lastName;
    private String firstName;
    private String createdAt;
    private String updatedAt;

    private Long accountId;
    private String username;
    @Builder.Default
    private String newPassword = "";
    private boolean enabled;
    private int failedLoginAttempts;
    private String lockoutUntil;
    private boolean locked;
    private String accountCreatedAt;
    private String accountUpdatedAt;
    private String accountLastAccessed;

    @Builder.Default
    private Set<String> roles = Set.of();

    public String idAsString() {
        return id.toString();
    }

    public String accountIdAsString() {
        return accountId.toString();
    }

    public String failedLoginAttemptsAsString() {
        return Integer.toString(failedLoginAttempts);
    }
}
