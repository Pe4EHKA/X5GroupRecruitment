package com.x5.recruitment.infrastructure.security;

import com.x5.recruitment.domain.model.User;
import com.x5.recruitment.domain.model.UserRole;
import com.x5.recruitment.domain.model.UserStatus;
import com.x5.recruitment.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for authentication with real users and password encoding.
 * Tests the complete authentication flow from HTTP Basic Auth to database validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean up existing test users
        userRepository.deleteAll();

        // Create test admin user with encoded password
        User admin = User.builder()
            .username("testadmin")
            .email("testadmin@test.com")
            .firstName("Test")
            .lastName("Admin")
            .passwordHash(passwordEncoder.encode("admin123"))
            .roles(Set.of(UserRole.ADMIN))
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();
        userRepository.save(admin);

        // Create test recruiter user
        User recruiter = User.builder()
            .username("testrecruiter")
            .email("testrecruiter@test.com")
            .firstName("Test")
            .lastName("Recruiter")
            .passwordHash(passwordEncoder.encode("recruiter123"))
            .roles(Set.of(UserRole.RECRUITER))
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();
        userRepository.save(recruiter);
    }

    @Test
    void testAuthentication_withValidCredentials_shouldSucceed() throws Exception {
        // Test admin authentication
        mockMvc.perform(get("/actuator/health")
                .with(httpBasic("testadmin", "admin123")))
            .andExpect(status().isOk());
    }

    @Test
    void testAuthentication_withWrongPassword_shouldFail() throws Exception {
        // Test authentication with wrong password
        mockMvc.perform(get("/actuator/health")
                .with(httpBasic("testadmin", "wrongpassword")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testAuthentication_withNonExistentUser_shouldFail() throws Exception {
        // Test authentication with non-existent user
        mockMvc.perform(get("/actuator/health")
                .with(httpBasic("nonexistent", "password")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testAuthentication_multipleUsers_withCorrectPasswords() throws Exception {
        // Test that multiple users can authenticate with their respective passwords
        
        // Admin
        mockMvc.perform(get("/actuator/health")
                .with(httpBasic("testadmin", "admin123")))
            .andExpect(status().isOk());

        // Recruiter
        mockMvc.perform(get("/actuator/health")
                .with(httpBasic("testrecruiter", "recruiter123")))
            .andExpect(status().isOk());
    }

    @Test
    void testAuthentication_inactiveUser_shouldFail() throws Exception {
        // Create an inactive user
        User inactiveUser = User.builder()
            .username("inactive")
            .email("inactive@test.com")
            .firstName("Inactive")
            .lastName("User")
            .passwordHash(passwordEncoder.encode("password123"))
            .roles(Set.of(UserRole.RECRUITER))
            .status(UserStatus.DISABLED)
            .active(false)
            .build();
        userRepository.save(inactiveUser);

        // Should fail even with correct password
        mockMvc.perform(get("/actuator/health")
                .with(httpBasic("inactive", "password123")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testAuthentication_caseSensitivePassword() throws Exception {
        // Passwords should be case-sensitive
        mockMvc.perform(get("/actuator/health")
                .with(httpBasic("testadmin", "Admin123"))) // Wrong case
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/health")
                .with(httpBasic("testadmin", "ADMIN123"))) // Wrong case
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testAuthentication_withoutCredentials_shouldFail() throws Exception {
        // Request without authentication should fail for protected endpoints
        // Note: /actuator/health is actually public in our config, so use a protected endpoint
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized());
    }
}
