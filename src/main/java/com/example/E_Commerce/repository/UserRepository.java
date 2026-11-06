
package com.example.E_Commerce.repository;
import java.util.List;
import com.example.E_Commerce.entity.User;
import com.example.E_Commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    Optional<User> findByEmail(String email);

    List<User> findByRoleIgnoreCaseOrderByCreatedAtDesc(String role);
    // Check whether email already exists
    boolean existsByEmail(String email);

    long countByRoleIgnoreCase(String role);

}
