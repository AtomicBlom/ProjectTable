package com.github.atomicblom.projecttable.events;

import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientPlayerEvents {
    @SubscribeEvent
    public static void onWorldUnloaded(LevelEvent.Unload event) {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ProjectTableManager.INSTANCE::resetRecipesToInitial);
    }
}
