package com.grosshaeuser.olmp.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_type_id", nullable = false)
    private MediaType mediaType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @NotBlank
    @Size(max = 500)
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Size(max = 500)
    @Column(name = "subtitle", length = 500)
    private String subtitle;

    @Size(max = 100)
    @Column(name = "edition", length = 100)
    private String edition;

    @Min(1)
    @Max(9999)
    @Column(name = "publication_year")
    private Short publicationYear;

    @Size(max = 10)
    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Size(max = 100)
    @Column(name = "cover_image", length = 100)
    private String coverImageFileName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
