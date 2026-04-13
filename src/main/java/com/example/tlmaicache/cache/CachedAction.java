package com.example.tlmaicache.cache;

import com.google.gson.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CachedAction {
    private final String functionName;
    private final String parameter;
    private final AtomicInteger hitCount;
    private final AtomicLong lastUsed;
    private final String originalInput;

    public CachedAction(String functionName, String parameter, String originalInput) {
        this.functionName = functionName;
        this.parameter = parameter;
        this.hitCount = new AtomicInteger(0);
        this.lastUsed = new AtomicLong(System.currentTimeMillis());
        this.originalInput = originalInput;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getParameter() {
        return parameter;
    }

    public int getHitCount() {
        return hitCount.get();
    }

    public long getLastUsed() {
        return lastUsed.get();
    }

    public String getOriginalInput() {
        return originalInput;
    }

    public void recordHit() {
        hitCount.incrementAndGet();
        lastUsed.set(System.currentTimeMillis());
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("functionName", functionName);
        obj.addProperty("parameter", parameter);
        obj.addProperty("hitCount", hitCount.get());
        obj.addProperty("lastUsed", lastUsed.get());
        obj.addProperty("originalInput", originalInput);
        return obj;
    }

    public static CachedAction fromJson(JsonObject obj) {
        String fn = obj.get("functionName").getAsString();
        String param = obj.get("parameter").getAsString();
        String original = obj.has("originalInput") ? obj.get("originalInput").getAsString() : "";
        CachedAction action = new CachedAction(fn, param, original);
        if (obj.has("hitCount")) action.hitCount.set(obj.get("hitCount").getAsInt());
        if (obj.has("lastUsed")) action.lastUsed.set(obj.get("lastUsed").getAsLong());
        return action;
    }
}
