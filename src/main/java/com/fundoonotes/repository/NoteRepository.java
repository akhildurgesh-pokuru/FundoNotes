
package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // UC5: Ownership check for every note operation.
    Optional<Note> findByIdAndUserId(
            Long noteId,
            Long userId
    );

    // UC6: Main notes list (exclude archived and trashed).
    List<Note> findByUserIdAndArchivedFalseAndTrashedFalseOrderByPinnedDescUpdatedAtDesc(
            Long userId
    );

    // UC6: Archived notes only.
    List<Note> findByUserIdAndArchivedTrueAndTrashedFalseOrderByUpdatedAtDesc(
            Long userId
    );

    // UC6: Trashed notes only.
    List<Note> findByUserIdAndTrashedTrueOrderByUpdatedAtDesc(
            Long userId
    );
}
