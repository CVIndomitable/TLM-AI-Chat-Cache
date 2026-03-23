package com.example.tlmaicache.normalizer;

import com.example.tlmaicache.config.CacheConfig;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalize(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return "";

        // 判断是否包含中文字符
        boolean hasChinese = trimmed.codePoints().anyMatch(cp ->
                Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN);

        String result;
        if (hasChinese) {
            result = ChineseNormalizer.normalize(trimmed, getExtraStopwords(CacheConfig.EXTRA_CN_STOPWORDS.get()));
        } else {
            result = EnglishNormalizer.normalize(trimmed, getExtraStopwords(CacheConfig.EXTRA_EN_STOPWORDS.get()));
        }

        return result.trim();
    }

    private static String[] getExtraStopwords(String config) {
        if (config == null || config.isBlank()) return new String[0];
        return config.split(",");
    }
}
