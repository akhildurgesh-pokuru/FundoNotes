package com.fundoonotes.controller;

import com.fundoonotes.dto.request.LabelRequestDto;
import com.fundoonotes.dto.response.LabelResponseDto;
import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.service.LabelService;
import com.fundoonotes.service.NoteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
public class LabelController {

    private final LabelService labelService;
    private final NoteService noteService;

    public LabelController(
            LabelService labelService,
            NoteService noteService
    ) {
        this.labelService = labelService;
        this.noteService = noteService;
    }

    // UC8: Create a label.
    @PostMapping
    public ResponseEntity<LabelResponseDto> createLabel(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody LabelRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(labelService.createLabel(
                        userId, request
                ));
    }

    // UC8: Get all labels for the logged-in user.
    @GetMapping
    public ResponseEntity<List<LabelResponseDto>> getLabels(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                labelService.getAllLabels(userId)
        );
    }

    // UC8: Rename a label.
    @PutMapping("/{labelId}")
    public ResponseEntity<LabelResponseDto> renameLabel(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long labelId,
            @Valid @RequestBody LabelRequestDto request
    ) {
        return ResponseEntity.ok(
                labelService.renameLabel(
                        userId, labelId, request
                )
        );
    }

    // UC8: Delete a label without deleting its notes.
    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> deleteLabel(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long labelId
    ) {
        labelService.deleteLabel(userId, labelId);
        return ResponseEntity.noContent().build();
    }

    // UC8: View active notes assigned to a label.
    @GetMapping("/{labelId}/notes")
    public ResponseEntity<List<NoteResponseDto>> getNotesByLabel(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long labelId
    ) {
        return ResponseEntity.ok(
                noteService.getNotesByLabel(userId, labelId)
        );
    }
}
