package com.grosshaeuser.olmp.member.service;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.members.dto.*;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import com.grosshaeuser.olmp.members.repository.MemberContactInformationRepository;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.memberships.MembershipStatus;
import com.grosshaeuser.olmp.memberships.MembershipType;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.repository.MembershipRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MemberServiceIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAccountRepository memberAccountRepository;

    @Autowired
    private MemberContactInformationRepository memberContactInformationRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @MockitoBean
    private AuthenticationContext authenticationContext;

    @Autowired
    private MemberService serviceUnderTest;

    @BeforeEach
    void setUp() {
        membershipRepository.deleteAll();
        memberContactInformationRepository.deleteAll();
        memberAccountRepository.deleteAll();
        memberRepository.deleteAll();

        reset(authenticationContext);
    }


    @Test
    void getMembersReturnsAllMembersIfProvidedSearchStringIsEmptyOrNull() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(john);

        MemberRegistrationDTO jane = createBasicMemberRegistrationDTO("Jane", "Doe", LocalDate.of(1995, 5, 15));
        populateWithAccountData(jane, "jane.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(jane);

        MemberRegistrationDTO james = createBasicMemberRegistrationDTO("James", "Doe", LocalDate.of(1985, 10, 20));
        populateWithAccountData(james, "james.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(james);

        MemberRegistrationDTO joan = createBasicMemberRegistrationDTO("Joan", "Doe", LocalDate.of(1975, 7, 25));
        populateWithAccountData(joan, "joan.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(joan);

        assertThat(serviceUnderTest.getMembers("", PageRequest.of(0, 10)).getContent()).hasSize(4);
        assertThat(serviceUnderTest.getMembers(null, PageRequest.of(0, 10)).getContent()).hasSize(4);
    }

    @Test
    void getMembersReturnsOnlyMembersWithAPartialOrFullMatchOfSearchStringAndFirstName() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(john);

        MemberRegistrationDTO jane = createBasicMemberRegistrationDTO("Jane", "Doe", LocalDate.of(1995, 5, 15));
        populateWithAccountData(jane, "jane.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(jane);

        MemberRegistrationDTO james = createBasicMemberRegistrationDTO("James", "Doe", LocalDate.of(1985, 10, 20));
        populateWithAccountData(james, "james.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(james);

        MemberRegistrationDTO joan = createBasicMemberRegistrationDTO("Joan", "Doe", LocalDate.of(1975, 7, 25));
        populateWithAccountData(joan, "joan.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(joan);

        assertThat(serviceUnderTest.getMembers("ame", PageRequest.of(0, 10)).getContent()).hasSize(1);
        assertThat(serviceUnderTest.getMembers("AME", PageRequest.of(0, 10)).getContent()).hasSize(1);
        assertThat(serviceUnderTest.getMembers("aMe", PageRequest.of(0, 10)).getContent()).hasSize(1);

        assertThat(serviceUnderTest.getMembers("ja", PageRequest.of(0, 10)).getContent()).hasSize(2);
        assertThat(serviceUnderTest.getMembers("JA", PageRequest.of(0, 10)).getContent()).hasSize(2);
        assertThat(serviceUnderTest.getMembers("Ja", PageRequest.of(0, 10)).getContent()).hasSize(2);

        assertThat(serviceUnderTest.getMembers("jane", PageRequest.of(0, 10)).getContent()).hasSize(1);
        assertThat(serviceUnderTest.getMembers("JANE", PageRequest.of(0, 10)).getContent()).hasSize(1);
        assertThat(serviceUnderTest.getMembers("jAne", PageRequest.of(0, 10)).getContent()).hasSize(1);
    }

    @Test
    void getMembersReturnsOnlyMembersWithAPartialOrFullMatchOfSearchStringAndLastName() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(john);

        MemberRegistrationDTO jane = createBasicMemberRegistrationDTO("Jane", "Smith", LocalDate.of(1995, 5, 15));
        populateWithAccountData(jane, "jane.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(jane);

        MemberRegistrationDTO james = createBasicMemberRegistrationDTO("James", "Grant", LocalDate.of(1985, 10, 20));
        populateWithAccountData(james, "james.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(james);

        MemberRegistrationDTO joan = createBasicMemberRegistrationDTO("Joan", "Dobbs", LocalDate.of(1975, 7, 25));
        populateWithAccountData(joan, "joan.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(joan);

        assertThat(serviceUnderTest.getMembers("mi", PageRequest.of(0, 10)).getContent()).hasSize(1);
        assertThat(serviceUnderTest.getMembers("MI", PageRequest.of(0, 10)).getContent()).hasSize(1);
        assertThat(serviceUnderTest.getMembers("Mi", PageRequest.of(0, 10)).getContent()).hasSize(1);

        assertThat(serviceUnderTest.getMembers("do", PageRequest.of(0, 10)).getContent()).hasSize(2);
        assertThat(serviceUnderTest.getMembers("DO", PageRequest.of(0, 10)).getContent()).hasSize(2);
        assertThat(serviceUnderTest.getMembers("dO", PageRequest.of(0, 10)).getContent()).hasSize(2);

        assertThat(serviceUnderTest.getMembers("grant", PageRequest.of(0, 10)).getContent()).hasSize(1);
        assertThat(serviceUnderTest.getMembers("GRANT", PageRequest.of(0, 10)).getContent()).hasSize(1);
        assertThat(serviceUnderTest.getMembers("grAnt", PageRequest.of(0, 10)).getContent()).hasSize(1);
    }

    @Test
    void getMembersReturnsAnEmptyListIfNoMembersMatchSearchString() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(john);

        MemberRegistrationDTO jane = createBasicMemberRegistrationDTO("Jane", "Doe", LocalDate.of(1995, 5, 15));
        populateWithAccountData(jane, "jane.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(jane);

        MemberRegistrationDTO james = createBasicMemberRegistrationDTO("James", "Doe", LocalDate.of(1985, 10, 20));
        populateWithAccountData(james, "james.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(james);

        MemberRegistrationDTO joan = createBasicMemberRegistrationDTO("Joan", "Doe", LocalDate.of(1975, 7, 25));
        populateWithAccountData(joan, "joan.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(joan);

        assertThat(serviceUnderTest.getMembers("grant", PageRequest.of(0, 10)).getContent()).hasSize(0);
    }

    @Test
    void getMembersReturnsAnEmptyListIfThereAreNoMembersInTheDatabase() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        assertThat(serviceUnderTest.getMembers("", PageRequest.of(0, 10)).getContent()).hasSize(0);
    }





    @Test
    void createMemberCreatesMemberAndMemberAccountFromProvidedDataIfNeitherContactDetailsNorMembershipAreProvided() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        serviceUnderTest.createMember(john);

        MemberDTO member = serviceUnderTest.getMembers("doe", PageRequest.of(0, 10)).getContent().getFirst();
        assertThat(member.firstName()).isEqualTo("John");
        assertThat(member.lastName()).isEqualTo("Doe");

        Optional<MemberAccount> accountResult = memberAccountRepository.findByMemberId(member.id());
        assertThat(accountResult).isPresent();

        MemberAccount account = accountResult.get();
        assertThat(account.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(account.getPasswordHash()).isNotNull();
        assertThat(account.isVerified()).isTrue();
        assertThat(account.isEnabled()).isTrue();

        Optional<MemberContactInformation> contactInfoResult = memberContactInformationRepository.findByMemberId(member.id());
        assertEquals(Optional.empty(), contactInfoResult);

        Optional<Membership> membershipResult = membershipRepository.findActiveByMemberId(member.id());
        assertEquals(Optional.empty(), membershipResult);
    }

    @Test
    void createMemberCreatesMemberAndMemberAccountAndMemberContactInformationIfContactDetailsAreProvided() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        populateWithContactDetails(john, "123-456789", "Main Street", "1", "12345", "Berlin", "Germany");
        serviceUnderTest.createMember(john);

        MemberDTO member = serviceUnderTest.getMembers("doe", PageRequest.of(0, 10)).getContent().getFirst();
        assertThat(member.firstName()).isEqualTo("John");
        assertThat(member.lastName()).isEqualTo("Doe");

        Optional<MemberAccount> accountResult = memberAccountRepository.findByMemberId(member.id());
        assertThat(accountResult).isPresent();
        MemberAccount account = accountResult.get();
        assertThat(account.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(account.getPasswordHash()).isNotNull();
        assertThat(account.isVerified()).isTrue();
        assertThat(account.isEnabled()).isTrue();

        Optional<MemberContactInformation> contactInfoResult = memberContactInformationRepository.findByMemberId(member.id());
        assertThat(contactInfoResult).isPresent();
        MemberContactInformation contactInfo = contactInfoResult.get();
        assertThat(contactInfo.getPhoneNumber()).isEqualTo("123-456789");
        assertThat(contactInfo.getStreet()).isEqualTo("Main Street");
        assertThat(contactInfo.getHouseNumber()).isEqualTo("1");
        assertThat(contactInfo.getPostalCode()).isEqualTo("12345");
        assertThat(contactInfo.getCity()).isEqualTo("Berlin");
        assertThat(contactInfo.getCountry()).isEqualTo("Germany");

        Optional<Membership> membershipResult = membershipRepository.findActiveByMemberId(member.id());
        assertEquals(Optional.empty(), membershipResult);
    }

    @Test
    void createMemberCreatesMemberAndMemberAccountAndMembershipIfMembershipIsProvided() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        populateWithMembership(john, "STANDARD", "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), null, null);
        serviceUnderTest.createMember(john);

        MemberDTO member = serviceUnderTest.getMembers("doe", PageRequest.of(0, 10)).getContent().getFirst();
        assertThat(member.firstName()).isEqualTo("John");
        assertThat(member.lastName()).isEqualTo("Doe");

        Optional<MemberAccount> accountResult = memberAccountRepository.findByMemberId(member.id());
        assertThat(accountResult).isPresent();
        MemberAccount account = accountResult.get();
        assertThat(account.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(account.getPasswordHash()).isNotNull();
        assertThat(account.isVerified()).isTrue();
        assertThat(account.isEnabled()).isTrue();

        Optional<Membership> membershipResult = membershipRepository.findActiveByMemberId(member.id());
        assertThat(membershipResult).isPresent();
        Membership membership = membershipResult.get();
        assertThat(membership.getType()).isEqualTo(MembershipType.valueOf("STANDARD"));
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.valueOf("ACTIVE"));
        assertThat(membership.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(membership.getExpirationDate()).isEqualTo(LocalDate.now().plusYears(1));

        Optional<MemberContactInformation> contactInformationResult = memberContactInformationRepository.findByMemberId(member.id());
        assertEquals(Optional.empty(), contactInformationResult);
    }

    @Test
    void createMemberCreatesMemberAndMemberAccountAndMemberContactInformationAndMembershipIfContactDetailsAndMembershipAreProvided() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        populateWithContactDetails(john, "123-456789", "Main Street", "1", "12345", "Berlin", "Germany");
        populateWithMembership(john, "STANDARD", "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), null, null);
        serviceUnderTest.createMember(john);

        MemberDTO member = serviceUnderTest.getMembers("doe", PageRequest.of(0, 10)).getContent().getFirst();
        assertThat(member.firstName()).isEqualTo("John");
        assertThat(member.lastName()).isEqualTo("Doe");

        Optional<MemberAccount> accountResult = memberAccountRepository.findByMemberId(member.id());
        assertThat(accountResult).isPresent();
        MemberAccount account = accountResult.get();
        assertThat(account.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(account.getPasswordHash()).isNotNull();
        assertThat(account.isVerified()).isTrue();
        assertThat(account.isEnabled()).isTrue();

        Optional<MemberContactInformation> contactInfoResult = memberContactInformationRepository.findByMemberId(member.id());
        assertThat(contactInfoResult).isPresent();
        MemberContactInformation contactInfo = contactInfoResult.get();
        assertThat(contactInfo.getPhoneNumber()).isEqualTo("123-456789");
        assertThat(contactInfo.getStreet()).isEqualTo("Main Street");
        assertThat(contactInfo.getHouseNumber()).isEqualTo("1");
        assertThat(contactInfo.getPostalCode()).isEqualTo("12345");
        assertThat(contactInfo.getCity()).isEqualTo("Berlin");
        assertThat(contactInfo.getCountry()).isEqualTo("Germany");

        Optional<Membership> membershipResult = membershipRepository.findActiveByMemberId(member.id());
        assertThat(membershipResult).isPresent();
        Membership membership = membershipResult.get();
        assertThat(membership.getType()).isEqualTo(MembershipType.valueOf("STANDARD"));
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.valueOf("ACTIVE"));
        assertThat(membership.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(membership.getExpirationDate()).isEqualTo(LocalDate.now().plusYears(1));
    }






    @Test
    void getDetailedMemberReturnsOptionalEmptyIfNoMemberWithTheProvidedIdExists() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        Optional<MemberEditDTO> result = serviceUnderTest.getDetailedMember(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void getDetailedMemberReturnsMergedMemberAndMemberAccountAndMemberContactInformationAndMembershipDto() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        populateWithContactDetails(john, "123-456789", "Main Street", "1", "12345", "Berlin", "Germany");
        populateWithMembership(john, "STANDARD", "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), null, null);
        Optional<MemberDTO> member = serviceUnderTest.createMember(john);
        assertThat(member).isPresent();

        Optional<MemberEditDTO> result = serviceUnderTest.getDetailedMember(member.get().id());
        assertThat(result).isPresent();
        MemberEditDTO dto = result.get();
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        MemberContactInformationEditDTO contactInfo = dto.getContact();
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getPhoneNumber()).isEqualTo("123-456789");
        assertThat(contactInfo.getStreet()).isEqualTo("Main Street");
        MembershipEditDTO membership = dto.getMemberships().getFirst();
        assertThat(membership).isNotNull();
        assertThat(membership.getMembershipType()).isEqualTo("STANDARD");
        assertThat(membership.getMembershipStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void getDetailedMemberReturnsMergedMemberAndMemberAccountDtoWithEmptyContactInformationAndMembershipIfNeitherContactInformationNoMembershipExists() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        Optional<MemberDTO> member = serviceUnderTest.createMember(john);
        assertThat(member).isPresent();

        Optional<MemberEditDTO> result = serviceUnderTest.getDetailedMember(member.get().id());
        assertThat(result).isPresent();
        MemberEditDTO dto = result.get();
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        MemberContactInformationEditDTO contactInfo = dto.getContact();
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getPhoneNumber()).isNull();
        assertThat(contactInfo.getStreet()).isNull();
        assertThat(dto.getMemberships()).isEmpty();
    }

    @Test
    void getDetailedMemberReturnsMergedMemberAndMemberAccountAndMemberContactInformationDtoWithEmptyMembershipIfNoMembershipExists() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        populateWithContactDetails(john, "123-456789", "Main Street", "1", "12345", "Berlin", "Germany");
        Optional<MemberDTO> member = serviceUnderTest.createMember(john);
        assertThat(member).isPresent();

        Optional<MemberEditDTO> result = serviceUnderTest.getDetailedMember(member.get().id());
        assertThat(result).isPresent();
        MemberEditDTO dto = result.get();
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        MemberContactInformationEditDTO contactInfo = dto.getContact();
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getPhoneNumber()).isEqualTo("123-456789");
        assertThat(contactInfo.getStreet()).isEqualTo("Main Street");
        assertThat(dto.getMemberships()).isEmpty();
    }

    @Test
    void getDetailedMemberReturnsMergedMemberAndMemberAccountAndMembershipDtoWithEmptyContactInformationIfNoContactInformationExists() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        populateWithMembership(john, "STANDARD", "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), null, null);
        Optional<MemberDTO> member = serviceUnderTest.createMember(john);
        assertThat(member).isPresent();

        Optional<MemberEditDTO> result = serviceUnderTest.getDetailedMember(member.get().id());
        assertThat(result).isPresent();
        MemberEditDTO dto = result.get();
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        MemberContactInformationEditDTO contactInfo = dto.getContact();
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getPhoneNumber()).isNull();
        assertThat(contactInfo.getStreet()).isNull();
        MembershipEditDTO membership = dto.getMemberships().getFirst();
        assertThat(membership).isNotNull();
        assertThat(membership.getMembershipType()).isEqualTo("STANDARD");
        assertThat(membership.getMembershipStatus()).isEqualTo("ACTIVE");
    }





    @Test
    void updateMemberReturnOptionalEmptyIfTheProvidedMemberIdDoesNotExist() {
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        MemberEditDTO updateDto = createBasicMemberEditDTO(999L, "Missing", "Member", LocalDate.of(1990, 1, 1));
        assertThat(serviceUnderTest.updateMember(updateDto)).isEmpty();
    }

    @Test
    void loadsAllMembershipsNewestFirstEvenWithoutAnActiveMembership() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);
        Member member = memberRepository.save(Member.builder().firstName("Ada").lastName("Lovelace").build());
        Membership older = saveMembership(member, "MBR-OLD", LocalDate.of(2024, 1, 1), MembershipStatus.EXPIRED);
        Membership newer = saveMembership(member, "MBR-NEW", LocalDate.of(2025, 1, 1), MembershipStatus.CANCELLED);

        assertThat(serviceUnderTest.getDetailedMember(member.getId()).orElseThrow().getMemberships())
                .extracting(MembershipEditDTO::getId).containsExactly(newer.getId(), older.getId());
    }

    @Test
    void savesHistoricalEditsAndNewMembershipAndReloadsGeneratedIdentity() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBERSHIP);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        Member member = memberRepository.save(Member.builder().firstName("Ada").lastName("Lovelace").build());
        Membership old = saveMembership(member, "MBR-OLD", LocalDate.of(2024, 1, 1), MembershipStatus.EXPIRED);
        MemberEditDTO dto = serviceUnderTest.getDetailedMember(member.getId()).orElseThrow();
        dto.setAccount(null);
        dto.setWithMembershipDetails(true);
        dto.getMemberships().getFirst().setMembershipType("STUDENT");
        var rows = new ArrayList<>(dto.getMemberships());
        rows.add(MembershipEditDTO.builder().membershipType("PREMIUM").membershipStatus("ACTIVE")
                .startDate(LocalDate.of(2026, 1, 1)).build());
        dto.setMemberships(rows);

        var result = serviceUnderTest.updateMember(dto).orElseThrow().getMemberships();
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isNotNull().isNotEqualTo(old.getId());
        assertThat(result.getFirst().getMembershipNumber()).isNotBlank().isNotEqualTo("MBR-OLD");
        assertThat(result.getFirst().getExpirationDate()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(result.getLast().getMembershipType()).isEqualTo("STUDENT");
        assertThat(result.getLast().getMembershipStatus()).isEqualTo("EXPIRED");
        assertThat(result.getLast().getMembershipNumber()).isEqualTo("MBR-OLD");
        assertThat(result.getLast().getStartDate()).isEqualTo(old.getStartDate());
        assertThat(serviceUnderTest.getDetailedMember(member.getId()).orElseThrow().getMemberships()).isEqualTo(result);
    }

    @Test
    void rollsBackProfileAndHistoricalEditsWhenNewMembershipCannotBeCreated() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBERSHIP);
        Member member = memberRepository.save(Member.builder().firstName("Ada").lastName("Lovelace").build());
        Membership old = saveMembership(member, "MBR-OLD", LocalDate.of(2024, 1, 1), MembershipStatus.EXPIRED);
        MemberEditDTO dto = serviceUnderTest.getDetailedMember(member.getId()).orElseThrow();
        dto.setAccount(null);
        dto.setFirstName("Augusta");
        dto.setWithMembershipDetails(true);
        dto.getMemberships().getFirst().setMembershipType("STUDENT");
        var rows = new ArrayList<>(dto.getMemberships());
        rows.add(MembershipEditDTO.builder().membershipType("STANDARD").membershipStatus("ACTIVE")
                .startDate(LocalDate.now()).build());
        dto.setMemberships(rows);

        assertThatThrownBy(() -> serviceUnderTest.updateMember(dto))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getFirstName()).isEqualTo("Ada");
        assertThat(membershipRepository.findById(old.getId()).orElseThrow().getType()).isEqualTo(MembershipType.STANDARD);
        assertThat(membershipRepository.count()).isEqualTo(1);
    }

    private Membership saveMembership(Member member, String number, LocalDate start, MembershipStatus status) {
        return membershipRepository.save(Membership.builder().member(member).membershipNumber(number)
                .type(MembershipType.STANDARD).status(status).startDate(start)
                .expirationDate(start.plusYears(1)).build());
    }


    @Test
    void updateMemberReturnsMergedMemberAndMemberAccountAndMemberContactInformationAndMembershipDto() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBERSHIP);
        givenUserHasPermission(VERIFY_MEMBER);

        MemberRegistrationDTO registrationDto = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(registrationDto, "john@doe.com", "passworD%123", true, true);
        populateWithContactDetails(registrationDto, "123456789", "Main St", "1", "12345", "Street", "Country");
        populateWithMembership(registrationDto, "STANDARD", "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), LocalDate.now().plusYears(1), LocalDate.now().plusYears(1));

        MemberDTO member = serviceUnderTest.createMember(registrationDto).orElseThrow();

        MemberEditDTO updateDto = serviceUnderTest.getDetailedMember(member.id()).orElseThrow();
        assertNotNull(updateDto.getId());

        MemberAccountEditDTO accountUpdate = updateDto.getAccount();
        accountUpdate.setEmail("new@doe.com");
        accountUpdate.setNewPassword("newPassword!23");
        updateDto.setAccount(accountUpdate);

        MemberContactInformationEditDTO contactUpdate = updateDto.getContact();
        assertNotNull(contactUpdate);
        contactUpdate.setPhoneNumber("987654321");
        contactUpdate.setStreet("New St");
        updateDto.setContact(contactUpdate);
        updateDto.setWithContactInformation(true);

        MembershipEditDTO membershipUpdate = updateDto.getMemberships().getFirst();
        assertNotNull(membershipUpdate);
        membershipUpdate.setMembershipType("STUDENT");
        updateDto.setMemberships(List.of(membershipUpdate));
        updateDto.setWithMembershipDetails(true);

        Optional<MemberEditDTO> updatedMember = serviceUnderTest.updateMember(updateDto);

        assertTrue(updatedMember.isPresent());
        MemberEditDTO updatedMemberDto = updatedMember.get();

        assertNotNull(updatedMemberDto.getAccount());
        assertNotNull(updatedMemberDto.getContact());
        assertNotNull(updatedMemberDto.getMemberships().getFirst());

        assertEquals("new@doe.com", updatedMemberDto.getAccount().getEmail());
        assertEquals("987654321", updatedMemberDto.getContact().getPhoneNumber());
        assertEquals("STUDENT", updatedMemberDto.getMemberships().getFirst().getMembershipType());
    }

    @Test
    void updateMemberReturnsMergedDtoWithExistingAccountDataIfNoNewAccountDataIsProvided() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBERSHIP);
        givenUserHasPermission(VERIFY_MEMBER);

        MemberRegistrationDTO registrationDto = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(registrationDto, "john@doe.com", "passworD%123", true, true);
        populateWithContactDetails(registrationDto, "123456789", "Main St", "1", "12345", "Street", "Country");
        populateWithMembership(registrationDto, "STANDARD", "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), LocalDate.now().plusYears(1), LocalDate.now().plusYears(1));

        MemberDTO member = serviceUnderTest.createMember(registrationDto).orElseThrow();
        assertNotNull(member.id());

        MemberEditDTO updateDto = serviceUnderTest.getDetailedMember(member.id()).orElseThrow();

        MemberContactInformationEditDTO contactUpdate = updateDto.getContact();
        assertNotNull(contactUpdate);
        contactUpdate.setPhoneNumber("987654321");
        updateDto.setContact(contactUpdate);
        updateDto.setWithContactInformation(true);

        MembershipEditDTO membershipUpdate = updateDto.getMemberships().getFirst();
        assertNotNull(membershipUpdate);
        membershipUpdate.setMembershipType("STUDENT");
        updateDto.setMemberships(List.of(membershipUpdate));
        updateDto.setWithMembershipDetails(true);

        Optional<MemberEditDTO> updatedMemberDtoOpt = serviceUnderTest.updateMember(updateDto);

        assertTrue(updatedMemberDtoOpt.isPresent());
        MemberEditDTO updatedMemberDto = updatedMemberDtoOpt.get();

        assertNotNull(updatedMemberDto.getAccount());
        assertNotNull(updatedMemberDto.getContact());
        assertNotNull(updatedMemberDto.getMemberships().getFirst());

        assertEquals("john@doe.com", updatedMemberDto.getAccount().getEmail());
        assertEquals("987654321", updatedMemberDto.getContact().getPhoneNumber());
        assertEquals("STUDENT", updatedMemberDto.getMemberships().getFirst().getMembershipType());
    }

    @Test
    void updateMemberReturnsMergedDtoWithExistingContactInformationIfNoNewContactInformationIsProvided() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBERSHIP);
        givenUserHasPermission(VERIFY_MEMBER);

        MemberRegistrationDTO registrationDto = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(registrationDto, "john@doe.com", "passworD%123", true, true);
        populateWithContactDetails(registrationDto, "123456789", "Main St", "1", "12345", "Street", "Country");
        populateWithMembership(registrationDto, "STANDARD", "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), LocalDate.now().plusYears(1), LocalDate.now().plusYears(1));

        MemberDTO member = serviceUnderTest.createMember(registrationDto).orElseThrow();
        assertNotNull(member.id());

        MemberEditDTO updateDto = serviceUnderTest.getDetailedMember(member.id()).orElseThrow();

        MemberAccountEditDTO accountUpdate = updateDto.getAccount();
        assertNotNull(accountUpdate);
        accountUpdate.setEmail("new@doe.com");
        updateDto.setAccount(accountUpdate);

        MembershipEditDTO membershipUpdate = updateDto.getMemberships().getFirst();
        assertNotNull(membershipUpdate);
        membershipUpdate.setMembershipType("STUDENT");
        updateDto.setMemberships(List.of(membershipUpdate));
        updateDto.setWithMembershipDetails(true);

        Optional<MemberEditDTO> updatedMemberDtoOpt = serviceUnderTest.updateMember(updateDto);

        assertTrue(updatedMemberDtoOpt.isPresent());
        MemberEditDTO updatedMemberDto = updatedMemberDtoOpt.get();

        assertNotNull(updatedMemberDto.getAccount());
        assertNotNull(updatedMemberDto.getContact());
        assertNotNull(updatedMemberDto.getMemberships().getFirst());

        assertEquals("new@doe.com", updatedMemberDto.getAccount().getEmail());
        assertEquals("123456789", updatedMemberDto.getContact().getPhoneNumber());
        assertEquals("Main St", updatedMemberDto.getContact().getStreet());
        assertEquals("STUDENT", updatedMemberDto.getMemberships().getFirst().getMembershipType());
    }

    @Test
    void updateMemberReturnsMergedDtoWithExistingMembershipDataIfNoNewMembershipDataIsProvided() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(READ_MEMBERSHIP);
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBERSHIP);
        givenUserHasPermission(VERIFY_MEMBER);

        MemberRegistrationDTO registrationDto = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(registrationDto, "john@doe.com", "passworD%123", true, true);
        populateWithContactDetails(registrationDto, "123456789", "Main St", "1", "12345", "Street", "Country");
        populateWithMembership(registrationDto, "STANDARD", "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), LocalDate.now().plusYears(1), LocalDate.now().plusYears(1));

        MemberDTO member = serviceUnderTest.createMember(registrationDto).orElseThrow();
        assertNotNull(member.id());

        MemberEditDTO updateDto = serviceUnderTest.getDetailedMember(member.id()).orElseThrow();

        MemberAccountEditDTO accountUpdate = updateDto.getAccount();
        assertNotNull(accountUpdate);
        accountUpdate.setEmail("new@doe.com");
        updateDto.setAccount(accountUpdate);

        MemberContactInformationEditDTO contactUpdate = updateDto.getContact();
        assertNotNull(contactUpdate);
        contactUpdate.setPhoneNumber("987654321");
        updateDto.setContact(contactUpdate);
        updateDto.setWithContactInformation(true);

        Optional<MemberEditDTO> updatedMemberDtoOpt = serviceUnderTest.updateMember(updateDto);

        assertTrue(updatedMemberDtoOpt.isPresent());
        MemberEditDTO updatedMemberDto = updatedMemberDtoOpt.get();

        assertNotNull(updatedMemberDto.getAccount());
        assertNotNull(updatedMemberDto.getContact());
        assertNotNull(updatedMemberDto.getMemberships().getFirst());

        assertEquals("new@doe.com", updatedMemberDto.getAccount().getEmail());
        assertEquals("987654321", updatedMemberDto.getContact().getPhoneNumber());
        assertEquals("STANDARD", updatedMemberDto.getMemberships().getFirst().getMembershipType());
        assertEquals("ACTIVE", updatedMemberDto.getMemberships().getFirst().getMembershipStatus());
    }





    @Test
    void deleteMemberRemovesMemberAndCascadedData() {
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBERSHIP);
        givenUserHasPermission(DELETE_MEMBER_ACCOUNT);

        MemberRegistrationDTO john = createBasicMemberRegistrationDTO("John", "Doe", LocalDate.of(1990, 1, 1));
        populateWithAccountData(john, "john.doe@example.com", "paSword1#", true, true);
        populateWithContactDetails(john, "123-456789", "Main Street", "1", "12345", "Berlin", "Germany");
        populateWithMembership(john, "STANDARD", "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), null, null);

        Long memberId = serviceUnderTest.createMember(john).orElseThrow().id();

        assertThat(memberRepository.findById(memberId)).isPresent();
        assertThat(memberAccountRepository.findByMemberId(memberId)).isPresent();
        assertThat(memberContactInformationRepository.findByMemberId(memberId)).isPresent();
        assertThat(membershipRepository.findActiveByMemberId(memberId)).isPresent();

        serviceUnderTest.deleteMember(memberId);

        assertThat(memberRepository.findById(memberId)).isEmpty();
        assertThat(memberAccountRepository.findByMemberId(memberId)).isEmpty();
        assertThat(memberContactInformationRepository.findByMemberId(memberId)).isEmpty();
        assertThat(membershipRepository.findActiveByMemberId(memberId)).isEmpty();
    }





    private void givenUserHasPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(true);
    }

    private static MemberRegistrationDTO createBasicMemberRegistrationDTO(
            String firstName,
            String lastName,
            LocalDate dateOfBirth
    ) {
        MemberRegistrationDTO dto = new MemberRegistrationDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setDateOfBirth(dateOfBirth);
        dto.setWithContactDetails(false);
        dto.setWithMembership(false);
        return dto;
    }

    public static void populateWithAccountData(
            MemberRegistrationDTO dto,
            String email,
            String password,
            boolean verified,
            boolean enabled
    ) {
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setVerified(verified);
        dto.setEnabled(enabled);
    }

    public static void populateWithContactDetails(
            MemberRegistrationDTO dto,
            String phoneNumber,
            String street,
            String houseNumber,
            String postalCode,
            String city,
            String country
    ) {
        dto.setWithContactDetails(true);
        dto.setPhoneNumber(phoneNumber);
        dto.setStreet(street);
        dto.setHouseNumber(houseNumber);
        dto.setPostalCode(postalCode);
        dto.setCity(city);
        dto.setCountry(country);
    }

    public static void populateWithMembership(
            MemberRegistrationDTO dto,
            String membershipType,
            String membershipStatus,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate renewalDate,
            LocalDate cancellationDate
    ) {
        dto.setWithMembership(true);
        dto.setMembershipType(membershipType);
        dto.setMembershipStatus(membershipStatus);
        dto.setMembershipStartDate(startDate);
        dto.setMembershipEndDate(endDate);
        dto.setMembershipRenewalDate(renewalDate);
        dto.setMembershipCancellationDate(cancellationDate);
    }

    public static MemberEditDTO createBasicMemberEditDTO(Long id, String firstName, String lastName, LocalDate birthDate) {
        return MemberEditDTO.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .birthDate(birthDate)
                .build();
    }
}
