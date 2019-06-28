package com.github.atomicblom.projecttable.events;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber
public class WorldEvents {
    @SubscribeEvent
    public static void onClientJoinedWorld(PlayerEvent.PlayerLoggedInEvent event) {
        //ProjectTableMod.network.sendTo(new message, event.player);
    }
}
