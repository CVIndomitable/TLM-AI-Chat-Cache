package com.example.tlmaicache.intercept;

import com.example.tlmaicache.TlmAiCache;
import com.example.tlmaicache.cache.ActionCache;
import com.example.tlmaicache.cache.CachedAction;
import com.example.tlmaicache.config.CacheConfig;
import com.example.tlmaicache.ui.ConfirmationMessage;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.TextChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatInterceptor {

    // 女仆 UUID → 待确认的查询
    private static final Map<UUID, PendingQuery> pendingQueries = new ConcurrentHashMap<>();
    // 确认 ID → 待确认信息
    private static final Map<UUID, PendingConfirmation> pendingConfirmations = new ConcurrentHashMap<>();

    private ChatInterceptor() {
    }

    /**
     * 缓存命中时直接执行操作
     */
    public static boolean executeCachedAction(CachedAction action, EntityMaid maid, ServerPlayer sender) {
        String functionName = action.getFunctionName();
        String parameter = action.getParameter();

        boolean success;
        String actionDesc;

        if ("switch_maid_work_task".equals(functionName)) {
            ResourceLocation taskId = new ResourceLocation(parameter);
            Optional<IMaidTask> taskOpt = TaskManager.findTask(taskId);
            if (taskOpt.isPresent()) {
                IMaidTask task = taskOpt.get();
                maid.setTask(task);
                task.onFunctionCallSwitch(maid);
                actionDesc = taskId.getPath();
                success = true;
            } else {
                sender.sendSystemMessage(Component.translatable("tlmaicache.error.unknown_task", parameter)
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        } else if ("switch_maid_follow_state".equals(functionName)) {
            boolean follow = Boolean.parseBoolean(parameter);
            if (follow) {
                maid.restrictTo(BlockPos.ZERO, MaidConfig.MAID_NON_HOME_RANGE.get());
                maid.setHomeModeEnable(false);
                actionDesc = "follow";
            } else {
                maid.getSchedulePos().setHomeModeEnable(maid, maid.blockPosition());
                maid.setHomeModeEnable(true);
                actionDesc = "stay";
            }
            success = true;
        } else {
            TlmAiCache.LOGGER.warn("Unknown cached function: {}", functionName);
            return false;
        }

        if (success) {
            // 显示聊天气泡
            Component bubbleText = Component.translatable("tlmaicache.bubble.ok", actionDesc);
            maid.getChatBubbleManager().addChatBubble(TextChatBubbleData.type2(bubbleText));

            if (CacheConfig.SHOW_CACHE_DEBUG.get()) {
                sender.sendSystemMessage(Component.literal("[TLM Cache] ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("tlmaicache.debug.cache_hit", actionDesc)
                                .withStyle(ChatFormatting.GREEN)));
            }
        }

        return success;
    }

    /**
     * 缓存未命中时记录待查询
     */
    public static void recordPendingQuery(EntityMaid maid, String normalizedText, String originalMessage, ServerPlayer sender) {
        pendingQueries.put(maid.getUUID(), new PendingQuery(
                normalizedText, originalMessage, sender.getUUID(), System.currentTimeMillis()
        ));

        if (CacheConfig.SHOW_CACHE_DEBUG.get()) {
            sender.sendSystemMessage(Component.literal("[TLM Cache] ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("tlmaicache.debug.cache_miss")
                            .withStyle(ChatFormatting.YELLOW)));
        }
    }

    /**
     * LLM 工具调用完成后，检查是否有待确认的查询
     */
    public static void onToolCallCompleted(EntityMaid maid, String toolName, String parameter) {
        PendingQuery query = pendingQueries.remove(maid.getUUID());
        if (query == null) return;

        // 检查是否超时（30秒）
        if (System.currentTimeMillis() - query.timestamp > 30_000) return;

        if (!CacheConfig.REQUIRE_CONFIRMATION.get()) {
            // 不需要确认，直接写入缓存
            ActionCache.getInstance().put(query.normalizedText,
                    new CachedAction(toolName, parameter, query.originalMessage));
            TlmAiCache.LOGGER.debug("Auto-cached: '{}' → {}({})", query.normalizedText, toolName, parameter);
            return;
        }

        // 需要确认，生成确认 ID 并发送消息
        UUID confirmId = UUID.randomUUID();
        PendingConfirmation confirmation = new PendingConfirmation(
                maid.getUUID(), query.normalizedText, query.originalMessage,
                toolName, parameter, query.playerUUID, System.currentTimeMillis()
        );
        pendingConfirmations.put(confirmId, confirmation);

        // 发送确认消息给玩家
        if (maid.getOwner() instanceof ServerPlayer player) {
            String actionDesc = formatActionDescription(toolName, parameter);
            ConfirmationMessage.sendConfirmation(player, maid, actionDesc, confirmId);
        }
    }

    /**
     * 玩家确认缓存映射
     */
    public static boolean confirmMapping(UUID confirmId, ServerPlayer player) {
        PendingConfirmation confirmation = pendingConfirmations.remove(confirmId);
        if (confirmation == null) return false;
        if (!confirmation.playerUUID.equals(player.getUUID())) return false;

        // OP 检查
        if (CacheConfig.OP_ONLY_CONFIRM.get() && !player.hasPermissions(2)) {
            player.sendSystemMessage(Component.translatable("tlmaicache.error.op_only")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        ActionCache.getInstance().put(confirmation.normalizedText,
                new CachedAction(confirmation.toolName, confirmation.parameter, confirmation.originalMessage));

        player.sendSystemMessage(Component.translatable("tlmaicache.confirm.saved",
                        confirmation.normalizedText, formatActionDescription(confirmation.toolName, confirmation.parameter))
                .withStyle(ChatFormatting.GREEN));
        return true;
    }

    /**
     * 玩家拒绝并发送可选操作列表
     */
    public static boolean rejectMapping(UUID confirmId, ServerPlayer player) {
        PendingConfirmation confirmation = pendingConfirmations.remove(confirmId);
        if (confirmation == null) return false;
        if (!confirmation.playerUUID.equals(player.getUUID())) return false;

        // 发送可选操作列表
        ConfirmationMessage.sendTaskList(player, confirmation.normalizedText, confirmation.originalMessage);
        return true;
    }

    /**
     * 玩家手动选择正确操作
     */
    public static void selectAction(ServerPlayer player, String normalizedText, String originalMessage,
                                    String toolName, String parameter) {
        if (CacheConfig.OP_ONLY_CONFIRM.get() && !player.hasPermissions(2)) {
            player.sendSystemMessage(Component.translatable("tlmaicache.error.op_only")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        ActionCache.getInstance().put(normalizedText,
                new CachedAction(toolName, parameter, originalMessage));

        player.sendSystemMessage(Component.translatable("tlmaicache.confirm.saved",
                        normalizedText, formatActionDescription(toolName, parameter))
                .withStyle(ChatFormatting.GREEN));
    }

    public static void clearPending() {
        pendingQueries.clear();
        pendingConfirmations.clear();
    }

    /**
     * 清理过期的待确认项（30秒超时）
     */
    public static void cleanupExpired() {
        long now = System.currentTimeMillis();
        pendingQueries.entrySet().removeIf(e -> now - e.getValue().timestamp > 30_000);
        pendingConfirmations.entrySet().removeIf(e -> now - e.getValue().timestamp > 30_000);
    }

    public static String formatActionDescription(String toolName, String parameter) {
        if ("switch_maid_work_task".equals(toolName)) {
            ResourceLocation rl = ResourceLocation.tryParse(parameter);
            return rl != null ? rl.getPath() : parameter;
        } else if ("switch_maid_follow_state".equals(toolName)) {
            return Boolean.parseBoolean(parameter) ? "follow" : "stay";
        }
        return toolName + "(" + parameter + ")";
    }

    // 内部记录类
    public record PendingQuery(String normalizedText, String originalMessage, UUID playerUUID, long timestamp) {
    }

    public record PendingConfirmation(UUID maidUUID, String normalizedText, String originalMessage,
                                      String toolName, String parameter, UUID playerUUID, long timestamp) {
    }
}
