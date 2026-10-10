package com.grosshaeuser.olmp.members.dto;

public record MemberDTO(
        Long id,
        String lastName,
        String firstName,
        String dateOfBirth,
        String creationTimestamp,
        String lastModifiedTimestamp
) {
}
