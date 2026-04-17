package com.example.tlmaicache.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CacheConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_CACHE;
    public static final ModConfigSpec.BooleanValue REQUIRE_CONFIRMATION;
    public static final ModConfigSpec.BooleanValue OP_ONLY_CONFIRM;
    public static final ModConfigSpec.ConfigValue<String> EXTRA_CN_STOPWORDS;
    public static final ModConfigSpec.ConfigValue<String> EXTRA_EN_STOPWORDS;
    public static final ModConfigSpec.BooleanValue SHOW_CACHE_DEBUG;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        ENABLE_CACHE = builder
                .comment("Enable the AI chat cache system")
                .define("enable_cache", true);
        REQUIRE_CONFIRMATION = builder
                .comment("Require player confirmation before caching new LLM mappings")
                .define("require_confirmation", true);
        OP_ONLY_CONFIRM = builder
                .comment("Only OP players can confirm new cache mappings")
                .define("op_only_confirm", false);
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
