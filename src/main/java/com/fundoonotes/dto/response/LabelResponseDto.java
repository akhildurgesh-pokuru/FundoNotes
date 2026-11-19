
package com.fundoonotes.dto.response;

import java.io.Serializable;

// UC11: Serializable allows Spring Redis Cache's default
// JDK serializer to store this DTO.
public record LabelResponseDto(
        Long id,
        String name
) implements Serializable {
}
