package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.dto.media.MediaAuthorDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaAuthorWriteDto;
import com.grosshaeuser.olmp.inventory.entity.Author;
import com.grosshaeuser.olmp.inventory.entity.Media;
import com.grosshaeuser.olmp.inventory.entity.MediaAuthor;
import com.grosshaeuser.olmp.inventory.entity.MediaAuthorId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MediaAuthorMapper {

    private final AuthorMapper authorMapper;

    public MediaAuthorDto toDto(MediaAuthor source) {
        return source == null ? null : new MediaAuthorDto(
                authorMapper.toSummary(source.getAuthor()), source.getId().getRole(), source.getSortOrder());
    }

    public MediaAuthorWriteDto toWriteDto(MediaAuthor source) {
        if (source == null) {
            return null;
        }
        MediaAuthorWriteDto dto = new MediaAuthorWriteDto();
        dto.setAuthorId(source.getAuthor().getId());
        dto.setRole(source.getId().getRole());
        dto.setSortOrder(source.getSortOrder());
        return dto;
    }

    public MediaAuthor toEntity(MediaAuthorWriteDto source, Media media, Author author) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(media, "media");
        Objects.requireNonNull(author, "author");
        MappingChecks.sameId(source.getAuthorId(), author.getId(), "authorId");
        return MediaAuthor.builder()
                .id(new MediaAuthorId(media.getId(), author.getId(), source.getRole()))
                .media(media)
                .author(author)
                .sortOrder(source.getSortOrder())
                .build();
    }

    /**
     * Updates ordering only. Author and role are part of the primary key;
     * changing either requires removing the old assignment and creating a new one.
     */
    public void update(MediaAuthorWriteDto source, MediaAuthor target, Media media) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(media, "media");
        MappingChecks.sameMedia(media, target.getMedia());
        MappingChecks.sameId(source.getAuthorId(), target.getId().getAuthorId(), "authorId");
        if (!Objects.equals(source.getRole(), target.getId().getRole())) {
            throw new IllegalArgumentException("Cannot change the role of an existing author assignment");
        }
        target.setSortOrder(source.getSortOrder());
    }
}
