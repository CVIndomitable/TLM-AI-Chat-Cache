package com.example.tlmaicache.mixin;

import com.example.tlmaicache.intercept.ChatInterceptor;
import com.github.tartaricacid.touhoulittlemaid.ai.agent.tool.implement.SwitchWorkTaskTool;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SwitchWorkTaskTool.class, remap = false)
public abstract class SwitchWorkTaskToolMixin {

    @Inject(
            method = "onCall(Ljava/lang/String;Lcom/github/tartaricacid/touhoulittlemaid/ai/agent/tool/implement/SwitchWorkTaskTool$Result;Lcom/github/tartaricacid/touhoulittlemaid/ai/manager/entity/LLMCallback;)Lcom/github/tartaricacid/touhoulittlemaid/ai/manager/entity/LLMCallback;",
            at = @At("RETURN")
    )
    private void tlmcache$afterSwitchTask(String toolCallId, SwitchWorkTaskTool.Result result, LLMCallback callback,
                                          CallbackInfoReturnable<LLMCallback> cir) {
        if (callback == null || result == null || result.id() == null) return;
        ChatInterceptor.onToolCallCompleted(callback.getMaid(),
                "switch_maid_work_task", result.id().toString());
    }
}
