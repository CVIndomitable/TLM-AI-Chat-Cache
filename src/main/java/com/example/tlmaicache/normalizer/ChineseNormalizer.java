package com.example.tlmaicache.normalizer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

public final class ChineseNormalizer {

    // 按长度降序排列，优先匹配长词
    private static final List<String> STOPWORDS = List.of(
            "能不能", "可不可以", "帮我", "给我", "可以", "一下",
            "的", "了", "吧", "啊", "呢", "哦", "嘛", "呀", "哈", "嗯",
            "去", "来", "你", "请", "把", "让", "我",
            "模式", "工作", "任务", "开始", "切换", "进行",
            "现在", "快", "赶紧", "试试", "到", "成", "为",
            "吗", "么", "嘞", "哟", "喽", "咯", "啦", "呐"
    );

    private static final Pattern PUNCTUATION = Pattern.compile("[\\p{Punct}\\p{IsPunctuation}\\s　]+");

    private ChineseNormalizer() {
    }

    public static String normalize(String input, String[] extraStopwords) {
        String text = input;

        // 去除标点和空白
        text = PUNCTUATION.matcher(text).replaceAll("");

        // 去除停用词（优先匹配长词）
        List<String> allStopwords = new ArrayList<>(STOPWORDS);
        for (String extra : extraStopwords) {
            String trimmed = extra.trim();
            if (!trimmed.isEmpty()) {
                allStopwords.add(trimmed);
            }
        }
        // 按长度降序排列，确保先匹配长词
        allStopwords.sort(Comparator.comparingInt(String::length).reversed());

        for (String word : allStopwords) {
            text = text.replace(word, "");
        }

        return text;
    }
}
