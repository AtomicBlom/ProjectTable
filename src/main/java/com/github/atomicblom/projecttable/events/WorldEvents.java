package com.github.atomicblom.projecttable.events;

import com.github.atomicblom.projecttable.ProjectTableMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@SubscribeEvent
public class WorldEvents {
    public static void onClientJoinedWorld(PlayerEvent.PlayerLoggedInEvent event) {
        ProjectTableMod.network.sendTo(new message, event.player);
    }
}
