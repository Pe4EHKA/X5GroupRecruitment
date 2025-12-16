package com.x5.recruitment.infrastructure.config;

import com.x5.recruitment.domain.model.User;
import com.x5.recruitment.domain.model.UserRole;
import com.x5.recruitment.domain.model.UserStatus;
import com.x5.recruitment.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Development data initializer.
 * Ensures that test users exist with correct passwords for development and testing.
 * Only active in 'dev' or 'default' profiles.
 */
@Configuration
@Profile({"dev", "default"})
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Bean
    public CommandLineRunner initDevData() {
        return args -> {
            log.info("=== Development Data Initializer ===");
            
            // Check if we should reset passwords
            boolean resetPasswords = Boolean.parseBoolean(
                environment.getProperty("app.dev.reset-passwords", "false")
            );
            
            if (resetPasswords) {
                log.warn("Password reset enabled (app.dev.reset-passwords=true)");
            }
            
            // Ensure admin user exists with correct password
            ensureTestUser("admin", "admin@x5.ru", "admin123", "Admin", "User", 
                Set.of(UserRole.ADMIN), resetPasswords);
            
            // Ensure recruiter user exists with correct password
            ensureTestUser("recruiter", "recruiter@x5.ru", "recruiter123", "Test", "Recruiter",
                Set.of(UserRole.RECRUITER), resetPasswords);
            
            // Ensure HM user exists with correct password
            ensureTestUser("hm", "hm@x5.ru", "hm123", "Test", "HiringManager",
                Set.of(UserRole.HM), resetPasswords);
            
            log.info("=== Development data initialization complete ===");
            log.info("Test credentials:");
            log.info("  Admin:     username=admin      password=admin123");
            log.info("  Recruiter: username=recruiter  password=recruiter123");
            log.info("  HM:        username=hm         password=hm123");
        };
    }

    private void ensureTestUser(String username, String email, String password,
                                String firstName, String lastName,
                                Set<UserRole> roles, boolean resetPassword) {
        
        userRepository.findByUsername(username).ifPresentOrElse(
            user -> {
                // User exists
                log.info("User '{}' already exists (id: {})", username, user.getId());
                
                // Ensure user is active
                if (user.getStatus() != UserStatus.ACTIVE) {
                    user.setStatus(UserStatus.ACTIVE);
                    user.setActive(true);
                    userRepository.save(user);
                    log.info("  - Activated user '{}'", username);
                }
                
                // Ensure user has correct roles
                if (!rolesAreEqual(user.getRoles(), roles)) {
                    user.setRoles(roles);
                    userRepository.save(user);
                    log.info("  - Updated roles for '{}': {}", username, roles);
                }
                
                // Reset password if flag is set
                if (resetPassword) {
                    user.setPasswordHash(passwordEncoder.encode(password));
                    userRepository.save(user);
                    log.warn("  - RESET password for '{}' to '{}'", username, password);
                }
            },
            () -> {
                // User doesn't exist, create it
                User newUser = User.builder()
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .passwordHash(passwordEncoder.encode(password))
                    .roles(roles)
                    .status(UserStatus.ACTIVE)
                    .active(true)
                    .build();
                
                userRepository.save(newUser);
                log.info("Created test user '{}' with password '{}'", username, password);
            }
        );
    }

    /**
     * Compare two sets of UserRoles for equality.
     * HashSet.equals() works correctly for this case, but being explicit for clarity.
     */
    private boolean rolesAreEqual(Set<UserRole> roles1, Set<UserRole> roles2) {
        return roles1.size() == roles2.size() && roles1.containsAll(roles2);
    }
}
