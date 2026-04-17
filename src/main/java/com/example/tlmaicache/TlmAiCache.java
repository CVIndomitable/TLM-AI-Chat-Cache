package com.example.tlmaicache;

import com.example.tlmaicache.cache.ActionCache;
import com.example.tlmaicache.command.CacheCommands;
import com.example.tlmaicache.intercept.ChatInterceptor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TlmAiCache.MOD_ID)
public class TlmAiCache {
    public static final String MOD_ID = "tlmaicache";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private int tickCounter = 0;

    public TlmAiCache(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON,
                com.example.tlmaicache.config.CacheConfig.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ActionCache.getInstance().load();
        LOGGER.info("TLM AI Cache loaded {} entries", ActionCache.getInstance().size());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        ActionCache.getInstance().shutdown();
        ChatInterceptor.clearPending();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter >= 600) {
            tickCounter = 0;
            ChatInterceptor.cleanupExpired();
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CacheCommands.register(event.getDispatcher());
    }
}
