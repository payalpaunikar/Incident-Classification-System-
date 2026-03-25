package com.incident.classification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classified_texts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassifiedText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long textId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "text_chunk", columnDefinition = "TEXT", nullable = false)
    private String textChunk;

    //Null when UNCLASSIFIED
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_topic_id")
    private Topic assignedTopic;

    //Score between 0.0 and 1.0.
    //0.0 = unclassified / no match.
    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "chunk_index")
    private Integer chunkIndex;

}
