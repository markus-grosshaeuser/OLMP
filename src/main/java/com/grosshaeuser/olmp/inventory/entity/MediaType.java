package com.grosshaeuser.olmp.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "media_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "[^\\p{Ll}]*", message = "must be uppercase")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
