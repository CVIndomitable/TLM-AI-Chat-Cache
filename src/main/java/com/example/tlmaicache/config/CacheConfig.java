package com.example.tlmaicache.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CacheConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_CACHE;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_CONFIRMATION;
    public static final ForgeConfigSpec.BooleanValue SHARED_CACHE;
    public static final ForgeConfigSpec.BooleanValue OP_ONLY_CONFIRM;
    public static final ForgeConfigSpec.ConfigValue<String> EXTRA_CN_STOPWORDS;
    public static final ForgeConfigSpec.ConfigValue<String> EXTRA_EN_STOPWORDS;
    public static final ForgeConfigSpec.BooleanValue SHOW_CACHE_DEBUG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        ENABLE_CACHE = builder
                .comment("Enable the AI chat cache system")
                .define("enable_cache", true);
        REQUIRE_CONFIRMATION = builder
                .comment("Require player confirmation before caching new LLM mappings")
                .define("require_confirmation", true);
        SHARED_CACHE = builder
                .comment("Share learned cache across all players on the server")
                .define("shared_cache", true);
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
