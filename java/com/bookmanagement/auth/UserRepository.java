// src/main/java/com/bookmanagement/auth/UserRepository.java
package com.bookmanagement.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    default Optional<User> findByUsernameOrEmail(String input) {
        return input.contains("@") ? findByEmail(input) : findByUsername(input);
    }

    @Query("SELECT u FROM User u WHERE TYPE(u) = AdminUser")
    List<User> findAllAdmins();

    @Query("SELECT u FROM User u WHERE TYPE(u) = Customer")
    List<User> findAllCustomers();
}