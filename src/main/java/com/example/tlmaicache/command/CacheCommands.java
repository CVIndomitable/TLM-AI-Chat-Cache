package com.example.tlmaicache.command;

import com.example.tlmaicache.cache.ActionCache;
import com.example.tlmaicache.cache.CachedAction;
import com.example.tlmaicache.cache.CacheStorage;
import com.example.tlmaicache.intercept.ChatInterceptor;
import com.example.tlmaicache.ui.ConfirmationMessage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

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
                // 内部命令（用于聊天确认 UI）
                .then(Commands.literal("confirm")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(CacheCommands::confirmAction)))
                .then(Commands.literal("reject")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(CacheCommands::rejectAction)))
                .then(Commands.literal("select")
                        .then(Commands.argument("normalizedB64", StringArgumentType.string())
                                .then(Commands.argument("originalB64", StringArgumentType.string())
                                        .then(Commands.argument("toolName", StringArgumentType.string())
                                                .then(Commands.argument("parameter", StringArgumentType.greedyString())
                                                        .executes(CacheCommands::selectAction))))))
        );
    }

    private static int listEntries(CommandContext<CommandSourceStack> ctx, int page) {
        Map<String, CachedAction> entries = ActionCache.getInstance().getAllEntries();
        if (entries.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.list.empty")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        List<Map.Entry<String, CachedAction>> sorted = new ArrayList<>(entries.entrySet());
        sorted.sort(Comparator.comparingInt((Map.Entry<String, CachedAction> e) -> e.getValue().getHitCount()).reversed());

        int totalPages = (sorted.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        page = Math.min(page, totalPages);
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, sorted.size());

        final int finalPage = page;
        ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.list.header",
                finalPage, totalPages, sorted.size()).withStyle(ChatFormatting.GOLD), false);

        for (int i = start; i < end; i++) {
            Map.Entry<String, CachedAction> entry = sorted.get(i);
            CachedAction action = entry.getValue();
            String desc = ChatInterceptor.formatActionDescription(action.getFunctionName(), action.getParameter());
            ctx.getSource().sendSuccess(() -> Component.literal("  ")
                    .append(Component.literal(entry.getKey()).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" → ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(desc).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" (×" + action.getHitCount() + ")").withStyle(ChatFormatting.DARK_GRAY)), false);
        }

        return sorted.size();
    }

    private static int clearLearned(CommandContext<CommandSourceStack> ctx) {
        int count = ActionCache.getInstance().learnedSize();
        ActionCache.getInstance().clearLearned();
        ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.clear.done", count)
                .withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int removeEntry(CommandContext<CommandSourceStack> ctx) {
        String key = StringArgumentType.getString(ctx, "key");
        boolean removed = ActionCache.getInstance().remove(key);
        if (removed) {
            ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.remove.done", key)
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            ctx.getSource().sendFailure(Component.translatable("tlmaicache.remove.not_found", key));
        }
        return removed ? 1 : 0;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ActionCache.getInstance().reload();
        ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.reload.done",
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

        ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.stats.header")
                .withStyle(ChatFormatting.GOLD), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.stats.total", total, builtin, learned)
                .withStyle(ChatFormatting.WHITE), false);
        ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.stats.hits", hits, misses,
                String.format("%.1f%%", hitRate)).withStyle(ChatFormatting.WHITE), false);

        // 最常用的映射
        List<Map.Entry<String, CachedAction>> top = cache.getAllEntries().entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, CachedAction> e) -> e.getValue().getHitCount()).reversed())
                .limit(5)
                .toList();

        if (!top.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.stats.top")
                    .withStyle(ChatFormatting.GOLD), false);
            for (Map.Entry<String, CachedAction> entry : top) {
                CachedAction action = entry.getValue();
                if (action.getHitCount() == 0) continue;
                String desc = ChatInterceptor.formatActionDescription(action.getFunctionName(), action.getParameter());
                ctx.getSource().sendSuccess(() -> Component.literal("  " + entry.getKey() + " → " + desc
                        + " (×" + action.getHitCount() + ")").withStyle(ChatFormatting.WHITE), false);
            }
        }

        return 1;
    }

    private static int exportCache(CommandContext<CommandSourceStack> ctx) {
        Map<String, CachedAction> learned = ActionCache.getInstance().getLearnedEntries();
        if (learned.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.export.empty")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }
        // saveLearned 已经保存到 learned.json
        ActionCache.getInstance().saveLearned();
        ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.export.done",
                CacheStorage.getLearnedPath().toAbsolutePath().toString(), learned.size())
                .withStyle(ChatFormatting.GREEN), false);
        return learned.size();
    }

    private static int importCache(CommandContext<CommandSourceStack> ctx) {
        String file = StringArgumentType.getString(ctx, "file");
        java.nio.file.Path path = CacheStorage.getConfigDir().resolve(file);
        if (!java.nio.file.Files.exists(path)) {
            ctx.getSource().sendFailure(Component.translatable("tlmaicache.import.not_found", file));
            return 0;
        }

        try (java.io.Reader reader = java.nio.file.Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
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
            final int finalCount = count;
            ctx.getSource().sendSuccess(() -> Component.translatable("tlmaicache.import.done", finalCount)
                    .withStyle(ChatFormatting.GREEN), true);
            return count;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("tlmaicache.import.error", e.getMessage()));
            return 0;
        }
    }

    // ===== 内部确认命令 =====

    private static int confirmAction(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
        String idStr = StringArgumentType.getString(ctx, "id");
        try {
            UUID confirmId = UUID.fromString(idStr);
            boolean ok = ChatInterceptor.confirmMapping(confirmId, player);
            if (!ok) {
                player.sendSystemMessage(Component.translatable("tlmaicache.confirm.expired")
                        .withStyle(ChatFormatting.GRAY));
            }
            return ok ? 1 : 0;
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    private static int rejectAction(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
        String idStr = StringArgumentType.getString(ctx, "id");
        try {
            UUID confirmId = UUID.fromString(idStr);
            boolean ok = ChatInterceptor.rejectMapping(confirmId, player);
            if (!ok) {
                player.sendSystemMessage(Component.translatable("tlmaicache.confirm.expired")
                        .withStyle(ChatFormatting.GRAY));
            }
            return ok ? 1 : 0;
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    private static int selectAction(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return 0;
        String normalizedB64 = StringArgumentType.getString(ctx, "normalizedB64");
        String originalB64 = StringArgumentType.getString(ctx, "originalB64");
        String toolName = StringArgumentType.getString(ctx, "toolName");
        String parameter = StringArgumentType.getString(ctx, "parameter");

        try {
            String normalized = ConfirmationMessage.decode(normalizedB64);
            String original = ConfirmationMessage.decode(originalB64);
            ChatInterceptor.selectAction(player, normalized, original, toolName, parameter);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
