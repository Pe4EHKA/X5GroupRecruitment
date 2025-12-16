package com.x5.recruitment.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility test to generate BCrypt hashes for test users.
 * Run this test and copy the output hashes to the migration file.
 */
class HashGeneratorTest {

    @Test
    void generateHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        String admin123 = encoder.encode("admin123");
        String recruiter123 = encoder.encode("recruiter123");
        String hm123 = encoder.encode("hm123");
        
        System.out.println("\n=== Generated BCrypt Hashes (strength 10) ===");
        System.out.println("Password: admin123");
        System.out.println("Hash: " + admin123);
        System.out.println();
        System.out.println("Password: recruiter123");
        System.out.println("Hash: " + recruiter123);
        System.out.println();
        System.out.println("Password: hm123");
        System.out.println("Hash: " + hm123);
        System.out.println();
        
        // Verify they work
        System.out.println("=== Verification ===");
        System.out.println("admin123 matches: " + encoder.matches("admin123", admin123));
        System.out.println("recruiter123 matches: " + encoder.matches("recruiter123", recruiter123));
        System.out.println("hm123 matches: " + encoder.matches("hm123", hm123));
        System.out.println();
    }
}
