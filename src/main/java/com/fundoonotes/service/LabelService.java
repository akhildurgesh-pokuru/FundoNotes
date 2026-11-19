package com.fundoonotes.service;

import com.fundoonotes.dto.request.LabelRequestDto;
import com.fundoonotes.dto.response.LabelResponseDto;

import java.util.List;

public interface LabelService {

    LabelResponseDto createLabel(
            Long userId,
            LabelRequestDto request
    );

    List<LabelResponseDto> getAllLabels(Long userId);

    LabelResponseDto renameLabel(
            Long userId,
            Long labelId,
            LabelRequestDto request
    );

    void deleteLabel(
            Long userId,
            Long labelId
    );
}
