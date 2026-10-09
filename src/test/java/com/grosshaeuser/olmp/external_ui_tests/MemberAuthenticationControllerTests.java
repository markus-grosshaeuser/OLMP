
package com.grosshaeuser.olmp.external_ui_tests;

import tools.jackson.databind.ObjectMapper;
import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.security.dto.MemberLoginRequest;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class MemberAuthenticationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAccountRepository memberAccountRepository;

    private final String testEmail = "test@member.com";
    private final String testPassword = "password123";

    @BeforeEach
    void setUp() {
        memberAccountRepository.deleteAll();
        memberRepository.deleteAll();

        Member member = new Member();
        member.setFirstName("Test");
        member.setLastName("User");
        member = memberRepository.save(member);

        MemberAccount account = new MemberAccount();
        account.setMember(member);
        account.setEmail(testEmail);
        account.setPasswordHash(passwordEncoder.encode(testPassword));
        account.setEnabled(true);
        account.setVerified(true);
        account.setLocked(false);
        memberAccountRepository.save(account);
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        MemberLoginRequest request = new MemberLoginRequest(testEmail, testPassword);

        mockMvc.perform(post("/api/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresInSeconds", notNullValue()));
    }

    @Test
    void loginWithInvalidPasswordReturnsUnauthorized() throws Exception {
        MemberLoginRequest request = new MemberLoginRequest(testEmail, "wrong-password");

        mockMvc.perform(post("/api/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithNonExistentEmailReturnsUnauthorized() throws Exception {
        MemberLoginRequest request = new MemberLoginRequest("unknown@example.com", testPassword);

        mockMvc.perform(post("/api/members/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}