package com.fundoonotes.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Column;


@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // These fields will be used in UC6.
    @Column(nullable = false)
    private boolean pinned = false;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private boolean trashed = false;


    // UC9: Optional reminder timestamp.
    // Instant represents an exact moment in UTC.
    @Column(name = "reminder_at")
    private Instant reminderAt;

    public Instant getReminderAt() {
        return reminderAt;
    }

    public void setReminderAt(Instant reminderAt) {
        this.reminderAt = reminderAt;
    }


    // UC15: When the note most recently entered Trash.
    // NULL means it has no known trash-entry timestamp.
    @Column(name = "trashed_at")
    private Instant trashedAt;

    public Instant getTrashedAt() {
        return trashedAt;
    }

    public void setTrashedAt(Instant trashedAt) {
        this.trashedAt = trashedAt;
    }



    // Each note belongs to exactly one registered user.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Note() {
    }


    // UC8: A note can belong to multiple labels.
    @ManyToMany
    @JoinTable(
            name = "note_labels",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<Label> labels = new HashSet<>();

    // Safe collection access for new and existing notes.
    public Set<Label> getLabels() {
        if (labels == null) {
            labels = new HashSet<>();
        }
        return labels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels == null
                ? new HashSet<>()
                : labels;
    }


    // Automatically set timestamps when a note is created.
    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    // Update the modification time before database updates.
    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isTrashed() {
        return trashed;
    }

    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // UC10: Records when the reminder email was sent.
    // NULL means the current reminder has not been sent.
    @Column(name = "reminder_sent_at")
    private Instant reminderSentAt;

    public Instant getReminderSentAt() {
        return reminderSentAt;
    }

    public void setReminderSentAt(Instant reminderSentAt) {
        this.reminderSentAt = reminderSentAt;
    }

}
