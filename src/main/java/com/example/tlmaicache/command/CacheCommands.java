package com.example.tlmaicache.command;

import com.example.tlmaicache.cache.ActionCache;
import com.example.tlmaicache.cache.CachedAction;
import com.example.tlmaicache.cache.CacheStorage;
import com.example.tlmaicache.intercept.ChatKeywordHandler;
import com.example.tlmaicache.normalizer.TextNormalizer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class CacheCommands {

    private static final int PAGE_SIZE = 10;

    private CacheCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tlmcache")
                .then(Commands.literal("list")
                        .executes(ctx -> listEntries(ctx, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> listEntries(ctx, IntegerArgumentType.getInteger(ctx, "page")))))
                .then(Commands.literal("clear")
                        .requires(src -> src.hasPermission(2))
                        .executes(CacheCommands::clearLearned))
                .then(Commands.literal("remove")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("key", StringArgumentType.greedyString())
                                .executes(CacheCommands::removeEntry)))
                .then(Commands.literal("reload")
                        .requires(src -> src.hasPermission(2))
                        .executes(CacheCommands::reload))
                .then(Commands.literal("stats")
                        .executes(CacheCommands::showStats))
                .then(Commands.literal("export")
                        .requires(src -> src.hasPermission(2))
                        .executes(CacheCommands::exportCache))
                .then(Commands.literal("import")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("file", StringArgumentType.greedyString())
                                .executes(CacheCommands::importCache)))
                .then(Commands.literal("add")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("task")
                                .then(Commands.argument("taskId", StringArgumentType.string())
                                        .then(Commands.argument("phrase", StringArgumentType.greedyString())
                                                .executes(CacheCommands::addTaskMapping))))
                        .then(Commands.literal("follow")
                                .then(Commands.argument("phrase", StringArgumentType.greedyString())
                                        .executes(ctx -> addFollowMapping(ctx, true))))
                        .then(Commands.literal("stay")
                                .then(Commands.argument("phrase", StringArgumentType.greedyString())
                                        .executes(ctx -> addFollowMapping(ctx, false)))))
        );
    }

    private static int listEntries(CommandContext<CommandSourceStack> ctx, int page) {
        Map<String, CachedAction> entries = ActionCache.getInstance().getAllEntries();
        CommandSourceStack src = ctx.getSource();
        if (entries.isEmpty()) {
            src.sendSuccess(new TranslatableComponent("tlmaicache.list.empty")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        List<Map.Entry<String, CachedAction>> sorted = new ArrayList<>(entries.entrySet());
        sorted.sort(Comparator.comparingInt((Map.Entry<String, CachedAction> e) -> e.getValue().getHitCount()).reversed());

        int totalPages = (sorted.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        page = Math.min(page, totalPages);
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, sorted.size());

        src.sendSuccess(new TranslatableComponent("tlmaicache.list.header",
                page, totalPages, sorted.size()).withStyle(ChatFormatting.GOLD), false);

        for (int i = start; i < end; i++) {
            Map.Entry<String, CachedAction> entry = sorted.get(i);
            CachedAction action = entry.getValue();
            String desc = ChatKeywordHandler.describe(action);
            src.sendSuccess(new TextComponent("  ")
                    .append(new TextComponent(entry.getKey()).withStyle(ChatFormatting.AQUA))
                    .append(new TextComponent(" → ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(desc).withStyle(ChatFormatting.WHITE))
                    .append(new TextComponent(" (×" + action.getHitCount() + ")").withStyle(ChatFormatting.DARK_GRAY)), false);
        }

        return sorted.size();
    }

    private static int clearLearned(CommandContext<CommandSourceStack> ctx) {
        int count = ActionCache.getInstance().learnedSize();
        ActionCache.getInstance().clearLearned();
        ctx.getSource().sendSuccess(new TranslatableComponent("tlmaicache.clear.done", count)
                .withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int removeEntry(CommandContext<CommandSourceStack> ctx) {
        String key = StringArgumentType.getString(ctx, "key");
        boolean removed = ActionCache.getInstance().remove(key);
        if (removed) {
            ctx.getSource().sendSuccess(new TranslatableComponent("tlmaicache.remove.done", key)
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            ctx.getSource().sendFailure(new TranslatableComponent("tlmaicache.remove.not_found", key));
        }
        return removed ? 1 : 0;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ActionCache.getInstance().reload();
        ctx.getSource().sendSuccess(new TranslatableComponent("tlmaicache.reload.done",
                ActionCache.getInstance().size()).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int showStats(CommandContext<CommandSourceStack> ctx) {
        ActionCache cache = ActionCache.getInstance();
        int total = cache.size();
        int learned = cache.learnedSize();
        int builtin = total - learned;
        int hits = cache.getHits();
        int misses = cache.getMisses();
        int totalQueries = hits + misses;
        double hitRate = totalQueries > 0 ? (hits * 100.0 / totalQueries) : 0;

        CommandSourceStack src = ctx.getSource();
        src.sendSuccess(new TranslatableComponent("tlmaicache.stats.header")
                .withStyle(ChatFormatting.GOLD), false);
        src.sendSuccess(new TranslatableComponent("tlmaicache.stats.total", total, builtin, learned)
                .withStyle(ChatFormatting.WHITE), false);
        src.sendSuccess(new TranslatableComponent("tlmaicache.stats.hits", hits, misses,
                String.format("%.1f%%", hitRate)).withStyle(ChatFormatting.WHITE), false);

        List<Map.Entry<String, CachedAction>> top = cache.getAllEntries().entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, CachedAction> e) -> e.getValue().getHitCount()).reversed())
                .limit(5)
                .toList();

        if (!top.isEmpty() && top.get(0).getValue().getHitCount() > 0) {
            src.sendSuccess(new TranslatableComponent("tlmaicache.stats.top")
                    .withStyle(ChatFormatting.GOLD), false);
            for (Map.Entry<String, CachedAction> entry : top) {
                CachedAction action = entry.getValue();
                if (action.getHitCount() == 0) continue;
                String desc = ChatKeywordHandler.describe(action);
                src.sendSuccess(new TextComponent("  " + entry.getKey() + " → " + desc
                        + " (×" + action.getHitCount() + ")").withStyle(ChatFormatting.WHITE), false);
            }
        }

        return 1;
    }

    private static int exportCache(CommandContext<CommandSourceStack> ctx) {
        Map<String, CachedAction> learned = ActionCache.getInstance().getLearnedEntries();
        if (learned.isEmpty()) {
            ctx.getSource().sendSuccess(new TranslatableComponent("tlmaicache.export.empty")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }
        Path exportPath = CacheStorage.exportLearned(learned);
        ctx.getSource().sendSuccess(new TranslatableComponent("tlmaicache.export.done",
                learned.size(), exportPath.toAbsolutePath().toString())
                .withStyle(ChatFormatting.GREEN), false);
        return learned.size();
    }

    private static int importCache(CommandContext<CommandSourceStack> ctx) {
        String file = StringArgumentType.getString(ctx, "file");
        Path configDir = CacheStorage.getConfigDir().normalize();
        Path path = configDir.resolve(file).normalize();
        if (!path.startsWith(configDir)) {
            ctx.getSource().sendFailure(new TranslatableComponent("tlmaicache.import.invalid_path"));
            return 0;
        }
        if (!Files.exists(path)) {
            ctx.getSource().sendFailure(new TranslatableComponent("tlmaicache.import.not_found", file));
            return 0;
        }

        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            com.google.gson.JsonObject root = new com.google.gson.Gson().fromJson(reader, com.google.gson.JsonObject.class);
            int count = 0;
            for (var entry : root.entrySet()) {
                try {
                    CachedAction action = CachedAction.fromJson(entry.getValue().getAsJsonObject());
                    ActionCache.getInstance().put(entry.getKey(), action);
                    count++;
                } catch (Exception ignored) {
                }
            }
            ctx.getSource().sendSuccess(new TranslatableComponent("tlmaicache.import.done", count)
                    .withStyle(ChatFormatting.GREEN), true);
            return count;
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TranslatableComponent("tlmaicache.import.error", e.getMessage()));
            return 0;
        }
    }

    private static int addTaskMapping(CommandContext<CommandSourceStack> ctx) {
        String taskId = StringArgumentType.getString(ctx, "taskId");
        String phrase = StringArgumentType.getString(ctx, "phrase");

        String param = taskId.contains(":") ? taskId : "touhou_little_maid:" + taskId;
        if (ResourceLocation.tryParse(param) == null) {
            ctx.getSource().sendFailure(new TranslatableComponent("tlmaicache.error.unknown_task", taskId));
            return 0;
        }

        String key = TextNormalizer.normalize(phrase);
        if (key.isEmpty()) {
            ctx.getSource().sendFailure(new TranslatableComponent("tlmaicache.add.empty_key"));
            return 0;
        }

        ActionCache.getInstance().put(key, new CachedAction(
                ChatKeywordHandler.FUNC_SWITCH_TASK, param, phrase));
        ctx.getSource().sendSuccess(new TranslatableComponent("tlmaicache.add.done", key,
                ChatKeywordHandler.describe(ChatKeywordHandler.FUNC_SWITCH_TASK, param))
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int addFollowMapping(CommandContext<CommandSourceStack> ctx, boolean follow) {
        String phrase = StringArgumentType.getString(ctx, "phrase");
        String key = TextNormalizer.normalize(phrase);
        if (key.isEmpty()) {
            ctx.getSource().sendFailure(new TranslatableComponent("tlmaicache.add.empty_key"));
            return 0;
        }
        String param = Boolean.toString(follow);
        ActionCache.getInstance().put(key, new CachedAction(
                ChatKeywordHandler.FUNC_SWITCH_FOLLOW, param, phrase));
        ctx.getSource().sendSuccess(new TranslatableComponent("tlmaicache.add.done", key,
                ChatKeywordHandler.describe(ChatKeywordHandler.FUNC_SWITCH_FOLLOW, param))
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }
}
