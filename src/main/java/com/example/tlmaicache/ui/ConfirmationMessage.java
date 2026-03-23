package com.example.tlmaicache.ui;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class ConfirmationMessage {

    private ConfirmationMessage() {
    }

    /**
     * 发送确认消息：[女仆名] 我理解为【XX模式】 [✔ 确认] [✘ 不对]
     */
    public static void sendConfirmation(ServerPlayer player, EntityMaid maid, String actionDesc, UUID confirmId) {
        MutableComponent msg = Component.empty();

        // 女仆名
        msg.append(Component.literal("[").withStyle(ChatFormatting.GRAY));
        msg.append(maid.getDisplayName().copy().withStyle(ChatFormatting.AQUA));
        msg.append(Component.literal("] ").withStyle(ChatFormatting.GRAY));

        // 操作描述
        msg.append(Component.translatable("tlmaicache.confirm.understood", actionDesc)
                .withStyle(ChatFormatting.WHITE));
        msg.append(Component.literal(" "));

        // 确认按钮
        msg.append(Component.literal("[✔ ")
                .append(Component.translatable("tlmaicache.confirm.yes"))
                .append(Component.literal("]"))
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/tlmcache confirm " + confirmId))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("tlmaicache.confirm.yes.tooltip")))));

        msg.append(Component.literal(" "));

        // 拒绝按钮
        msg.append(Component.literal("[✘ ")
                .append(Component.translatable("tlmaicache.confirm.no"))
                .append(Component.literal("]"))
                .withStyle(style -> style
                        .withColor(ChatFormatting.RED)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/tlmcache reject " + confirmId))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("tlmaicache.confirm.no.tooltip")))));

        player.sendSystemMessage(msg);
    }

    /**
     * 发送可选任务列表（玩家拒绝后选择正确操作）
     */
    public static void sendTaskList(ServerPlayer player, String normalizedText, String originalMessage) {
        player.sendSystemMessage(Component.translatable("tlmaicache.select.title")
                .withStyle(ChatFormatting.YELLOW));

        // 工作任务列表
        MutableComponent taskLine = Component.empty();
        int count = 0;
        for (IMaidTask task : TaskManager.getTaskIndex()) {
            if (task.isHidden(null)) continue;
            String taskId = task.getUid().toString();
            String taskName = task.getUid().getPath();

            taskLine.append(Component.literal("[" + taskName + "]")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/tlmcache select " + encode(normalizedText) + " " + encode(originalMessage)
                                            + " switch_maid_work_task " + taskId))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal(taskId)))));
            taskLine.append(Component.literal(" "));
            count++;
            if (count % 5 == 0) {
                player.sendSystemMessage(taskLine);
                taskLine = Component.empty();
            }
        }
        if (count % 5 != 0) {
            player.sendSystemMessage(taskLine);
        }

        // 跟随/待命选项
        MutableComponent followLine = Component.empty();
        followLine.append(Component.literal("[follow]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/tlmcache select " + encode(normalizedText) + " " + encode(originalMessage)
                                        + " switch_maid_follow_state true"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("tlmaicache.action.follow")))));
        followLine.append(Component.literal(" "));
        followLine.append(Component.literal("[stay]")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GOLD)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/tlmcache select " + encode(normalizedText) + " " + encode(originalMessage)
                                        + " switch_maid_follow_state false"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("tlmaicache.action.stay")))));
        player.sendSystemMessage(followLine);
    }

    /**
     * 简单编码以便在命令中传递（用 Base64 避免空格/特殊字符问题）
     */
    public static String encode(String text) {
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * 解码
     */
    public static String decode(String encoded) {
        return new String(java.util.Base64.getUrlDecoder().decode(encoded),
                java.nio.charset.StandardCharsets.UTF_8);
    }
}
