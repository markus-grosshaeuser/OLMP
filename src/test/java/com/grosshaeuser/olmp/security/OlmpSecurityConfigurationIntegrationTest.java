package com.grosshaeuser.olmp.security;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.employees.entity.Employee;
import com.grosshaeuser.olmp.employees.entity.EmployeeAccount;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.security.principal.MemberPrincipal;
import com.grosshaeuser.olmp.security.principal.EmployeePrincipal;
import com.grosshaeuser.olmp.security.service.JwtService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, OlmpSecurityConfigurationIntegrationTest.SecurityTestController.class})
public class OlmpSecurityConfigurationIntegrationTest {

    private static final String AUTH_ENDPOINT = "/api/members/auth/security-test";
    private static final String MEMBER_ENDPOINT = "/api/members/security-test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void authenticationEndpointsAllowAnonymousPostWithoutCsrfOrSession() throws Exception {
        mockMvc.perform(post(AUTH_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"))
                .andExpect(result -> assertThat(result.getRequest().getSession(false)).isNull());
    }

    @Test
    void authenticationEndpointsAreNotProcessedByTheJwtFilter() throws Exception {
        mockMvc.perform(post(AUTH_ENDPOINT).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isOk());
    }

    @Test
    void memberApiRejectsAnonymousRequestsWithoutLoginRedirectOrBasicChallenge() throws Exception {
        mockMvc.perform(get(MEMBER_ENDPOINT))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void memberApiRejectsHttpBasicAuthentication() throws Exception {
        mockMvc.perform(get(MEMBER_ENDPOINT).with(httpBasic("member@example.com", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberApiAcceptsValidMemberJwt() throws Exception {
        mockMvc.perform(get(MEMBER_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithAuthorities("ROLE_MEMBER")))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_EMPLOYEE", "ROLE_ADMIN", "MEMBER"})
    void memberApiRejectsJwtWithoutMemberRole(String authority) throws Exception {
        mockMvc.perform(get(MEMBER_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithAuthorities(authority)))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberApiRejectsInvalidJwt() throws Exception {
        mockMvc.perform(get(MEMBER_ENDPOINT).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void memberApiAllowsAuthenticatedPostWithoutCsrfAndDoesNotCreateSession() throws Exception {
        mockMvc.perform(post(MEMBER_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithAuthorities("ROLE_MEMBER")))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"))
                .andExpect(result -> assertThat(result.getRequest().getSession(false)).isNull());
    }

    @Test
    void memberApiDoesNotReuseAuthenticationFromHttpSession() throws Exception {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken
                .authenticated("member@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        mockMvc.perform(get(MEMBER_ENDPOINT).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void jwtAuthenticationDoesNotCarryOverToTheNextRequest() throws Exception {
        mockMvc.perform(get(MEMBER_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithAuthorities("ROLE_MEMBER")))
                .andExpect(status().isOk());

        mockMvc.perform(get(MEMBER_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/security-test", "/api/members-other/security-test"})
    void fallbackChainDeniesOtherApisEvenToAuthenticatedAdministrators(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint).with(user("admin").roles("ADMIN", "EMPLOYEE", "MEMBER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void stylesArePublic() throws Exception {
        mockMvc.perform(get("/styles/security-test.css"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedUiRedirectsAnonymousUsersToLogin() throws Exception {
        mockMvc.perform(get("/security-test/private").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void fallbackChainStillRequiresCsrfForAuthenticatedPosts() throws Exception {
        mockMvc.perform(post("/styles/security-test.css").with(user("employee").roles("EMPLOYEE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/styles/security-test.css")
                        .with(user("employee").roles("EMPLOYEE")).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void passwordEncoderUsesDelegatingBcryptEncoding() {
        String encoded = passwordEncoder.encode("test-password");

        assertThat(encoded).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("test-password", encoded)).isTrue();
        assertThat(passwordEncoder.matches("wrong-password", encoded)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"employee", "member"})
    @Transactional
    void authenticationManagersUseTheirOwnUserDetailsServiceAndPasswordEncoder(String accountType) {
        // Matching identifiers and different passwords detect accidentally swapped providers.
        String username = "security-test@example.com";
        Employee employee = new Employee();
        employee.setFirstName("Security");
        employee.setLastName("Test");
        entityManager.persist(employee);
        EmployeeAccount employeeAccount = new EmployeeAccount();
        employeeAccount.setEmployee(employee);
        employeeAccount.setUsername(username);
        employeeAccount.setPasswordHash(passwordEncoder.encode("employee-password"));
        entityManager.persist(employeeAccount);

        Member member = new Member();
        member.setFirstName("Security");
        member.setLastName("Test");
        entityManager.persist(member);
        MemberAccount memberAccount = new MemberAccount();
        memberAccount.setMember(member);
        memberAccount.setEmail(username);
        memberAccount.setPasswordHash(passwordEncoder.encode("member-password"));
        memberAccount.setEnabled(true);
        memberAccount.setVerified(true);
        entityManager.persist(memberAccount);
        entityManager.flush();

        AuthenticationManager manager = applicationContext.getBean(
                accountType + "AuthenticationManager", AuthenticationManager.class);
        var authentication = manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                username, accountType + "-password"));

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(
                accountType.equals("employee") ? EmployeePrincipal.class : MemberPrincipal.class);
        assertThat(authentication.getCredentials()).isNull();

        String otherPassword = accountType.equals("employee") ? "member-password" : "employee-password";
        assertThatThrownBy(() -> manager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, otherPassword)))
                .isInstanceOf(BadCredentialsException.class);
    }

    private String tokenWithAuthorities(String... authorities) {
        Member member = new Member();
        member.setId(1L);
        MemberAccount account = new MemberAccount();
        account.setId(2L);
        account.setMember(member);
        account.setEmail("member@example.com");
        return jwtService.createAccessToken(new MemberPrincipal(account,
                java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList()));
    }

    // Real handlers distinguish successful authorization from a missing route (404).
    @RestController
    static class SecurityTestController {
        @RequestMapping({AUTH_ENDPOINT, MEMBER_ENDPOINT, "/api/security-test",
                "/api/members-other/security-test", "/styles/security-test.css", "/security-test/private"})
        String securityTest() {
            return "ok";
        }
    }
}
