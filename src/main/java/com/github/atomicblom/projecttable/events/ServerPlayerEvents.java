package com.github.atomicblom.projecttable.events;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.github.atomicblom.projecttable.networking.ReplaceProjectTableRecipesPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Collection;

@Mod.EventBusSubscriber
public class ServerPlayerEvents {
    @SubscribeEvent
    public static void onPlayerJoinedWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerEntity = (ServerPlayerEntity)event.getPlayer();
            Collection<ProjectTableRecipe> recipes = ProjectTableManager.INSTANCE.getRecipes();
            ProjectTableMod.logger.info("Sending {} recipes list to client", recipes.size());
            ProjectTableMod.network.send(PacketDistributor.PLAYER.with(() -> playerEntity), new ReplaceProjectTableRecipesPacket(recipes));
        }
    }
}