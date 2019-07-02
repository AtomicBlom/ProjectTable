package com.github.atomicblom.projecttable.events;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.github.atomicblom.projecttable.networking.ReplaceProjectTableRecipesPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collection;

@Mod.EventBusSubscriber
public class PlayerEvents {
    @SubscribeEvent
    public static void onPlayerJoinedWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && event.player instanceof EntityPlayerMP) {
            Collection<ProjectTableRecipe> recipes = ProjectTableManager.INSTANCE.getRecipes();
            ProjectTableMod.logger.info("Sending {} recipes list to client", recipes.size());
            ProjectTableMod.network.sendTo(new ReplaceProjectTableRecipesPacket(recipes), (EntityPlayerMP)event.player);
        }
    }

    @SubscribeEvent
    public static void onWorldUnloaded(WorldEvent.Unload event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            ProjectTableManager.INSTANCE.resetRecipesToInitial();
        }
    }
}
