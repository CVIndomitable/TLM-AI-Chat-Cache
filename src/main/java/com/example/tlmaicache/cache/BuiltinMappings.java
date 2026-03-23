package com.example.tlmaicache.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BuiltinMappings {

    private static final String SWITCH_TASK = "switch_maid_work_task";
    private static final String SWITCH_FOLLOW = "switch_maid_follow_state";
    private static final String TLM = "touhou_little_maid:";

    private BuiltinMappings() {
    }

    public static Map<String, CachedAction> create() {
        Map<String, CachedAction> map = new ConcurrentHashMap<>();

        // ===== 攻击 / Attack =====
        task(map, "攻击", "attack", "攻击");
        task(map, "打怪", "attack", "打怪");
        task(map, "战斗", "attack", "战斗");
        task(map, "保护", "attack", "保护我");
        task(map, "attack", "attack", "attack");
        task(map, "fight", "attack", "fight");
        task(map, "combat", "attack", "combat");

        // ===== 远程攻击 / Ranged Attack =====
        task(map, "射击", "ranged_attack", "射击");
        task(map, "弓箭", "ranged_attack", "弓箭攻击");
        task(map, "远程攻击", "ranged_attack", "远程攻击");
        task(map, "远程", "ranged_attack", "远程攻击");
        task(map, "ranged attack", "ranged_attack", "ranged attack");
        task(map, "shoot", "ranged_attack", "shoot");
        task(map, "bow", "ranged_attack", "bow attack");

        // ===== 弩攻击 / Crossbow Attack =====
        task(map, "弩", "crossbow_attack", "弩攻击");
        task(map, "弩箭", "crossbow_attack", "弩箭攻击");
        task(map, "crossbow", "crossbow_attack", "crossbow attack");

        // ===== 弹幕攻击 / Danmaku Attack =====
        task(map, "弹幕", "danmaku_attack", "弹幕攻击");
        task(map, "弹幕攻击", "danmaku_attack", "弹幕攻击");
        task(map, "danmaku", "danmaku_attack", "danmaku attack");

        // ===== 三叉戟攻击 / Trident Attack =====
        task(map, "三叉戟", "trident_attack", "三叉戟攻击");
        task(map, "trident", "trident_attack", "trident attack");

        // ===== 种植 / Farm =====
        task(map, "种植", "farm", "种植");
        task(map, "种地", "farm", "种地");
        task(map, "种菜", "farm", "种菜");
        task(map, "种田", "farm", "种田");
        task(map, "farming", "farm", "farming");
        task(map, "farm", "farm", "farm");
        task(map, "plant", "farm", "plant");
        task(map, "grow", "farm", "grow");

        // ===== 甘蔗 / Sugar Cane =====
        task(map, "甘蔗", "sugar_cane", "收甘蔗");
        task(map, "sugar cane", "sugar_cane", "sugar cane");
        task(map, "sugarcane", "sugar_cane", "sugarcane");

        // ===== 西瓜 / Melon =====
        task(map, "西瓜", "melon", "收西瓜");
        task(map, "南瓜", "melon", "收南瓜");
        task(map, "melon", "melon", "melon");
        task(map, "pumpkin", "melon", "pumpkin");

        // ===== 可可豆 / Cocoa =====
        task(map, "可可豆", "cocoa", "收可可豆");
        task(map, "可可", "cocoa", "可可");
        task(map, "cocoa", "cocoa", "cocoa");

        // ===== 采蜜 / Honey =====
        task(map, "采蜜", "honey", "采蜜");
        task(map, "蜂蜜", "honey", "采蜂蜜");
        task(map, "honey", "honey", "honey");

        // ===== 割草 / Grass =====
        task(map, "割草", "grass", "割草");
        task(map, "除草", "grass", "除草");
        task(map, "草", "grass", "割草");
        task(map, "grass", "grass", "grass");
        task(map, "mow", "grass", "mow");

        // ===== 雪 / Snow =====
        task(map, "铲雪", "snow", "铲雪");
        task(map, "雪", "snow", "收集雪");
        task(map, "snow", "snow", "snow");

        // ===== 喂食主人 / Feed Owner =====
        task(map, "喂食", "feed", "喂食主人");
        task(map, "喂", "feed", "喂我吃东西");
        task(map, "feed owner", "feed", "feed owner");
        task(map, "feed", "feed", "feed me");

        // ===== 剪羊毛 / Shears =====
        task(map, "剪羊毛", "shears", "剪羊毛");
        task(map, "剪毛", "shears", "剪毛");
        task(map, "shear", "shears", "shear");
        task(map, "shears", "shears", "shears");

        // ===== 挤牛奶 / Milk =====
        task(map, "挤奶", "milk", "挤牛奶");
        task(map, "挤牛奶", "milk", "挤牛奶");
        task(map, "牛奶", "milk", "挤牛奶");
        task(map, "milk", "milk", "milk");

        // ===== 火把 / Torch =====
        task(map, "火把", "torch", "放火把");
        task(map, "插火把", "torch", "插火把");
        task(map, "照明", "torch", "照明");
        task(map, "torch", "torch", "torch");
        task(map, "light", "torch", "light");

        // ===== 喂动物 / Feed Animal =====
        task(map, "喂动物", "feed_animal", "喂动物");
        task(map, "繁殖", "feed_animal", "繁殖动物");
        task(map, "feed animal", "feed_animal", "feed animal");
        task(map, "breed", "feed_animal", "breed");

        // ===== 钓鱼 / Fishing =====
        task(map, "钓鱼", "fishing", "钓鱼");
        task(map, "fish", "fishing", "fish");
        task(map, "fishing", "fishing", "fishing");

        // ===== 灭火 / Extinguishing =====
        task(map, "灭火", "extinguishing", "灭火");
        task(map, "extinguish", "extinguishing", "extinguish");
        task(map, "fire", "extinguishing", "put out fire");

        // ===== 棋盘游戏 / Board Games =====
        task(map, "下棋", "board_games", "下棋");
        task(map, "棋盘", "board_games", "棋盘游戏");
        task(map, "board games", "board_games", "board games");
        task(map, "chess", "board_games", "chess");

        // ===== 空闲 / Idle =====
        task(map, "空闲", "idle", "空闲");
        task(map, "待机", "idle", "待机");
        task(map, "休息", "idle", "休息");
        task(map, "idle", "idle", "idle");
        task(map, "rest", "idle", "rest");

        // ===== 跟随 / Follow =====
        follow(map, "跟随", "true", "跟随");
        follow(map, "跟着", "true", "跟着我");
        follow(map, "过来", "true", "过来");
        follow(map, "跟上", "true", "跟上");
        follow(map, "follow", "true", "follow me");
        follow(map, "come", "true", "come");

        // ===== 待命 / Stay =====
        follow(map, "待命", "false", "待命");
        follow(map, "别动", "false", "别动");
        follow(map, "等着", "false", "等着");
        follow(map, "站着", "false", "站着");
        follow(map, "stay", "false", "stay");
        follow(map, "wait", "false", "wait");
        follow(map, "stop", "false", "stop");

        return map;
    }

    private static void task(Map<String, CachedAction> map, String key, String taskId, String original) {
        map.put(key, new CachedAction(SWITCH_TASK, TLM + taskId, original));
    }

    private static void follow(Map<String, CachedAction> map, String key, String followValue, String original) {
        map.put(key, new CachedAction(SWITCH_FOLLOW, followValue, original));
    }
}
