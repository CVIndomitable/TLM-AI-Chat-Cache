package com.example.tlmaicache.cache;

import com.example.tlmaicache.TlmAiCache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionCache {
    private static final ActionCache INSTANCE = new ActionCache();

    private final Map<String, CachedAction> cache = new ConcurrentHashMap<>();
    private final Map<String, CachedAction> learnedOnly = new ConcurrentHashMap<>();
    private final AtomicInteger hits = new AtomicInteger(0);
    private final AtomicInteger misses = new AtomicInteger(0);

    private ActionCache() {
    }

    public static ActionCache getInstance() {
        return INSTANCE;
    }

    public void load() {
        cache.clear();
        learnedOnly.clear();

        // 先加载 builtin
        Map<String, CachedAction> builtins = CacheStorage.loadBuiltin();
        cache.putAll(builtins);

        // 再加载 learned（优先级更高，可覆盖 builtin）
        Map<String, CachedAction> learned = CacheStorage.loadLearned();
        cache.putAll(learned);
        learnedOnly.putAll(learned);

        TlmAiCache.LOGGER.info("Loaded {} builtin + {} learned = {} total cache entries",
                builtins.size(), learned.size(), cache.size());
    }

    public CachedAction get(String normalizedKey) {
        CachedAction action = cache.get(normalizedKey);
        if (action != null) {
            action.recordHit();
            hits.incrementAndGet();
        } else {
            misses.incrementAndGet();
        }
        return action;
    }

    public CachedAction getWithoutStats(String normalizedKey) {
        return cache.get(normalizedKey);
    }

    public void put(String normalizedKey, CachedAction action) {
        cache.put(normalizedKey, action);
        learnedOnly.put(normalizedKey, action);
        saveLearnedAsync();
    }

    public boolean remove(String normalizedKey) {
        CachedAction removed = cache.remove(normalizedKey);
        learnedOnly.remove(normalizedKey);
        if (removed != null) {
            saveLearnedAsync();
            return true;
        }
        return false;
    }

    public void clearLearned() {
        learnedOnly.keySet().forEach(cache::remove);
        learnedOnly.clear();
        saveLearnedAsync();
    }

    public void saveLearned() {
        CacheStorage.saveLearned(learnedOnly);
    }

    private void saveLearnedAsync() {
        Thread t = new Thread(this::saveLearned, "tlmcache-save");
        t.setDaemon(true);
        t.start();
    }

    public int size() {
        return cache.size();
    }

    public int learnedSize() {
        return learnedOnly.size();
    }

    public int getHits() {
        return hits.get();
    }

    public int getMisses() {
        return misses.get();
    }

    public Map<String, CachedAction> getAllEntries() {
        return Collections.unmodifiableMap(cache);
    }

    public Map<String, CachedAction> getLearnedEntries() {
        return Collections.unmodifiableMap(learnedOnly);
    }

    public void reload() {
        hits.set(0);
        misses.set(0);
        load();
    }
}
