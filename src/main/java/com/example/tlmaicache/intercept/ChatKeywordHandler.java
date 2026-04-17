package com.example.tlmaicache.intercept;

import com.example.tlmaicache.TlmAiCache;
import com.example.tlmaicache.cache.ActionCache;
import com.example.tlmaicache.cache.CachedAction;
import com.example.tlmaicache.config.CacheConfig;
import com.example.tlmaicache.normalizer.TextNormalizer;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleManger;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatText;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatTextType;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ServerChatEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class ChatKeywordHandler {

    public static final String FUNC_SWITCH_TASK = "switch_maid_work_task";
    public static final String FUNC_SWITCH_FOLLOW = "switch_maid_follow_state";

    private static final double SEARCH_RADIUS = 32.0;
    private static final ResourceLocation EMPTY_ICON = new ResourceLocation("tlmaicache", "empty");

    private ChatKeywordHandler() {
    }

    public static void handle(ServerChatEvent event) {
        if (!CacheConfig.ENABLE_CACHE.get()) return;

        ServerPlayer player = event.getPlayer();
        String message = event.getMessage();
        if (message == null || message.isBlank()) return;

        String normalized = TextNormalizer.normalize(message);
        if (normalized.isEmpty()) return;

        CachedAction action = ActionCache.getInstance().get(normalized);
        if (action == null) {
            if (CacheConfig.SHOW_CACHE_DEBUG.get()) {
                sendDebug(player, new TranslatableComponent("tlmaicache.debug.cache_miss")
                        .withStyle(ChatFormatting.YELLOW));
            }
            return;
        }

        EntityMaid target = findNearestOwnedMaid(player);
        if (target == null) {
            if (CacheConfig.SHOW_CACHE_DEBUG.get()) {
                sendDebug(player, new TranslatableComponent("tlmaicache.debug.no_maid")
                        .withStyle(ChatFormatting.GRAY));
            }
            return;
        }

        boolean ok = execute(action, target, player);
        if (ok && CacheConfig.SHOW_CACHE_DEBUG.get()) {
            sendDebug(player, new TranslatableComponent("tlmaicache.debug.cache_hit",
                    describe(action)).withStyle(ChatFormatting.GREEN));
        }
    }

    private static boolean execute(CachedAction action, EntityMaid maid, ServerPlayer player) {
        String func = action.getFunctionName();
        String param = action.getParameter();

        if (FUNC_SWITCH_TASK.equals(func)) {
            ResourceLocation taskId = ResourceLocation.tryParse(param);
            if (taskId == null) {
                warn(player, "tlmaicache.error.unknown_task", param);
                return false;
            }
            Optional<IMaidTask> opt = TaskManager.findTask(taskId);
            if (opt.isEmpty()) {
                warn(player, "tlmaicache.error.unknown_task", param);
                return false;
            }
            maid.setTask(opt.get());
            bubble(maid, taskId.getPath());
            return true;
        }

        if (FUNC_SWITCH_FOLLOW.equals(func)) {
            boolean follow = Boolean.parseBoolean(param);
            // 1.18.2 TLM: homeModeEnable=true 代表驻守, false 代表跟随
            maid.setHomeModeEnable(!follow);
            bubble(maid, follow ? "follow" : "stay");
            return true;
        }

        TlmAiCache.LOGGER.warn("Unknown cached function: {}", func);
        return false;
    }

    private static EntityMaid findNearestOwnedMaid(ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(SEARCH_RADIUS);
        List<EntityMaid> maids = player.getLevel().getEntitiesOfClass(EntityMaid.class, box,
                m -> m.isAlive() && m.isOwnedBy(player));
        if (maids.isEmpty()) return null;
        return maids.stream()
                .min(Comparator.comparingDouble(m -> m.distanceToSqr(player)))
                .orElse(null);
    }

    private static void bubble(EntityMaid maid, String text) {
        try {
            ChatText chat = new ChatText(ChatTextType.TEXT, EMPTY_ICON, text);
            ChatBubbleManger.addChatBubble(System.currentTimeMillis(), chat, maid);
        } catch (Throwable t) {
            // 气泡失败不影响主流程
            TlmAiCache.LOGGER.debug("Chat bubble failed", t);
        }
    }

    private static void warn(ServerPlayer player, String key, Object... args) {
        player.sendMessage(new TranslatableComponent(key, args)
                .withStyle(ChatFormatting.RED), Util.NIL_UUID);
    }

    private static void sendDebug(ServerPlayer player, Component msg) {
        player.sendMessage(new TextComponent("[TLM Cache] ")
                .withStyle(ChatFormatting.GRAY).append(msg), Util.NIL_UUID);
    }

    public static String describe(CachedAction action) {
        return describe(action.getFunctionName(), action.getParameter());
    }

    public static String describe(String func, String param) {
        if (FUNC_SWITCH_TASK.equals(func)) {
            ResourceLocation rl = ResourceLocation.tryParse(param);
            return rl != null ? rl.getPath() : param;
        }
        if (FUNC_SWITCH_FOLLOW.equals(func)) {
            return Boolean.parseBoolean(param) ? "follow" : "stay";
        }
        return func + "(" + param + ")";
    }
}
