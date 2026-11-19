package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.LabelRequestDto;
import com.fundoonotes.dto.response.LabelResponseDto;
import com.fundoonotes.entity.Label;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.LabelRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.LabelService;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public LabelServiceImpl(
            LabelRepository labelRepository,
            NoteRepository noteRepository,
            UserRepository userRepository
    ) {
        this.labelRepository = labelRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }


    @Override
    @CacheEvict(cacheNames = "userLabels", key = "#p0")
    @Transactional
    public LabelResponseDto createLabel(
            Long userId,
            LabelRequestDto request
    ) {
        String name = validateName(request);

        if (labelRepository.existsByUserIdAndNameIgnoreCase(
                userId, name)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Label already exists"
            );
        }

        // Get the authenticated user from the database.
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        Label label = new Label();
        label.setName(name);
        label.setUser(user);

        Label saved = labelRepository.save(label);

        return mapToResponse(saved);
    }


    @Override
    @Cacheable(
            cacheNames = "userLabels",
            key = "#p0"
    )
    @Transactional(readOnly = true)
    public List<LabelResponseDto> getAllLabels(Long userId) {

        return labelRepository
                .findByUserIdOrderByNameAsc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    @CacheEvict(cacheNames = "userLabels", key = "#p0")
    @Transactional
    public LabelResponseDto renameLabel(
            Long userId,
            Long labelId,
            LabelRequestDto request
    ) {
        String newName = validateName(request);

        Label label = getOwnedLabel(userId, labelId);

        // Allow saving the current name without a conflict.
        if (!label.getName().equalsIgnoreCase(newName)
                && labelRepository.existsByUserIdAndNameIgnoreCase(
                userId, newName)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Label already exists"
            );
        }

        label.setName(newName);

        return mapToResponse(labelRepository.save(label));
    }

    @Override
    @CacheEvict(cacheNames = "userLabels", key = "#p0")
    @Transactional
    public void deleteLabel(Long userId, Long labelId) {

        Label label = getOwnedLabel(userId, labelId);

        // Remove join-table links from every note first.
        // This includes notes in Archive and Trash.
        List<Note> notes =
                noteRepository.findAllNotesByLabel(
                        userId, labelId
                );

        for (Note note : notes) {
            note.getLabels().remove(label);
        }

        noteRepository.saveAllAndFlush(notes);

        // Delete the label, not its associated notes.
        labelRepository.delete(label);
    }

    private Label getOwnedLabel(
            Long userId,
            Long labelId
    ) {
        return labelRepository
                .findByIdAndUserId(labelId, userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Label not found"
                        )
                );
    }

    private String validateName(LabelRequestDto request) {

        if (request == null
                || request.name() == null
                || request.name().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Label name cannot be empty"
            );
        }

        String name = request.name().trim();

        if (name.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Label name must be at most 100 characters"
            );
        }

        return name;
    }

    private LabelResponseDto mapToResponse(Label label) {

        return new LabelResponseDto(
                label.getId(),
                label.getName()
        );
    }
}
