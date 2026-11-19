package com.fundoonotes.repository;

import com.fundoonotes.entity.Label;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository
        extends JpaRepository<Label, Long> {

    // Find only labels owned by the logged-in user.
    List<Label> findByUserIdOrderByNameAsc(Long userId);

    // Ownership check for rename, delete, and assignment.
    Optional<Label> findByIdAndUserId(
            Long labelId,
            Long userId
    );

    // Prevent duplicate label names for a single user.
    boolean existsByUserIdAndNameIgnoreCase(
            Long userId,
            String name
    );
}
