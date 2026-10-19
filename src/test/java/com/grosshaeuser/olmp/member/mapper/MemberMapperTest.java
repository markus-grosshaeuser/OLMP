package com.grosshaeuser.olmp.member.mapper;

import com.grosshaeuser.olmp.members.dto.MemberAccountCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberAccountEditDTO;
import com.grosshaeuser.olmp.members.dto.MemberContactInformationCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberContactInformationEditDTO;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import com.grosshaeuser.olmp.members.mapper.MemberMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MemberMapperTest {

    @Test
    void mapMemberToMemberDTOMapsAllFieldsAndFormatsDates() {
        Member member = member(
                1L,
                "Doe",
                "John",
                LocalDate.of(1990, 1, 1)
        );

        MemberDTO result = MemberMapper.mapMemberToMemberDTO(member);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.dateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(result.creationTimestamp()).isEqualTo(LocalDateTime.of(2026, 10, 12, 10, 0));
        assertThat(result.lastModifiedTimestamp()).isEqualTo(LocalDateTime.of(2026, 10, 12, 11, 0));
    }

    @Test
    void mapMemberToMemberDTOReturnsEmptyStringsForNullDates() {
        Member member = Member.builder()
                .id(1L)
                .lastName("Doe")
                .firstName("John")
                .dateOfBirth(null)
                .createdAt(null)
                .updatedAt(null)
                .build();

        MemberDTO result = MemberMapper.mapMemberToMemberDTO(member);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.dateOfBirth()).isNull();
        assertThat(result.creationTimestamp()).isNull();
        assertThat(result.lastModifiedTimestamp()).isNull();
    }

    @Test
    void createMemberFromMemberRegistrationDataMapsBasicMemberFields() {
        MemberRegistrationDTO dto = registrationDto();

        Member result = MemberMapper.createMemberFromMemberRegistrationData(dto);

        assertThat(result.getId()).isNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createAccountDtoFromMemberRegistrationDataMapsAccountCreationFields() {
        MemberRegistrationDTO dto = registrationDto();
        Member member = member(
                10L,
                "Doe",
                "John",
                LocalDate.of(1990, 1, 1)
        );

        MemberAccountCreateDTO result = MemberMapper.createAccountDtoFromMemberRegistrationData(dto, member);

        assertThat(result.getMember()).isSameAs(member);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.org");
        assertThat(result.getPassword()).isEqualTo("plain-password");
        assertThat(result.isVerified()).isTrue();
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void createContactDetailsDtoFromMemberRegistrationDataMapsContactCreationFields() {
        MemberRegistrationDTO dto = registrationDto();
        Member member = member(
                10L,
                "Doe",
                "John",
                LocalDate.of(1990, 1, 1)
        );

        MemberContactInformationCreateDTO result =
                MemberMapper.createContactDetailsDtoFromMemberRegistrationData(dto, member);

        assertThat(result.getMember()).isSameAs(member);
        assertThat(result.getPhoneNumber()).isEqualTo("+49 123 456789");
        assertThat(result.getStreet()).isEqualTo("Main Street");
        assertThat(result.getHouseNumber()).isEqualTo("1");
        assertThat(result.getPostalCode()).isEqualTo("12345");
        assertThat(result.getCity()).isEqualTo("Berlin");
        assertThat(result.getCountry()).isEqualTo("Germany");
    }

    @Test
    void mergeMemberInformationToEditDtoMapsMemberAccountAndContactInformation() {
        Member member = member(
                1L,
                "Doe",
                "John",
                LocalDate.of(1990, 1, 1)
        );

        MemberAccount account = account(
                20L,
                member,
                "john.doe@example.org"
        );

        MemberContactInformation contactInformation = contactInformation(
                30L,
                member
        );

        MemberEditDTO result = MemberMapper.mergeMemberInformationToEditDto(
                member,
                account,
                contactInformation
        );

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 10, 0));
        assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 11, 0));

        assertThat(result.getAccount()).isNotNull();
        assertThat(result.getAccount().getId()).isEqualTo(20L);
        assertThat(result.getAccount().getEmail()).isEqualTo("john.doe@example.org");
        assertThat(result.getAccount().isVerified()).isTrue();
        assertThat(result.getAccount().isEnabled()).isFalse();
        assertThat(result.getAccount().getFailedLoginAttempts()).isEqualTo(2);
        assertThat(result.getAccount().getLockoutUntil()).isEqualTo(LocalDateTime.of(2026, 10, 13, 12, 0));
        assertThat(result.getAccount().isLocked()).isTrue();
        assertThat(result.getAccount().getAccountCreatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 10, 15));
        assertThat(result.getAccount().getAccountUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 11, 15));
        assertThat(result.getAccount().getAccountLastAccessedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 12, 15));

        assertThat(result.getContact()).isNotNull();
        assertThat(result.getContact().getId()).isEqualTo(30L);
        assertThat(result.getContact().getMemberId()).isEqualTo(1L);
        assertThat(result.getContact().getPhoneNumber()).isEqualTo("+49 123 456789");
        assertThat(result.getContact().getStreet()).isEqualTo("Main Street");
        assertThat(result.getContact().getHouseNumber()).isEqualTo("1");
        assertThat(result.getContact().getPostalCode()).isEqualTo("12345");
        assertThat(result.getContact().getCity()).isEqualTo("Berlin");
        assertThat(result.getContact().getCountry()).isEqualTo("Germany");
    }

    @Test
    void mergeMemberInformationToEditDtoHandlesNullTimestamps() {
        Member member = Member.builder()
                .id(1L)
                .lastName("Doe")
                .firstName("John")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .createdAt(null)
                .updatedAt(null)
                .build();

        MemberEditDTO result = MemberMapper.mergeMemberInformationToEditDto(
                member,
                null,
                null
        );

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();

        assertThat(result.getAccount()).isNotNull();
        assertThat(result.getAccount().getId()).isNull();
        assertThat(result.getAccount().getEmail()).isNull();

        assertThat(result.getContact()).isNotNull();
        assertThat(result.getContact().getId()).isNull();
        assertThat(result.getContact().getMemberId()).isNull();
    }

    @Test
    void mapMemberAccountToEditDtoMapsAllFieldsAndFormatsDates() {
        Member member = member(
                1L,
                "Doe",
                "John",
                LocalDate.of(1990, 1, 1)
        );

        MemberAccount account = account(
                20L,
                member,
                "john.doe@example.org"
        );

        MemberAccountEditDTO result = MemberMapper.mapMemberAccountToEditDto(account);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.org");
        assertThat(result.getNewPassword()).isEmpty();
        assertThat(result.isVerified()).isTrue();
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(result.getLockoutUntil()).isEqualTo(LocalDateTime.of(2026, 10, 13, 12, 0));
        assertThat(result.isLocked()).isTrue();
        assertThat(result.getAccountCreatedAt()).isEqualTo(LocalDateTime.of(2026, 10,12, 10, 15));
        assertThat(result.getAccountUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 11, 15));
        assertThat(result.getAccountLastAccessedAt()).isEqualTo(LocalDateTime.of(2026, 10, 12, 12, 15));
    }

    @Test
    void mapMemberAccountToEditDtoReturnsEmptyDtoWhenAccountIsNull() {
        MemberAccountEditDTO result = MemberMapper.mapMemberAccountToEditDto(null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getNewPassword()).isEmpty();
        assertThat(result.isVerified()).isFalse();
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getFailedLoginAttempts()).isZero();
        assertThat(result.getLockoutUntil()).isNull();
        assertThat(result.isLocked()).isFalse();
        assertThat(result.getAccountCreatedAt()).isNull();
        assertThat(result.getAccountUpdatedAt()).isNull();
        assertThat(result.getAccountLastAccessedAt()).isNull();
    }

    @Test
    void mapMemberAccountToEditDtoReturnsEmptyStringsForNullDates() {
        MemberAccount account = MemberAccount.builder()
                .id(20L)
                .email("john.doe@example.org")
                .passwordHash("password-hash")
                .verified(true)
                .enabled(true)
                .failedLoginAttempts(0)
                .locked(false)
                .lockoutUntil(null)
                .createdAt(null)
                .updatedAt(null)
                .lastAccess(null)
                .build();

        MemberAccountEditDTO result = MemberMapper.mapMemberAccountToEditDto(account);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getLockoutUntil()).isNull();
        assertThat(result.getAccountCreatedAt()).isNull();
        assertThat(result.getAccountUpdatedAt()).isNull();
        assertThat(result.getAccountLastAccessedAt()).isNull();
    }

    @Test
    void mapMemberContactInformationToEditDtoMapsAllFields() {
        Member member = member(
                1L,
                "Doe",
                "John",
                LocalDate.of(1990, 1, 1)
        );

        MemberContactInformation contactInformation = contactInformation(
                30L,
                member
        );

        MemberContactInformationEditDTO result =
                MemberMapper.mapMemberContactInformationToEditDto(contactInformation);

        assertThat(result.getId()).isEqualTo(30L);
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getPhoneNumber()).isEqualTo("+49 123 456789");
        assertThat(result.getStreet()).isEqualTo("Main Street");
        assertThat(result.getHouseNumber()).isEqualTo("1");
        assertThat(result.getPostalCode()).isEqualTo("12345");
        assertThat(result.getCity()).isEqualTo("Berlin");
        assertThat(result.getCountry()).isEqualTo("Germany");
    }

    @Test
    void mapMemberContactInformationToEditDtoReturnsEmptyDtoWhenContactInformationIsNull() {
        MemberContactInformationEditDTO result =
                MemberMapper.mapMemberContactInformationToEditDto(null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getMemberId()).isNull();
        assertThat(result.getPhoneNumber()).isNull();
        assertThat(result.getStreet()).isNull();
        assertThat(result.getHouseNumber()).isNull();
        assertThat(result.getPostalCode()).isNull();
        assertThat(result.getCity()).isNull();
        assertThat(result.getCountry()).isNull();
    }

    @Test
    void mapMemberContactInformationToEditDtoReturnsEmptyDtoWhenMemberIsNull() {
        MemberContactInformation contactInformation = MemberContactInformation.builder()
                .id(30L)
                .member(null)
                .phoneNumber("+49 123 456789")
                .street("Main Street")
                .houseNumber("1")
                .postalCode("12345")
                .city("Berlin")
                .country("Germany")
                .build();

        MemberContactInformationEditDTO result =
                MemberMapper.mapMemberContactInformationToEditDto(contactInformation);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getMemberId()).isNull();
        assertThat(result.getPhoneNumber()).isNull();
        assertThat(result.getStreet()).isNull();
        assertThat(result.getHouseNumber()).isNull();
        assertThat(result.getPostalCode()).isNull();
        assertThat(result.getCity()).isNull();
        assertThat(result.getCountry()).isNull();
    }

    private static MemberRegistrationDTO registrationDto() {
        MemberRegistrationDTO dto = new MemberRegistrationDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));

        dto.setEmail("john.doe@example.org");
        dto.setPassword("plain-password");
        dto.setVerified(true);
        dto.setEnabled(true);

        dto.setPhoneNumber("+49 123 456789");
        dto.setStreet("Main Street");
        dto.setHouseNumber("1");
        dto.setPostalCode("12345");
        dto.setCity("Berlin");
        dto.setCountry("Germany");

        return dto;
    }

    private static Member member(
            Long id,
            String lastName,
            String firstName,
            LocalDate dateOfBirth
    ) {
        return Member.builder()
                .id(id)
                .lastName(lastName)
                .firstName(firstName)
                .dateOfBirth(dateOfBirth)
                .createdAt(OffsetDateTime.parse("2026-10-12T10:00:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-10-12T11:00:00Z"))
                .build();
    }

    private static MemberAccount account(
            Long id,
            Member member,
            String email
    ) {
        return MemberAccount.builder()
                .id(id)
                .member(member)
                .email(email)
                .passwordHash("password-hash")
                .verified(true)
                .enabled(false)
                .failedLoginAttempts(2)
                .lockoutUntil(OffsetDateTime.parse("2026-10-13T12:00:00Z"))
                .locked(true)
                .createdAt(OffsetDateTime.parse("2026-10-12T10:15:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-10-12T11:15:00Z"))
                .lastAccess(OffsetDateTime.parse("2026-10-12T12:15:00Z"))
                .build();
    }

    private static MemberContactInformation contactInformation(
            Long id,
            Member member
    ) {
        return MemberContactInformation.builder()
                .id(id)
                .member(member)
                .phoneNumber("+49 123 456789")
                .street("Main Street")
                .houseNumber("1")
                .postalCode("12345")
                .city("Berlin")
                .country("Germany")
                .build();
    }
}