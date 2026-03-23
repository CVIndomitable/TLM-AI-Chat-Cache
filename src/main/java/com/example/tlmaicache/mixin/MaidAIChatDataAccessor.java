package com.example.tlmaicache.mixin;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MaidAIChatData.class, remap = false)
public interface MaidAIChatDataAccessor {
    @Accessor("maid")
    EntityMaid tlmcache$getMaid();
}
