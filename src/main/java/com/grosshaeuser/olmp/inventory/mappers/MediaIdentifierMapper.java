package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.dto.media.MediaIdentifierDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaIdentifierWriteDto;
import com.grosshaeuser.olmp.inventory.entity.Media;
import com.grosshaeuser.olmp.inventory.entity.MediaIdentifier;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MediaIdentifierMapper {

    public MediaIdentifierDto toDto(MediaIdentifier source) {
        return source == null ? null : new MediaIdentifierDto(
                source.getId(), source.getIdentifierType(), source.getIdentifierValue());
    }

    public MediaIdentifierWriteDto toWriteDto(MediaIdentifier source) {
        if (source == null) {
            return null;
        }
        MediaIdentifierWriteDto dto = new MediaIdentifierWriteDto();
        dto.setId(source.getId());
        dto.setIdentifierType(source.getIdentifierType());
        dto.setIdentifierValue(source.getIdentifierValue());
        return dto;
    }

    public MediaIdentifier toEntity(MediaIdentifierWriteDto source, Media media) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(media, "media");
        if (source.getId() != null) {
            throw new IllegalArgumentException("A new identifier must not have an ID");
        }
        MediaIdentifier target = new MediaIdentifier();
        target.setMedia(media);
        copyWritableFields(source, target);
        return target;
    }

    public void update(MediaIdentifierWriteDto source, MediaIdentifier target, Media media) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(media, "media");
        MappingChecks.sameMedia(media, target.getMedia());
        MappingChecks.sameId(source.getId(), target.getId(), "id");
        copyWritableFields(source, target);
    }

    private void copyWritableFields(MediaIdentifierWriteDto source, MediaIdentifier target) {
        target.setIdentifierType(source.getIdentifierType());
        target.setIdentifierValue(source.getIdentifierValue());
    }

}
