package com.incident.classification.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotEmpty(message = "At least one keyword is required in mandatory field.")
    private List<String> mandatoryKeywords;

    @Builder.Default
    private List<String> optionalKeywords = new ArrayList<>();

}
