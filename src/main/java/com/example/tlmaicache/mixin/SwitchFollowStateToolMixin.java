package com.example.tlmaicache.mixin;

import com.example.tlmaicache.intercept.ChatInterceptor;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.implement.SwitchFollowStateFunction;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.ToolResponse;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SwitchFollowStateFunction.class, remap = false)
public abstract class SwitchFollowStateToolMixin {

    @Inject(
            method = "onToolCall(Lcom/github/tartaricacid/touhoulittlemaid/ai/service/function/implement/SwitchFollowStateFunction$Result;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)Lcom/github/tartaricacid/touhoulittlemaid/ai/service/function/response/ToolResponse;",
            at = @At("RETURN")
    )
    private void tlmcache$afterSwitchFollow(SwitchFollowStateFunction.Result result, EntityMaid maid,
                                            CallbackInfoReturnable<ToolResponse> cir) {
        ChatInterceptor.onToolCallCompleted(maid, "switch_maid_follow_state", String.valueOf(result.follow()));
    }
}
