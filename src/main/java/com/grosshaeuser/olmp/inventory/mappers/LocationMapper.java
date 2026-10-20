package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.dto.location.LocationCreateDto;
import com.grosshaeuser.olmp.inventory.dto.location.LocationDetailDto;
import com.grosshaeuser.olmp.inventory.dto.location.LocationEditDto;
import com.grosshaeuser.olmp.inventory.dto.location.LocationSummaryDto;
import com.grosshaeuser.olmp.inventory.entity.Location;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class LocationMapper {

    public LocationSummaryDto toSummary(Location source) {
        return source == null ? null : new LocationSummaryDto(
                source.getId(), source.getCode(), source.getName());
    }

    public LocationDetailDto toDetail(Location source) {
        return source == null ? null : new LocationDetailDto(
                source.getId(), source.getCode(), source.getName(), source.getDescription(),
                toSummary(source.getParent()), source.getCreatedAt());
    }

    public LocationEditDto toEditDto(Location source) {
        if (source == null) {
            return null;
        }
        LocationEditDto dto = new LocationEditDto();
        dto.setId(source.getId());
        dto.setCode(source.getCode());
        dto.setName(source.getName());
        dto.setDescription(source.getDescription());
        dto.setParentId(source.getParent() == null ? null : source.getParent().getId());
        return dto;
    }

    public Location toEntity(LocationCreateDto source, Location parent) {
        Objects.requireNonNull(source, "source");
        checkParent(source, parent, null);
        Location target = new Location();
        copyWritableFields(source, target, parent);
        return target;
    }

    public void update(LocationEditDto source, Location target, Location parent) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        MappingChecks.sameId(source.getId(), target.getId(), "id");
        checkParent(source, parent, target);
        copyWritableFields(source, target, parent);
    }

    private void checkParent(LocationCreateDto source, Location parent, Location target) {
        MappingChecks.optionalReference(source.getParentId(), parent, Location::getId, "parentId");
        if (parent != null && target != null
                && (parent == target || Objects.equals(parent.getId(), target.getId()))) {
            throw new IllegalArgumentException("A location cannot be its own parent");
        }
    }

    private void copyWritableFields(LocationCreateDto source, Location target, Location parent) {
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setParent(parent);
    }
}
