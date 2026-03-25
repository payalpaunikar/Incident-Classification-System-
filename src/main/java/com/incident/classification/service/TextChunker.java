package com.incident.classification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TextChunker {

    private static final int MIN_CHUNK_LENGTH = 10;

    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) return List.of();

        String normalized = normalize(text);
        List<String> paragraphs = splitIntoParagraphs(normalized);

        List<String> chunks = new ArrayList<>();
        for (String paragraph : paragraphs) {
            if (paragraph.length() > 500) {
                chunks.addAll(splitIntoSentences(paragraph));
            } else {
                chunks.add(paragraph);
            }
        }

        return chunks.stream()
                .map(String::trim)
                .filter(c -> c.length() >= MIN_CHUNK_LENGTH)
                .collect(Collectors.toList());
    }

    public List<String> splitIntoParagraphs(String text) {
        return Arrays.stream(text.split("\\n{2,}"))
                .map(String::trim)
                .filter(p -> !p.isBlank())
                .collect(Collectors.toList());
    }

    public List<String> splitIntoSentences(String text) {
        BreakIterator iterator = BreakIterator.getSentenceInstance();
        iterator.setText(text);

        List<String> sentences = new ArrayList<>();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; end = iterator.next()) {
            String sentence = text.substring(start, end).trim();
            if (!sentence.isBlank() && sentence.length() >= MIN_CHUNK_LENGTH) {
                sentences.add(sentence);
            }
            start = end;
        }

        if (sentences.size() <= 1) {
            sentences = Arrays.stream(text.split("(?<=[.!?])\\s+(?=[A-Z])"))
                    .map(String::trim)
                    .filter(s -> s.length() >= MIN_CHUNK_LENGTH)
                    .collect(Collectors.toList());
        }
        return sentences;
    }

    private String normalize(String text) {
        return text.replaceAll("\r\n", "\n")
                .replaceAll("\r", "\n")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}