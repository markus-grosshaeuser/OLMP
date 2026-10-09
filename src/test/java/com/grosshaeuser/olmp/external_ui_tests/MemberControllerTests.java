package com.grosshaeuser.olmp.external_ui_tests;

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
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class MemberControllerTests {

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
    private String validToken;


    @BeforeEach
    void setUp() throws Exception {
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

        MemberLoginRequest loginRequest = new MemberLoginRequest(testEmail, testPassword);
        String response = mockMvc.perform(
                        post("/api/members/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        validToken = objectMapper.readTree(response).get("accessToken").asString();
    }

    @Test
    void getWithValidJwtReturnsOk() throws Exception {
        mockMvc.perform(get("/api/members/")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Dummy"));
    }

    @Test
    void get_WithInvalidJwt_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/members/")
                        .header("Authorization", "Bearer invalid-token-here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getWithoutJwtReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/members/"))
                .andExpect(status().isForbidden());
    }



    @Test
    void postToNonExistentEndpointWithValidJwtReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/members/nonexistent")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void postToNonExistentEndpointWithInvalidJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/members/nonexistent")
                .header("Authorization", "Bearer invalid-token-here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postToNonExistentEndpointWithoutJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/members/nonexistent"))
                .andExpect(status().isForbidden());
    }

}
