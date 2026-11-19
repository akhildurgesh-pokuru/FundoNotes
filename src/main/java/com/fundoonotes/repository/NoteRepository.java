package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // UC5: Find a note only when it belongs to this user.
    Optional<Note> findByIdAndUserId(
            Long noteId,
            Long userId
    );

    // UC6: Retrieve active notes, pinned first.
    List<Note>
    findByUserIdAndArchivedFalseAndTrashedFalseOrderByPinnedDescUpdatedAtDesc(
            Long userId
    );

    // UC6: Retrieve archived notes.
    List<Note>
    findByUserIdAndArchivedTrueAndTrashedFalseOrderByUpdatedAtDesc(
            Long userId
    );

    // UC6: Retrieve notes in Trash.
    List<Note>
    findByUserIdAndTrashedTrueOrderByUpdatedAtDesc(
            Long userId
    );

    // UC7: Search both title and description.
    @Query("""
            SELECT n
            FROM Note n
            WHERE n.user.id = :userId
              AND n.archived = false
              AND n.trashed = false
              AND (
                  LOWER(n.title) LIKE
                      LOWER(CONCAT('%', :keyword, '%'))
                  OR
                  LOWER(COALESCE(n.description, '')) LIKE
                      LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY n.pinned DESC, n.updatedAt DESC
            """)
    List<Note> searchActiveNotes(
            @Param("userId") Long userId,
            @Param("keyword") String keyword
    );


    // UC8: Get active notes with a specific label.
    @Query("""
    SELECT DISTINCT n
    FROM Note n
    JOIN n.labels l
    WHERE n.user.id = :userId
      AND l.id = :labelId
      AND n.archived = false
      AND n.trashed = false
    ORDER BY n.pinned DESC, n.updatedAt DESC
    """)
    List<Note> findActiveNotesByLabel(
            @Param("userId") Long userId,
            @Param("labelId") Long labelId
    );

    // UC8: Find all note relationships for label deletion.
    // Include archived and trashed notes to clean up every link.
    @Query("""
    SELECT DISTINCT n
    FROM Note n
    JOIN n.labels l
    WHERE n.user.id = :userId
      AND l.id = :labelId
    """)
    List<Note> findAllNotesByLabel(
            @Param("userId") Long userId,
            @Param("labelId") Long labelId
    );


    // UC9: Find upcoming reminders for this user.
    // Archived and trashed notes are excluded.
    @Query("""
    SELECT n
    FROM Note n
    WHERE n.user.id = :userId
      AND n.reminderAt IS NOT NULL
      AND n.reminderAt > :now
      AND n.archived = false
      AND n.trashed = false
    ORDER BY n.reminderAt ASC
    """)
    List<Note> findUpcomingReminders(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );


}
