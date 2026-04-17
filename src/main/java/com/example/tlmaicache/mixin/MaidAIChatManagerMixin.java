package com.example.tlmaicache.mixin;

import com.example.tlmaicache.cache.ActionCache;
import com.example.tlmaicache.cache.CachedAction;
import com.example.tlmaicache.config.CacheConfig;
import com.example.tlmaicache.intercept.ChatInterceptor;
import com.example.tlmaicache.normalizer.TextNormalizer;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.ChatClientInfo;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MaidAIChatManager.class, remap = false)
public abstract class MaidAIChatManagerMixin {

    @Inject(method = "chat", at = @At("HEAD"), cancellable = true)
    private void tlmcache$onChat(String message, ChatClientInfo clientInfo, ServerPlayer sender, CallbackInfo ci) {
        if (!CacheConfig.ENABLE_CACHE.get()) return;

        EntityMaid maid = ((MaidAIChatManager) (Object) this).getMaid();

        String normalized = TextNormalizer.normalize(message);
        if (normalized.isEmpty()) return;

        CachedAction cached = ActionCache.getInstance().get(normalized);
        if (cached != null) {
            if (ChatInterceptor.executeCachedAction(cached, maid, sender)) {
                ci.cancel();
                return;
            }
        }

        ChatInterceptor.recordPendingQuery(maid, normalized, message, sender);
    }
}
