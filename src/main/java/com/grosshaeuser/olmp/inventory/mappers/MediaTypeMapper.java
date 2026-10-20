package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.dto.media.MediaTypeCreateDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaTypeDetailDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaTypeEditDto;
import com.grosshaeuser.olmp.inventory.dto.media.MediaTypeSummaryDto;
import com.grosshaeuser.olmp.inventory.entity.MediaType;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MediaTypeMapper {

    public MediaTypeSummaryDto toSummary(MediaType source) {
        return source == null ? null : new MediaTypeSummaryDto(
                source.getId(), source.getCode(), source.getName());
    }

    public MediaTypeDetailDto toDetail(MediaType source) {
        return source == null ? null : new MediaTypeDetailDto(
                source.getId(), source.getCode(), source.getName(), source.getDescription(), source.getCreatedAt());
    }

    public MediaTypeEditDto toEditDto(MediaType source) {
        if (source == null) {
            return null;
        }
        MediaTypeEditDto dto = new MediaTypeEditDto();
        dto.setId(source.getId());
        dto.setCode(source.getCode());
        dto.setName(source.getName());
        dto.setDescription(source.getDescription());
        return dto;
    }

    public MediaType toEntity(MediaTypeCreateDto source) {
        Objects.requireNonNull(source, "source");
        MediaType target = new MediaType();
        copyWritableFields(source, target);
        return target;
    }

    public void update(MediaTypeEditDto source, MediaType target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        MappingChecks.sameId(source.getId(), target.getId(), "id");
        copyWritableFields(source, target);
    }

    private void copyWritableFields(MediaTypeCreateDto source, MediaType target) {
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
    }
}
