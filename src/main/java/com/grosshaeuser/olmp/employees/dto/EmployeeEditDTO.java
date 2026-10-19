package com.grosshaeuser.olmp.employees.dto;

import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long accountId;
    private String username;
    @Builder.Default
    private String newPassword = "";
    private boolean enabled;
    private int failedLoginAttempts;
    private LocalDateTime lockoutUntil;
    private boolean locked;
    private LocalDateTime accountCreatedAt;
    private LocalDateTime accountUpdatedAt;
    private LocalDateTime accountLastAccessed;

    @Builder.Default
    private Set<String> roles = Set.of();
}
