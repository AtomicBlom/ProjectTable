package com.github.atomicblom.projecttable.events;

import com.github.atomicblom.projecttable.ProjectTableConfig;
import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.github.atomicblom.projecttable.networking.ReplaceProjectTableRecipesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;

@Mod.EventBusSubscriber
public class ServerPlayerEvents {
    @SubscribeEvent
    public static void onPlayerJoinedWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer playerEntity = (ServerPlayer)event.getEntity();
            Collection<ProjectTableRecipe> recipes = ProjectTableManager.INSTANCE.getRecipes();
            ProjectTableMod.logger.info("Sending {} recipes list to client", recipes.size());
            ProjectTableMod.network.send(PacketDistributor.PLAYER.with(() -> playerEntity), new ReplaceProjectTableRecipesPacket(recipes, ProjectTableConfig.COMMON.loadCraftingTableRecipes.get()));
        }
    }
}