package com.example.tlmaicache.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CacheConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_CACHE;
    public static final ForgeConfigSpec.ConfigValue<String> EXTRA_CN_STOPWORDS;
    public static final ForgeConfigSpec.ConfigValue<String> EXTRA_EN_STOPWORDS;
    public static final ForgeConfigSpec.BooleanValue SHOW_CACHE_DEBUG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        ENABLE_CACHE = builder
                .comment("Enable keyword → task dispatch on player chat")
                .define("enable_cache", true);
        builder.pop();

        builder.push("normalization");
        EXTRA_CN_STOPWORDS = builder
                .comment("Extra Chinese stopwords, comma separated")
                .define("extra_cn_stopwords", "");
        EXTRA_EN_STOPWORDS = builder
                .comment("Extra English stopwords, comma separated")
                .define("extra_en_stopwords", "");
        builder.pop();

        builder.push("debug");
        SHOW_CACHE_DEBUG = builder
                .comment("Show cache hit/miss debug info in chat")
                .define("show_cache_debug", false);
        builder.pop();

        SPEC = builder.build();
    }
}
