package com.grosshaeuser.olmp.rest_api_sru;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.security.entities.ApiKey;
import com.grosshaeuser.olmp.security.entities.Privilege;
import com.grosshaeuser.olmp.security.entities.Role;
import com.grosshaeuser.olmp.security.entities.User;
import com.grosshaeuser.olmp.security.repositories.ApiKeyRepository;
import com.grosshaeuser.olmp.security.repositories.PrivilegeRepository;
import com.grosshaeuser.olmp.security.repositories.RoleRepository;
import com.grosshaeuser.olmp.security.repositories.UserRepository;
import com.grosshaeuser.olmp.security.services.ApiKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class SruApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ApiKeyGenerator apiKeyGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "password123";

    private ApiKey apiKey;
    private String rawApiKeySecret;

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
        user = userRepository.save(user);

        this.apiKey = apiKeyGenerator.generateApiKeyForUser(user);
        rawApiKeySecret = this.apiKey.getSecret();
        this.apiKey.setSecret(passwordEncoder.encode(rawApiKeySecret));
        apiKeyRepository.save(this.apiKey);
    }

    @Test
    void requestWithValidApiKeyReturnsOK() throws Exception {
        String xApiKey = apiKey.getId() + "." + rawApiKeySecret;
        mockMvc.perform(get("/sru/protected").header("Authorization", "X-API-KEY: " + xApiKey))
            .andExpect(status().isOk());
    }

    @Test
    void requestWithInvalidApiKeyReturnsUnauthorized() throws Exception {
        String xApiKey = apiKey.getId() + "." + "invalid-secret";
        mockMvc.perform(get("/sru/protected").header("Authorization", "X-API-KEY: " + xApiKey))
            .andExpect(status().isUnauthorized());
    }
}
