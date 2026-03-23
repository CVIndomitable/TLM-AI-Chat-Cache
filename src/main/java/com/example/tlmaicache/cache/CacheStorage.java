package com.example.tlmaicache.cache;

import com.example.tlmaicache.TlmAiCache;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CacheStorage {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path CONFIG_DIR = Path.of("config", "tlm-ai-cache");
    private static final Path BUILTIN_FILE = CONFIG_DIR.resolve("builtin.json");
    private static final Path LEARNED_FILE = CONFIG_DIR.resolve("learned.json");

    private CacheStorage() {
    }

    public static Map<String, CachedAction> loadBuiltin() {
        ensureBuiltinFile();
        return loadFromFile(BUILTIN_FILE);
    }

    public static Map<String, CachedAction> loadLearned() {
        return loadFromFile(LEARNED_FILE);
    }

    public static void saveLearned(Map<String, CachedAction> entries) {
        saveToFile(LEARNED_FILE, entries);
    }

    public static void saveBuiltinIfAbsent() {
        ensureBuiltinFile();
    }

    private static void ensureBuiltinFile() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(BUILTIN_FILE)) {
                Map<String, CachedAction> builtins = BuiltinMappings.create();
                saveToFile(BUILTIN_FILE, builtins);
                TlmAiCache.LOGGER.info("Generated builtin cache with {} entries", builtins.size());
            }
        } catch (IOException e) {
            TlmAiCache.LOGGER.error("Failed to create config directory", e);
        }
    }

    private static Map<String, CachedAction> loadFromFile(Path path) {
        Map<String, CachedAction> result = new ConcurrentHashMap<>();
        if (!Files.exists(path)) return result;

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) return result;
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                try {
                    result.put(entry.getKey(), CachedAction.fromJson(entry.getValue().getAsJsonObject()));
                } catch (Exception e) {
                    TlmAiCache.LOGGER.warn("Failed to parse cache entry: {}", entry.getKey(), e);
                }
            }
        } catch (Exception e) {
            TlmAiCache.LOGGER.error("Failed to load cache from {}", path, e);
        }
        return result;
    }

    private static void saveToFile(Path path, Map<String, CachedAction> entries) {
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            entries.forEach((key, action) -> root.add(key, action.toJson()));
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            TlmAiCache.LOGGER.error("Failed to save cache to {}", path, e);
        }
    }

    public static Path getLearnedPath() {
        return LEARNED_FILE;
    }

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }
}
