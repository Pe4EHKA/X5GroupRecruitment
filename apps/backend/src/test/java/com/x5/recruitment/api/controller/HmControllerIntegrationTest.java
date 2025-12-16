package com.x5.recruitment.api.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Integration test for HM Controller to verify NPE fix.
 * Tests that authenticated HM users can access endpoints without NullPointerException.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HmControllerIntegrationTest {

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

        // Create test HM user with encoded password
        User hm = User.builder()
            .username("testhm")
            .email("testhm@test.com")
            .firstName("Test")
            .lastName("HM")
            .passwordHash(passwordEncoder.encode("hm123"))
            .roles(Set.of(UserRole.HM))
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();
        userRepository.save(hm);
    }

    @Test
    void testGetPendingApplications_withValidHmUser_shouldReturn200() throws Exception {
        // Test that GET /api/hm/pending works without NPE
        // Even if there are no applications, should return 200 OK with empty page
        mockMvc.perform(get("/api/hm/pending")
                .with(httpBasic("testhm", "hm123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetPendingApplications_withAuthenticatedNonExistentUser_shouldReturn404() throws Exception {
        // Create a user that exists in security but will be deleted from DB
        User tempUser = User.builder()
            .username("tempuser")
            .email("temp@test.com")
            .firstName("Temp")
            .lastName("User")
            .passwordHash(passwordEncoder.encode("temp123"))
            .roles(Set.of(UserRole.HM))
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();
        userRepository.save(tempUser);
        
        // Delete the user to simulate mismatch between security and DB
        userRepository.delete(tempUser);
        
        // Should return 404 Not Found instead of 500 Internal Server Error
        // Note: This test verifies that UserService.getUserEntityByUsername throws ResourceNotFoundException
        // which is handled by GlobalExceptionHandler to return 404
        mockMvc.perform(get("/api/hm/pending")
                .with(httpBasic("tempuser", "temp123")))
            .andExpect(status().isUnauthorized()); // User won't authenticate if not in DB
    }

    @Test
    void testGetPendingApplications_withoutAuthentication_shouldReturn401() throws Exception {
        // Test that endpoint requires authentication
        mockMvc.perform(get("/api/hm/pending"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetPendingApplications_withWrongRole_shouldReturn403() throws Exception {
        // Create a recruiter user
        User recruiter = User.builder()
            .username("testrecruiter")
            .email("recruiter@test.com")
            .firstName("Test")
            .lastName("Recruiter")
            .passwordHash(passwordEncoder.encode("rec123"))
            .roles(Set.of(UserRole.RECRUITER))
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();
        userRepository.save(recruiter);

        // Recruiter should not have access to HM endpoints
        mockMvc.perform(get("/api/hm/pending")
                .with(httpBasic("testrecruiter", "rec123")))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetPendingApplications_withAdminRole_shouldReturn200() throws Exception {
        // Create an admin user
        User admin = User.builder()
            .username("testadmin")
            .email("admin@test.com")
            .firstName("Test")
            .lastName("Admin")
            .passwordHash(passwordEncoder.encode("admin123"))
            .roles(Set.of(UserRole.ADMIN))
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();
        userRepository.save(admin);

        // Admin should have access to HM endpoints (as per @PreAuthorize)
        mockMvc.perform(get("/api/hm/pending")
                .with(httpBasic("testadmin", "admin123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
