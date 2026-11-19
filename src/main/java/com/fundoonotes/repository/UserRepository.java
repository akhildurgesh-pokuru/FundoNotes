
package com.fundoonotes.repository;

import com.fundoonotes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Finds a registered user using their email address.
    Optional<User> findByEmail(String email);

    // Checks whether an email is already registered.
    boolean existsByEmail(String email);
}
