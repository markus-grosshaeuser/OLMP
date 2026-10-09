package com.grosshaeuser.olmp.members.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_contact_information")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberContactInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "phone_number", length = 100)
    private String phoneNumber;

    @Column(name = "street", length = 100)
    private String street;

    @Column(name = "house_number", length = 10)
    private String houseNumber;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;
}