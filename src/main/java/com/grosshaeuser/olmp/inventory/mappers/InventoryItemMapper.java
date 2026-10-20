package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCondition;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemCreateDto;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemDetailDto;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemEditDto;
import com.grosshaeuser.olmp.inventory.dto.inventory.InventoryItemSummaryDto;
import com.grosshaeuser.olmp.inventory.entity.InventoryItem;
import com.grosshaeuser.olmp.inventory.entity.Location;
import com.grosshaeuser.olmp.inventory.entity.Media;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class InventoryItemMapper {

    private final MediaMapper mediaMapper;
    private final LocationMapper locationMapper;

    public InventoryItemSummaryDto toSummary(InventoryItem source) {
        return source == null ? null : new InventoryItemSummaryDto(
                source.getId(), source.getMedia().getId(), source.getMedia().getTitle(),
                source.getInventoryNumber(), source.getBarcode(), toDtoCondition(source.getCondition()),
                locationMapper.toSummary(source.getLocation()), source.getWithdrawnAt());
    }

    public InventoryItemDetailDto toDetail(InventoryItem source) {
        return source == null ? null : new InventoryItemDetailDto(
                source.getId(), mediaMapper.toReference(source.getMedia()), source.getInventoryNumber(),
                source.getBarcode(), toDtoCondition(source.getCondition()),
                locationMapper.toSummary(source.getLocation()), source.getAcquiredAt(),
                source.getAcquisitionCost(), source.getWithdrawnAt(), source.getNotes(),
                source.getCreatedAt(), source.getUpdatedAt());
    }

    public InventoryItemEditDto toEditDto(InventoryItem source) {
        if (source == null) {
            return null;
        }
        InventoryItemEditDto dto = new InventoryItemEditDto();
        dto.setId(source.getId());
        dto.setMediaId(source.getMedia().getId());
        dto.setInventoryNumber(source.getInventoryNumber());
        dto.setBarcode(source.getBarcode());
        dto.setCondition(toDtoCondition(source.getCondition()));
        dto.setLocationId(source.getLocation() == null ? null : source.getLocation().getId());
        dto.setAcquiredAt(source.getAcquiredAt());
        dto.setAcquisitionCost(source.getAcquisitionCost());
        dto.setWithdrawnAt(source.getWithdrawnAt());
        dto.setNotes(source.getNotes());
        return dto;
    }

    public InventoryItem toEntity(InventoryItemCreateDto source, Media media, Location location) {
        Objects.requireNonNull(source, "source");
        checkReferences(source, media, location);
        InventoryItem target = new InventoryItem();
        copyWritableFields(source, target, media, location);
        return target;
    }

    public void update(InventoryItemEditDto source, InventoryItem target, Media media, Location location) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        MappingChecks.sameId(source.getId(), target.getId(), "id");
        checkReferences(source, media, location);
        copyWritableFields(source, target, media, location);
    }

    private void checkReferences(InventoryItemCreateDto source, Media media, Location location) {
        Objects.requireNonNull(media, "media");
        MappingChecks.sameId(source.getMediaId(), media.getId(), "mediaId");
        MappingChecks.optionalReference(source.getLocationId(), location, Location::getId, "locationId");
    }

    private void copyWritableFields(InventoryItemCreateDto source, InventoryItem target,
                                    Media media, Location location) {
        target.setMedia(media);
        target.setInventoryNumber(source.getInventoryNumber());
        target.setBarcode(source.getBarcode());
        target.setCondition(source.getCondition() == null ? null
                : InventoryItem.Condition.valueOf(source.getCondition().name()));
        target.setLocation(location);
        target.setAcquiredAt(source.getAcquiredAt());
        target.setAcquisitionCost(source.getAcquisitionCost());
        target.setWithdrawnAt(source.getWithdrawnAt());
        target.setNotes(source.getNotes());
    }

    private InventoryItemCondition toDtoCondition(InventoryItem.Condition condition) {
        return condition == null ? null : InventoryItemCondition.valueOf(condition.name());
    }
}
