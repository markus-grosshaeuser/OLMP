package com.grosshaeuser.olmp.members.dto;

import lombok.*;

import java.time.LocalDateTime;

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
    private LocalDateTime lockoutUntil;
    private boolean locked;
    private LocalDateTime accountCreatedAt;
    private LocalDateTime accountUpdatedAt;
    private LocalDateTime accountLastAccessedAt;

}
