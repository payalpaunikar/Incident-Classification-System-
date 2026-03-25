package com.incident.classification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long topicId;

    @Column(nullable = false,unique = true)
    @NotBlank(message = "Topic title must not be blank")
    private String title;

    // Mandatory Keywords
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "topic_mandatory_keywords",
            joinColumns = @JoinColumn(name = "topic_id")
    )
    @Column(name = "keyword")
    @Builder.Default
    private List<String> mandatoryKeywords= new ArrayList<>();

    // Optional Keywords
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "topic_optional_keywords",
            joinColumns = @JoinColumn(name = "topic_id")
    )
    @Column(name = "keyword")
    @Builder.Default
    private List<String> optionalKeywords = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}
