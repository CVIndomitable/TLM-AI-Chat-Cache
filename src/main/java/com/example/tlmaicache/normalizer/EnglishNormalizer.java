package com.example.tlmaicache.normalizer;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class EnglishNormalizer {

    private static final Set<String> STOPWORDS = Set.of(
            "please", "can", "you", "could", "would", "should",
            "the", "a", "an", "my", "me", "i",
            "go", "help", "start", "do", "switch", "to", "mode",
            "task", "job", "work", "begin", "now", "set", "change",
            "put", "into", "make", "her", "it", "and", "or",
            "want", "need", "let", "have", "be", "is", "are",
            "maid", "she", "this", "that"
    );

    private static final Pattern PUNCTUATION = Pattern.compile("[^a-zA-Z0-9\\s]");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private EnglishNormalizer() {
    }

    public static String normalize(String input, String[] extraStopwords) {
        String text = input.toLowerCase(Locale.ROOT);

        // 去除标点
        text = PUNCTUATION.matcher(text).replaceAll("");

        Set<String> allStopwords = new HashSet<>(STOPWORDS);
        for (String extra : extraStopwords) {
            String trimmed = extra.trim().toLowerCase(Locale.ROOT);
            if (!trimmed.isEmpty()) {
                allStopwords.add(trimmed);
            }
        }

        // 分词后去除停用词
        String result = Arrays.stream(MULTI_SPACE.split(text.trim()))
                .filter(word -> !word.isEmpty() && !allStopwords.contains(word))
                .collect(Collectors.joining(" "));

        return result;
    }
}
