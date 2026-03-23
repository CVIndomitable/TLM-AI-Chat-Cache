package com.example.tlmaicache;

import com.example.tlmaicache.cache.ActionCache;
import com.example.tlmaicache.command.CacheCommands;
import com.example.tlmaicache.intercept.ChatInterceptor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TlmAiCache.MOD_ID)
public class TlmAiCache {
    public static final String MOD_ID = "tlmaicache";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public TlmAiCache() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,
                com.example.tlmaicache.config.CacheConfig.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ActionCache.getInstance().load();
        LOGGER.info("TLM AI Cache loaded {} entries", ActionCache.getInstance().size());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        ActionCache.getInstance().saveLearned();
        ChatInterceptor.clearPending();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CacheCommands.register(event.getDispatcher());
    }
}
