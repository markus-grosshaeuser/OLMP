package com.grosshaeuser.olmp.member.service;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.members.dto.MemberContactInformationCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberContactInformationEditDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import com.grosshaeuser.olmp.members.repository.MemberContactInformationRepository;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.members.service.MemberContactInformationService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MemberContactInformationServiceIntegrationTest {

    @Autowired
    private MemberContactInformationService memberContactInformationService;

    @Autowired
    private MemberContactInformationRepository memberContactInformationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        memberContactInformationRepository.deleteAll();
        memberRepository.deleteAll();
    }


    @Test
    void findByMemberIdReturnsOptionalEmptyIfUserWithProvidedIdDoesNotExist() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        Optional<MemberContactInformation> result = memberContactInformationService.findByMemberId(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByMemberIdReturnsMemberContactInformationForTheProvidedId() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);

        Member member = Member.builder().firstName("John").lastName("Doe").build();
        memberRepository.save(member);

        MemberContactInformationCreateDTO createDto = new MemberContactInformationCreateDTO();
        createDto.setMember(member);
        createDto.setPhoneNumber("123456789");
        createDto.setStreet("Main Street");
        createDto.setHouseNumber("1");
        createDto.setPostalCode("12345");
        createDto.setCity("City");
        createDto.setCountry("Country");

        memberContactInformationService.create(createDto);

        Optional<MemberContactInformation> result = memberContactInformationService.findByMemberId(member.getId());
        assertTrue(result.isPresent());
        assertEquals("123456789", result.get().getPhoneNumber());
        assertEquals(member.getId(), result.get().getMember().getId());
    }

    @Test
    void createCreatesMemberContactInformationFromProvidedDataAndReturnsCreatedMemberContactInformation() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);

        Member member = Member.builder().firstName("John").lastName("Doe").build();
        memberRepository.save(member);

        MemberContactInformationCreateDTO createDto = new MemberContactInformationCreateDTO();
        createDto.setMember(member);
        createDto.setPhoneNumber("123456789");
        createDto.setStreet("Main Street");
        createDto.setHouseNumber("1");
        createDto.setPostalCode("12345");
        createDto.setCity("City");
        createDto.setCountry("Country");

        Optional<MemberContactInformation> result = memberContactInformationService.create(createDto);

        assertTrue(result.isPresent());
        assertEquals("123456789", result.get().getPhoneNumber());
        assertEquals(member.getId(), result.get().getMember().getId());
    }

    @Test
    void updateContactInformationReturnsOptionalEmptyIfNoAccountWithTheIdFromProvidedDtoExists() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);

        MemberContactInformationEditDTO editDto = new MemberContactInformationEditDTO();
        editDto.setId(999L);

        Optional<MemberContactInformation> result = memberContactInformationService.updateContactInformation(editDto);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateContactInformationReturnsMemberContactInformationWithUpdatedValuesFromProvidedDto() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);

        Member member = Member.builder().firstName("John").lastName("Doe").build();
        memberRepository.save(member);

        MemberContactInformationCreateDTO createDto = new MemberContactInformationCreateDTO();
        createDto.setMember(member);
        createDto.setPhoneNumber("old");
        createDto.setStreet("old street");
        createDto.setHouseNumber("old");
        createDto.setPostalCode("old");
        createDto.setCity("old");
        createDto.setCountry("old");

        MemberContactInformation created = memberContactInformationService.create(createDto).orElseThrow();

        MemberContactInformationEditDTO editDto = new MemberContactInformationEditDTO();
        editDto.setId(created.getId());
        editDto.setPhoneNumber("new");
        editDto.setStreet("new street");
        editDto.setHouseNumber("new");
        editDto.setPostalCode("new");
        editDto.setCity("new");
        editDto.setCountry("new");

        Optional<MemberContactInformation> result = memberContactInformationService.updateContactInformation(editDto);

        assertTrue(result.isPresent());
        assertEquals("new", result.get().getPhoneNumber());
        assertEquals("new street", result.get().getStreet());
    }



    private void givenUserHasPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(true);
    }
}