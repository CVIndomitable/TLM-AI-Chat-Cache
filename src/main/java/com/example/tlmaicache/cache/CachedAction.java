package com.example.tlmaicache.cache;

import com.google.gson.JsonObject;

public class CachedAction {
    private final String functionName;
    private final String parameter;
    private int hitCount;
    private long lastUsed;
    private final String originalInput;

    public CachedAction(String functionName, String parameter, String originalInput) {
        this.functionName = functionName;
        this.parameter = parameter;
        this.hitCount = 0;
        this.lastUsed = System.currentTimeMillis();
        this.originalInput = originalInput;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getParameter() {
        return parameter;
    }

    public int getHitCount() {
        return hitCount;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public String getOriginalInput() {
        return originalInput;
    }

    public void recordHit() {
        hitCount++;
        lastUsed = System.currentTimeMillis();
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("functionName", functionName);
        obj.addProperty("parameter", parameter);
        obj.addProperty("hitCount", hitCount);
        obj.addProperty("lastUsed", lastUsed);
        obj.addProperty("originalInput", originalInput);
        return obj;
    }

    public static CachedAction fromJson(JsonObject obj) {
        String fn = obj.get("functionName").getAsString();
        String param = obj.get("parameter").getAsString();
        String original = obj.has("originalInput") ? obj.get("originalInput").getAsString() : "";
        CachedAction action = new CachedAction(fn, param, original);
        if (obj.has("hitCount")) action.hitCount = obj.get("hitCount").getAsInt();
        if (obj.has("lastUsed")) action.lastUsed = obj.get("lastUsed").getAsLong();
        return action;
    }
}
