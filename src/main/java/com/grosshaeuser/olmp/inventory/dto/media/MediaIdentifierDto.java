package com.grosshaeuser.olmp.inventory.dto.media;

public record MediaIdentifierDto(
        Long id,
        String identifierType,
        String identifierValue
) {
}

