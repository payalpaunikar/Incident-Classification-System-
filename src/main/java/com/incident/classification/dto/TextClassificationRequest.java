package com.incident.classification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextClassificationRequest {
    @NotBlank(message = "Text must not be empty")
    private String text;

    private String fileName;
}
