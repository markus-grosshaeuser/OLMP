package com.grosshaeuser.olmp.inventory.mappers;

import com.grosshaeuser.olmp.inventory.dto.author.AuthorCreateDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorDetailDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorEditDto;
import com.grosshaeuser.olmp.inventory.dto.author.AuthorSummaryDto;
import com.grosshaeuser.olmp.inventory.entity.Author;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AuthorMapper {

    public AuthorSummaryDto toSummary(Author source) {
        return source == null ? null : new AuthorSummaryDto(
                source.getId(), source.getFirstName(), source.getLastName());
    }

    public AuthorDetailDto toDetail(Author source) {
        return source == null ? null : new AuthorDetailDto(
                source.getId(), source.getFirstName(), source.getLastName(), source.getCreatedAt());
    }

    public AuthorEditDto toEditDto(Author source) {
        if (source == null) {
            return null;
        }
        AuthorEditDto dto = new AuthorEditDto();
        dto.setId(source.getId());
        dto.setFirstName(source.getFirstName());
        dto.setLastName(source.getLastName());
        return dto;
    }

    public Author toEntity(AuthorCreateDto source) {
        Objects.requireNonNull(source, "source");
        Author target = new Author();
        copyWritableFields(source, target);
        return target;
    }

    public void update(AuthorEditDto source, Author target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        MappingChecks.sameId(source.getId(), target.getId(), "id");
        copyWritableFields(source, target);
    }

    private void copyWritableFields(AuthorCreateDto source, Author target) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
    }
}
