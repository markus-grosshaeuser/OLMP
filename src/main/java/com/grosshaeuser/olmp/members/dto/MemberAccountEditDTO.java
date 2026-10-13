package com.grosshaeuser.olmp.members.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class MemberAccountEditDTO {
    private Long id;
    private Long memberId;
    private String email;
    @Builder.Default
    private String newPassword = "";
    private boolean verified;
    private boolean enabled;
    private int failedLoginAttempts;
    private String lockoutUntil;
    private boolean locked;
    private String accountCreatedAt;
    private String accountUpdatedAt;
    private String accountLastAccessedAt;

    public String idAsString() {
        return id.toString();
    }

    public String failedLoginAttemptsAsString() {
        return Integer.toString(failedLoginAttempts);
    }
}
