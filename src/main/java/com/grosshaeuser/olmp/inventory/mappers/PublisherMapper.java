package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherCreateDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherDetailDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherEditDto;
import com.grosshaeuser.olmp.inventory.dto.publisher.PublisherSummaryDto;
import com.grosshaeuser.olmp.inventory.entity.Publisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PublisherMapper {

    public PublisherSummaryDto toSummary(Publisher source) {
        return source == null ? null : new PublisherSummaryDto(
                source.getId(), source.getName(), source.getWebsite());
    }

    public PublisherDetailDto toDetail(Publisher source) {
        return source == null ? null : new PublisherDetailDto(
                source.getId(), source.getName(), source.getWebsite(), source.getCreatedAt());
    }

    public PublisherEditDto toEditDto(Publisher source) {
        if (source == null) {
            return null;
        }
        PublisherEditDto dto = new PublisherEditDto();
        dto.setId(source.getId());
        dto.setName(source.getName());
        dto.setWebsite(source.getWebsite());
        return dto;
    }

    public Publisher toEntity(PublisherCreateDto source) {
        Objects.requireNonNull(source, "source");
        Publisher target = new Publisher();
        copyWritableFields(source, target);
        return target;
    }

    public void update(PublisherEditDto source, Publisher target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        MappingChecks.sameId(source.getId(), target.getId(), "id");
        copyWritableFields(source, target);
    }

    private void copyWritableFields(PublisherCreateDto source, Publisher target) {
        target.setName(source.getName());
        target.setWebsite(source.getWebsite());
    }
}
