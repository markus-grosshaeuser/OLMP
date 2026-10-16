package com.grosshaeuser.olmp.member.service;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.members.dto.MemberAccountCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberAccountEditDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.members.service.MemberAccountService;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static com.grosshaeuser.olmp.security.SecurityConstants.Privileges.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class MemberAccountServiceIntegrationTest {

    @Autowired
    private MemberAccountRepository memberAccountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationContext authenticationContext;

    @Autowired
    private MemberAccountService serviceUnderTest;


    @BeforeEach
    void setUp() {
        memberAccountRepository.deleteAll();
        memberRepository.deleteAll();
    }


    @Test
    void findByMemberIdReturnsOptionalEmptyIfUserWithProvidedIdDoesNotExist() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        Long memberId = 1L;

        Optional<MemberAccount> result = serviceUnderTest.findByMemberId(memberId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByMemberIdReturnsMemberAccountDataForTheProvidedId() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);

        Member member = memberRepository.save(Member.builder().firstName("John").lastName("Doe").build());

        MemberAccount account = memberAccountRepository.save(MemberAccount.builder()
                .member(member)
                .email("test@example.com")
                .passwordHash("hash")
                .verified(true)
                .enabled(true)
                .build());

        Optional<MemberAccount> result = serviceUnderTest.findByMemberId(member.getId());

        assertTrue(result.isPresent());
        assertEquals(account.getEmail(), result.get().getEmail());
    }

    @Test
    void createMemberAccountCreatesAccountFromProvidedDataWithHashedPasswordAndReturnsCreatedMember() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(CREATE_MEMBER_ACCOUNT);

        String email = "new@example.com";
        String password = "Password%1";

        Member member = memberRepository.save(Member.builder().firstName("John").lastName("Doe").build());

        MemberAccountCreateDTO dto = new MemberAccountCreateDTO();
        dto.setMember(member);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setVerified(true);
        dto.setEnabled(true);

        Optional<MemberAccount> result = serviceUnderTest.createMemberAccount(dto);

        assertTrue(result.isPresent());
        MemberAccount created = result.get();
        assertTrue(passwordEncoder.matches(password, created.getPasswordHash()));
        assertEquals(email, created.getEmail());
    }

    @Test
    void updateReturnsOptionalEmptyIfNoAccountWithTheIdFromProvidedDtoExists() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);

        Long id = 1L;
        MemberAccountEditDTO dto = MemberAccountEditDTO.builder()
                .id(id)
                .build();

        Optional<MemberAccount> result = serviceUnderTest.update(dto);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateReturnsMemberAccountWithUpdatedValuesFromProvidedDto() {
        givenUserHasPermission(READ_MEMBER_ACCOUNT);
        givenUserHasPermission(UPDATE_MEMBER_ACCOUNT);
        givenUserHasPermission(VERIFY_MEMBER);

        Member member = memberRepository.save(Member.builder().firstName("John").lastName("Doe").build());
        MemberAccount account = memberAccountRepository.save(MemberAccount.builder()
                .member(member)
                .email("old@example.com")
                .passwordHash("oldhash")
                .verified(false)
                .enabled(true)
                .build());

        String newEmail = "new@example.com";
        String newPassword = "NewPassword123!";
        MemberAccountEditDTO dto = MemberAccountEditDTO.builder()
                .id(account.getId())
                .email(newEmail)
                .newPassword(newPassword)
                .verified(true)
                .enabled(false)
                .build();

        Optional<MemberAccount> result = serviceUnderTest.update(dto);

        assertTrue(result.isPresent());
        MemberAccount updated = result.get();
        assertEquals(newEmail, updated.getEmail());
        assertTrue(updated.isVerified());
        assertFalse(updated.isEnabled());
        assertTrue(passwordEncoder.matches(newPassword, updated.getPasswordHash()));
    }



    private void givenUserHasPermission(String permission) {
        when(authenticationContext.hasAuthority(permission)).thenReturn(true);
    }
}