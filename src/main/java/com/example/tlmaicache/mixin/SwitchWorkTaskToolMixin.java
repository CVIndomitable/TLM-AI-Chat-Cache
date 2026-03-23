package com.example.tlmaicache.mixin;

import com.example.tlmaicache.intercept.ChatInterceptor;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.implement.SwitchWorkTaskFunction;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.ToolResponse;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SwitchWorkTaskFunction.class, remap = false)
public abstract class SwitchWorkTaskToolMixin {

    @Inject(
            method = "onToolCall(Lcom/github/tartaricacid/touhoulittlemaid/ai/service/function/implement/SwitchWorkTaskFunction$Result;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)Lcom/github/tartaricacid/touhoulittlemaid/ai/service/function/response/ToolResponse;",
            at = @At("RETURN")
    )
    private void tlmcache$afterSwitchTask(SwitchWorkTaskFunction.Result result, EntityMaid maid,
                                          CallbackInfoReturnable<ToolResponse> cir) {
        ChatInterceptor.onToolCallCompleted(maid, "switch_maid_work_task", result.id());
    }
}
