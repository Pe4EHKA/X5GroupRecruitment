package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.User;
import com.x5.recruitment.domain.model.UserRole;
import com.x5.recruitment.domain.model.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailNormalized(String emailNormalized);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailNormalized(String emailNormalized);
    
    /**
     * Find users by status
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    /**
     * Search users by query (email, username, or full name)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);
    
    /**
     * Find users by role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    Page<User> findByRole(@Param("role") UserRole role, Pageable pageable);
    
    /**
     * Count active users with ADMIN role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = 'ADMIN' AND u.status = 'ACTIVE'")
    long countActiveAdmins();
    
    /**
     * Find all users with a specific role and status
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.status = :status")
    List<User> findByRoleAndStatus(@Param("role") UserRole role, @Param("status") UserStatus status);
}
