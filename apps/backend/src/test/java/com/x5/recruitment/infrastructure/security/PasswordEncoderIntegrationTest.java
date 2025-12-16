package com.x5.recruitment.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for password encoding and authentication.
 * Verifies that the PasswordEncoder is correctly configured and can match passwords.
 */
@SpringBootTest
@ActiveProfiles("test")
class PasswordEncoderIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testPasswordEncoderBean_exists() {
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    void testPasswordEncoder_encodeAndMatch() {
        // Test encoding and matching
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Verify encoded password is not the same as raw
        assertThat(encodedPassword).isNotEqualTo(rawPassword);

        // Verify encoded password matches
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();

        // Verify wrong password doesn't match
        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    @Test
    void testPasswordEncoder_bcryptFormat() {
        // Test that encoded passwords follow BCrypt format
        String encoded = passwordEncoder.encode("test");
        
        // BCrypt hash should start with $2a$, $2b$, or $2y$
        assertThat(encoded).matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    @Test
    void testPasswordEncoder_multipleEncodings_produceDifferentHashes() {
        // BCrypt should produce different hashes for the same password (due to random salt)
        String password = "samePassword";
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // Hashes should be different
        assertThat(hash1).isNotEqualTo(hash2);

        // But both should match the original password
        assertThat(passwordEncoder.matches(password, hash1)).isTrue();
        assertThat(passwordEncoder.matches(password, hash2)).isTrue();
    }

    @Test
    void testPasswordEncoder_testUserPasswords() {
        // Test that the documented test user passwords can be encoded and matched
        String[] testPasswords = {"admin123", "recruiter123", "hm123"};

        for (String password : testPasswords) {
            String encoded = passwordEncoder.encode(password);
            assertThat(passwordEncoder.matches(password, encoded))
                .as("Password '%s' should match its encoded version", password)
                .isTrue();
        }
    }

    @Test
    void testPasswordEncoder_knownHashMatches() {
        // Verify that the hashes from V8 migration match the expected passwords
        // These hashes were generated with BCryptPasswordEncoder(10)
        
        String adminHash = "$2a$10$ly3U/9WO2TzWJsGYr8WREe.IoTbksltexVVZPYZcWNRpa5e2mT7jG";
        String recruiterHash = "$2a$10$OX70brqYIzKQLphVNBeFCuFNE0mgX8CJGqiKQjpmfV.fFoKaoAzhS";
        String hmHash = "$2a$10$ZGAdXZAA/PLI4QTKbMMPq.10.EEFzQmLaS1S5m4eIOtMQOTlinE5G";

        assertThat(passwordEncoder.matches("admin123", adminHash))
            .as("admin123 should match admin hash")
            .isTrue();
        
        assertThat(passwordEncoder.matches("recruiter123", recruiterHash))
            .as("recruiter123 should match recruiter hash")
            .isTrue();
        
        assertThat(passwordEncoder.matches("hm123", hmHash))
            .as("hm123 should match hm hash")
            .isTrue();
    }

    @Test
    void testPasswordEncoder_wrongPasswordDoesNotMatch() {
        String hash = "$2a$10$ly3U/9WO2TzWJsGYr8WREe.IoTbksltexVVZPYZcWNRpa5e2mT7jG";
        
        // Wrong passwords should not match
        assertThat(passwordEncoder.matches("wrongpassword", hash)).isFalse();
        assertThat(passwordEncoder.matches("admin", hash)).isFalse();
        assertThat(passwordEncoder.matches("Admin123", hash)).isFalse(); // case sensitive
        assertThat(passwordEncoder.matches("", hash)).isFalse();
    }
}
