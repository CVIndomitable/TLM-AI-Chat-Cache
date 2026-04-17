package com.example.tlmaicache.cache;

import com.example.tlmaicache.normalizer.TextNormalizer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TLM 1.18.2 (release 1.2.2) 内置任务的关键词映射。
 *
 * 1.18.2 版 TLM 没有 LLM Function Calling，本扩展走纯本地关键词派发。
 * 原句在此直接列出，写入时统一经过 TextNormalizer 归一化为缓存 key。
 */
public final class BuiltinMappings {

    private static final String SWITCH_TASK = "switch_maid_work_task";
    private static final String SWITCH_FOLLOW = "switch_maid_follow_state";
    private static final String TLM = "touhou_little_maid:";

    private BuiltinMappings() {
    }

    public static Map<String, CachedAction> create() {
        Map<String, CachedAction> map = new ConcurrentHashMap<>();

        // ===== 空闲 / Idle =====
        task(map, "idle", new String[]{
                "空闲", "待机", "休息", "歇会", "歇会儿", "歇着", "停下", "停工",
                "停止工作", "什么都不做", "啥都别做", "发呆", "闲着", "下班", "打烊",
                "不干活了", "不工作了",
                "idle", "rest", "relax", "stop work", "stop working", "do nothing",
                "break", "pause", "chill", "take a break"
        });

        // ===== 攻击 / Attack =====
        task(map, "attack", new String[]{
                "攻击", "打怪", "战斗", "开打", "出击", "干架", "打架", "保护我", "保护",
                "防御", "御敌", "揍他", "揍它", "打他", "打它", "打敌人", "消灭敌人",
                "清怪", "刷怪", "近战", "砍怪", "灭了他", "灭了它",
                "attack", "fight", "combat", "strike", "protect me", "protect",
                "defend", "slay", "kill them", "melee"
        });

        // ===== 远程攻击（弓） / Ranged Attack =====
        task(map, "ranged_attack", new String[]{
                "远程", "远程攻击", "射击", "射箭", "放箭", "拉弓", "弓箭", "弓",
                "用弓", "用箭",
                "ranged attack", "ranged", "shoot", "bow", "bow attack", "archery", "archer"
        });

        // ===== 弩攻击 / Crossbow Attack =====
        task(map, "crossbow_attack", new String[]{
                "弩", "弩箭", "弩攻击", "用弩", "十字弓", "重弩",
                "crossbow", "crossbow attack"
        });

        // ===== 三叉戟 / Trident Attack =====
        task(map, "trident_attack", new String[]{
                "三叉戟", "叉子", "扔三叉戟", "三叉戟攻击", "投掷三叉戟",
                "trident", "trident attack", "throw trident"
        });

        // ===== 弹幕 / Danmaku Attack =====
        task(map, "danmaku_attack", new String[]{
                "弹幕", "弹幕攻击", "东方弹幕", "放弹幕", "打弹幕", "符卡",
                "danmaku", "bullet hell", "spell card", "touhou attack"
        });

        // ===== 种田 / Farm =====
        task(map, "farm", new String[]{
                "种地", "种田", "种菜", "种植", "种庄稼", "种作物",
                "种小麦", "种麦子", "种胡萝卜", "种土豆", "种马铃薯", "种甜菜",
                "下地", "下田", "务农", "农活", "耕田", "耕地",
                "收麦子", "收小麦", "割麦子", "收胡萝卜", "收土豆",
                "farm", "farming", "plant", "plant crops", "grow", "crops",
                "harvest", "wheat", "carrot", "potato", "beetroot", "cultivate"
        });

        // ===== 甘蔗 / Sugar Cane =====
        task(map, "sugar_cane", new String[]{
                "甘蔗", "收甘蔗", "割甘蔗", "砍甘蔗", "种甘蔗", "收糖", "糖料",
                "sugar cane", "sugarcane", "sugar", "reed", "harvest sugarcane"
        });

        // ===== 西瓜 / Melon（含南瓜） =====
        task(map, "melon", new String[]{
                "西瓜", "收西瓜", "切西瓜", "南瓜", "收南瓜", "切南瓜", "瓜", "收瓜",
                "melon", "watermelon", "pumpkin", "gourd", "harvest melon", "harvest pumpkin"
        });

        // ===== 可可豆 / Cocoa =====
        task(map, "cocoa", new String[]{
                "可可豆", "可可", "收可可", "收可可豆", "巧克力",
                "cocoa", "cocoa bean", "cocoa beans", "chocolate", "harvest cocoa"
        });

        // ===== 采蜜 / Honey =====
        task(map, "honey", new String[]{
                "采蜜", "收蜂蜜", "收蜜", "蜂蜜", "取蜜", "采蜂蜜", "养蜂",
                "honey", "beehive", "collect honey", "harvest honey", "bees"
        });

        // ===== 割草 / Grass =====
        task(map, "grass", new String[]{
                "割草", "除草", "打草", "收草", "踩草", "草",
                "grass", "mow", "cut grass", "trim grass"
        });

        // ===== 铲雪 / Snow =====
        task(map, "snow", new String[]{
                "雪", "铲雪", "收雪", "扫雪", "清雪", "雪块", "收集雪",
                "snow", "shovel snow", "clear snow", "remove snow"
        });

        // ===== 喂主人 / Feed =====
        task(map, "feed", new String[]{
                "喂食", "喂我", "给我吃", "喂主人", "给我食物", "给我东西吃",
                "喂饭", "投喂主人",
                "feed", "feed me", "feed owner", "feed master"
        });

        // ===== 喂动物 / Feed Animal =====
        task(map, "feed_animal", new String[]{
                "喂动物", "喂养动物", "投喂动物", "繁殖", "繁殖动物", "配种",
                "喂牛", "喂羊", "喂猪", "喂鸡",
                "feed animal", "feed animals", "breed", "breeding",
                "breed animal", "breed animals", "feed mob"
        });

        // ===== 钓鱼 / Fishing =====
        task(map, "fishing", new String[]{
                "钓鱼", "钓", "鱼", "下钩", "抛竿", "捕鱼",
                "fish", "fishing", "angle", "cast line", "catch fish"
        });

        // ===== 剪羊毛 / Shears =====
        task(map, "shears", new String[]{
                "剪羊毛", "剪毛", "羊毛", "薅羊毛", "剃毛",
                "shear", "shears", "shearing", "sheep wool", "wool"
        });

        // ===== 挤奶 / Milk =====
        task(map, "milk", new String[]{
                "挤奶", "挤牛奶", "牛奶", "取奶",
                "milk", "milking", "milk cow", "get milk"
        });

        // ===== 火把 / Torch =====
        task(map, "torch", new String[]{
                "火把", "插火把", "放火把", "照明", "点灯", "插灯", "布置火把",
                "torch", "torches", "place torch", "light", "lighting", "illuminate"
        });

        // ===== 灭火 / Extinguishing =====
        task(map, "extinguishing", new String[]{
                "灭火", "救火", "扑火",
                "extinguish", "extinguishing", "put out fire", "firefight"
        });

        // ===== 下棋 / Board Games =====
        task(map, "board_games", new String[]{
                "下棋", "棋", "棋盘", "棋盘游戏", "围棋", "五子棋", "象棋",
                "陪我下棋", "来一盘",
                "chess", "board game", "board games", "go game", "gomoku", "play chess"
        });

        // ===== 跟随 / Follow =====
        follow(map, true, new String[]{
                "跟随", "跟着", "跟我", "跟着我", "跟上", "跟好", "跟紧",
                "过来", "来我身边", "来找我", "过来找我", "靠过来",
                "跟我走", "跟我来", "走吧", "出发", "带上我",
                "follow", "follow me", "come here", "come to me", "heel",
                "go with me", "let's go", "tag along"
        });

        // ===== 待命 / Stay =====
        follow(map, false, new String[]{
                "待命", "别动", "等着", "站着", "站住", "留下",
                "原地待命", "原地", "不要动", "不动", "留在这", "待在这",
                "守着", "守好", "守家", "看家", "站岗",
                "stay", "stay here", "wait", "stop", "do not move", "don't move",
                "hold", "hold position", "stop moving", "guard", "stay put"
        });

        return map;
    }

    private static void task(Map<String, CachedAction> map, String taskId, String[] phrases) {
        CachedAction action = new CachedAction(SWITCH_TASK, TLM + taskId, null);
        for (String phrase : phrases) {
            put(map, phrase, action);
        }
    }

    private static void follow(Map<String, CachedAction> map, boolean follow, String[] phrases) {
        CachedAction action = new CachedAction(SWITCH_FOLLOW, Boolean.toString(follow), null);
        for (String phrase : phrases) {
            put(map, phrase, action);
        }
    }

    private static void put(Map<String, CachedAction> map, String phrase, CachedAction action) {
        String key = TextNormalizer.normalize(phrase);
        if (key.isEmpty()) return;
        // 同一个原句归一化后可能和更早的 key 冲突；保留最早写入的映射
        map.putIfAbsent(key, new CachedAction(action.getFunctionName(), action.getParameter(), phrase));
    }
}
