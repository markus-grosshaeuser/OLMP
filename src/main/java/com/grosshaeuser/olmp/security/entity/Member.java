package com.grosshaeuser.olmp.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private MemberContactInformation contactInformation;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Setter(AccessLevel.PRIVATE)
    private Set<Membership> memberships = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public void setContactInformation(MemberContactInformation contactInformation) {
        this.contactInformation = contactInformation;

        if (contactInformation != null) {
            contactInformation.setMember(this);
        }
    }

    public void addMembership(Membership membership) {
        memberships.add(membership);
        membership.setMember(this);
    }

    public void removeMembership(Membership membership) {
        memberships.remove(membership);
        membership.setMember(null);
    }

    public boolean hasActiveMembership() {
        return memberships.stream()
                .anyMatch(membership -> membership.getStatus() == Membership.MembershipStatus.ACTIVE);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}