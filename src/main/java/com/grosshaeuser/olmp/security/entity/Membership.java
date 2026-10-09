package com.grosshaeuser.olmp.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotBlank
    @Size(max = 50)
    @Column(name = "membership_number", nullable = false, unique = true, length = 50)
    private String membershipNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private MembershipType type = MembershipType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "renewal_date")
    private LocalDate renewalDate;

    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    @PrePersist
    protected void onCreate() {
        if (type == null) {
            type = MembershipType.STANDARD;
        }

        if (status == null) {
            status = MembershipStatus.ACTIVE;
        }

        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Membership that)) {
            return false;
        }
        return Objects.equals(membershipNumber, that.membershipNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(membershipNumber);
    }

    public enum MembershipType {
        STANDARD,
        STUDENT,
        SENIOR,
        PREMIUM
    }

    public enum MembershipStatus {
        ACTIVE,
        EXPIRED,
        SUSPENDED,
        CANCELLED
    }
}