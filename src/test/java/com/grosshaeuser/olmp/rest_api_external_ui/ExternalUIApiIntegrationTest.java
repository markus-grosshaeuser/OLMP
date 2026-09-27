package com.grosshaeuser.olmp.rest_api_external_ui;

import com.grosshaeuser.olmp.security.entities.Privilege;
import com.grosshaeuser.olmp.security.entities.Role;
import com.grosshaeuser.olmp.security.entities.User;
import com.grosshaeuser.olmp.security.repositories.PrivilegeRepository;
import com.grosshaeuser.olmp.security.repositories.RoleRepository;
import com.grosshaeuser.olmp.security.repositories.UserRepository;
import com.grosshaeuser.olmp.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ExternalUiApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        privilegeRepository.deleteAll();

        Privilege privilege = new Privilege();
        privilege.setName("READ");
        privilegeRepository.save(privilege);

        Role userRole = new Role();
        userRole.setName("USER");
        userRole.addPrivilege(privilege);
        roleRepository.save(userRole);

        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.addRole(userRole);
        user.setEnabled(true);
        user.setVerified(true);
        userRepository.save(user);
    }

    @Test
    void loginAttemptWithCorrectCredentialsReturnsValidJwt() throws Exception {
        ExternalUiController.LoginRequest loginRequest = new ExternalUiController.LoginRequest(TEST_USERNAME, TEST_PASSWORD);

        mockMvc.perform(post("/ui/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String token = result.getResponse().getContentAsString();
                    var user = userRepository.findByUsernameWithRolesAndPrivileges(TEST_USERNAME).orElseThrow();
                    var userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(user.getUsername())
                            .password(user.getPassword())
                            .authorities(user.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                                    .collect(Collectors.toList()))
                            .build();
                    assert(jwtService.isTokenValid(token, userDetails));
                });
    }

    @Test
    void loginAttemptWithWrongCredentialsReturnsUnauthorized() throws Exception {
        ExternalUiController.LoginRequest loginRequest = new ExternalUiController.LoginRequest(TEST_USERNAME, "wrong_password");
        mockMvc.perform(post("/ui/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessExistentProtectedEndpointWithValidTokenReturnsOk() throws Exception {
        String token = loginSuccessfully();
        mockMvc.perform(get("/ui/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void accessNonExistentEndpointWithValidTokenReturnsNotFound() throws Exception {
        String token = loginSuccessfully();
        mockMvc.perform(get("/ui/non-existent")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void accessExistentProtectedEndpointWithoutTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/ui/protected"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessNonExistentProtectedEndpointWithoutTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/ui/non-existent"))
                .andExpect(status().isForbidden());
    }

    private String loginSuccessfully() throws Exception {
        ExternalUiController.LoginRequest loginRequest = new ExternalUiController.LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        var result = mockMvc.perform(post("/ui/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();
        return result.getResponse().getContentAsString();
    }
}
