package com.fundoonotes.service;

import com.fundoonotes.dto.request.LabelRequestDto;
import com.fundoonotes.dto.response.LabelResponseDto;
import com.fundoonotes.entity.Label;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.LabelRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.impl.LabelServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabelServiceImplTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LabelServiceImpl labelService;

    private Label label;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(3L);

        label = new Label();
        label.setId(1L);
        label.setName("Study");
        label.setUser(user);
    }

    @Test
    void createLabel_ShouldCreateLabel() {

        when(labelRepository
                .existsByUserIdAndNameIgnoreCase(3L, "Study"))
                .thenReturn(false);

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(label.getUser()));

        when(labelRepository.save(any(Label.class)))
                .thenReturn(label);

        LabelResponseDto result = labelService.createLabel(
                3L,
                new LabelRequestDto("Study")
        );

        assertEquals("Study", result.name());
        assertEquals(1L, result.id());

        verify(labelRepository).save(any(Label.class));
    }

    @Test
    void getAllLabels_ShouldReturnUsersLabels() {

        when(labelRepository.findByUserIdOrderByNameAsc(3L))
                .thenReturn(List.of(label));

        List<LabelResponseDto> result =
                labelService.getAllLabels(3L);

        assertEquals(1, result.size());
        assertEquals("Study", result.get(0).name());
    }

    @Test
    void createLabel_ShouldRejectDuplicate() {

        when(labelRepository
                .existsByUserIdAndNameIgnoreCase(3L, "Study"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> labelService.createLabel(
                        3L,
                        new LabelRequestDto("Study")
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );
    }

    @Test
    void renameLabel_ShouldUpdateName() {

        when(labelRepository.findByIdAndUserId(1L, 3L))
                .thenReturn(Optional.of(label));

        when(labelRepository
                .existsByUserIdAndNameIgnoreCase(3L, "Learning"))
                .thenReturn(false);

        when(labelRepository.save(label))
                .thenReturn(label);

        LabelResponseDto result = labelService.renameLabel(
                3L,
                1L,
                new LabelRequestDto("Learning")
        );

        assertEquals("Learning", result.name());
    }

    @Test
    void deleteLabel_ShouldNotDeleteNotes() {

        when(labelRepository.findByIdAndUserId(1L, 3L))
                .thenReturn(Optional.of(label));

        when(noteRepository.findAllNotesByLabel(3L, 1L))
                .thenReturn(List.of());

        labelService.deleteLabel(3L, 1L);

        verify(labelRepository).delete(label);

        // UC8: Deleting labels must not delete notes.
        verify(noteRepository, never())
                .delete(any());
    }

    @Test
    void deleteLabel_ShouldRejectOtherUsersLabel() {

        when(labelRepository.findByIdAndUserId(1L, 4L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> labelService.deleteLabel(4L, 1L)
        );

        verify(labelRepository, never()).delete(any());
    }
}
