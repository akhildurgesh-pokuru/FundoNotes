
package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Retrieve only notes owned by the authenticated user.
    List<Note> findByUserIdOrderByUpdatedAtDesc(Long userId);

    // Both note ID and owner ID must match.
    Optional<Note> findByIdAndUserId(Long noteId, Long userId);
}
