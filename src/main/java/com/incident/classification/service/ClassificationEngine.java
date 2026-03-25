package com.incident.classification.service;


import com.incident.classification.entity.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ClassificationEngine {

    private static final double MANDATORY_EXACT_WEIGHT   = 2.0;
    private static final double MANDATORY_PARTIAL_WEIGHT = 1.0;
    private static final double MANDATORY_STEM_WEIGHT    = 1.2;

    private static final double OPTIONAL_EXACT_WEIGHT    = 1.0;
    private static final double OPTIONAL_PARTIAL_WEIGHT  = 0.4;
    private static final double OPTIONAL_STEM_WEIGHT     = 0.5;

    private static final double MIN_CONFIDENCE_THRESHOLD = 0.1;

    public static final String UNCLASSIFIED = "UNCLASSIFIED";

    public ClassificationResult classify(String textChunk, List<Topic> topics) {
        if (textChunk == null || textChunk.isBlank() || topics == null || topics.isEmpty()) {
            return ClassificationResult.unclassified();
        }

        String normalizedText = textChunk.toLowerCase();
        Set<String> textWords = tokenize(normalizedText);

        Map<Topic, ScoreBreakdown> scoreMap = new LinkedHashMap<>();
        for (Topic topic : topics) {
            scoreMap.put(topic, computeScore(normalizedText, textWords, topic));
        }

        // Must have at least one mandatory keyword hit to qualify
        Optional<Map.Entry<Topic, ScoreBreakdown>> best = scoreMap.entrySet().stream()
                .filter(e -> e.getValue().mandatoryHits > 0)
                .max(Comparator.comparingDouble(e -> e.getValue().totalScore));

        if (best.isEmpty()) return ClassificationResult.unclassified();

        Topic bestTopic = best.get().getKey();
        double confidence = normalizeConfidence(best.get().getValue(), bestTopic);

        if (confidence < MIN_CONFIDENCE_THRESHOLD) return ClassificationResult.unclassified();

        log.debug("Classified as '{}' (conf={}) : {}",
                bestTopic.getTitle(), String.format("%.2f", confidence),
                textChunk.length() > 60 ? textChunk.substring(0, 60) + "..." : textChunk);

        return ClassificationResult.builder()
                .topic(bestTopic)
                .confidence(Math.round(confidence * 100.0) / 100.0)
                .build();
    }

    private ScoreBreakdown computeScore(String text, Set<String> words, Topic topic) {
        ScoreBreakdown b = new ScoreBreakdown();

        List<String> mandatory = topic.getMandatoryKeywords();
        if (mandatory != null) {
            for (String kw : mandatory) {
                String k = kw.toLowerCase().trim();
                if (k.isBlank()) continue;
                if (words.contains(k))             { b.totalScore += MANDATORY_EXACT_WEIGHT;   b.mandatoryHits++; }
                else if (text.contains(k))         { b.totalScore += MANDATORY_PARTIAL_WEIGHT; b.mandatoryHits++; }
                else if (hasStemMatch(words, k))   { b.totalScore += MANDATORY_STEM_WEIGHT;    b.mandatoryHits++; }
            }
        }

        List<String> optional = topic.getOptionalKeywords();
        if (optional != null) {
            for (String kw : optional) {
                String k = kw.toLowerCase().trim();
                if (k.isBlank()) continue;
                if (words.contains(k))             { b.totalScore += OPTIONAL_EXACT_WEIGHT;   b.optionalHits++; }
                else if (text.contains(k))         { b.totalScore += OPTIONAL_PARTIAL_WEIGHT; b.optionalHits++; }
                else if (hasStemMatch(words, k))   { b.totalScore += OPTIONAL_STEM_WEIGHT;    b.optionalHits++; }
            }
        }

        return b;
    }

    private double normalizeConfidence(ScoreBreakdown b, Topic topic) {
        int mc = topic.getMandatoryKeywords() == null ? 0 : topic.getMandatoryKeywords().size();
        int oc = topic.getOptionalKeywords()  == null ? 0 : topic.getOptionalKeywords().size();
        double max = (mc * MANDATORY_EXACT_WEIGHT) + (oc * OPTIONAL_EXACT_WEIGHT);
        return max <= 0 ? 0.0 : Math.min(b.totalScore / max, 1.0);
    }

    private boolean hasStemMatch(Set<String> words, String keyword) {
        String root = stem(keyword);
        if (root.length() < 4) return false;
        return words.stream().anyMatch(w -> stem(w).equals(root));
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.split("[^a-z0-9]+"))
                .filter(w -> !w.isBlank() && w.length() >= 2)
                .collect(Collectors.toSet());
    }

    private String stem(String word) {
        if (word.endsWith("ing")  && word.length() > 6) return word.substring(0, word.length() - 3);
        if (word.endsWith("tion") && word.length() > 6) return word.substring(0, word.length() - 4);
        if (word.endsWith("ed")   && word.length() > 4) return word.substring(0, word.length() - 2);
        if (word.endsWith("er")   && word.length() > 4) return word.substring(0, word.length() - 2);
        if (word.endsWith("ly")   && word.length() > 4) return word.substring(0, word.length() - 2);
        if (word.endsWith("s")    && word.length() > 3) return word.substring(0, word.length() - 1);
        return word;
    }

    private static class ScoreBreakdown {
        double totalScore    = 0.0;
        int    mandatoryHits = 0;
        int    optionalHits  = 0;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ClassificationResult {
        private Topic topic;
        private double confidence;

        public boolean isUnclassified() { return topic == null; }

        public static ClassificationResult unclassified() {
            return ClassificationResult.builder().topic(null).confidence(0.0).build();
        }
    }
}