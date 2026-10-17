package com.grosshaeuser.olmp.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import jakarta.validation.Valid;

@Entity
@Table(name = "media_authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAuthor {

    @Valid
    @NotNull
    @EmbeddedId
    @Builder.Default
    private MediaAuthorId id = new MediaAuthorId();

    @NotNull
    @MapsId("mediaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @NotNull
    @MapsId("authorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @NotNull
    @PositiveOrZero
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Short sortOrder = 0;
}
