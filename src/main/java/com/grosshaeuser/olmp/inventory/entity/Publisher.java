package com.grosshaeuser.olmp.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "publishers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
