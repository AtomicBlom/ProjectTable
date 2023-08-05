package com.github.atomicblom.projecttable.networking;

import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ProjectTableCraftPacket
{
    private final ProjectTableRecipe recipe;

    public ProjectTableCraftPacket(ProjectTableRecipe recipe)
    {
        this.recipe = recipe;
    }

    public ProjectTableRecipe getRecipe() {
        return recipe;
    }

    public void serialize(ByteBuf buf)
    {
        recipe.writeToBuffer(new FriendlyByteBuf(buf));
    }

    public static ProjectTableCraftPacket deserialize(ByteBuf buf)
    {
        return new ProjectTableCraftPacket(ProjectTableRecipe.readFromBuffer(new FriendlyByteBuf(buf)));
    }

    public static void received(final ProjectTableCraftPacket message, final Supplier<NetworkEvent.Context> ctx)
    {
        final NetworkEvent.Context context = ctx.get();

        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            assert sender != null;
            final Inventory playerInventory = sender.getInventory();
            final ProjectTableRecipe recipe = message.getRecipe();

            final boolean canCraft = ProjectTableManager.INSTANCE.canCraftRecipe(recipe, playerInventory);
            if (!canCraft) {
                return;
            }

            ProjectTableManager.INSTANCE.craftRecipe(recipe, playerInventory);
        });
    }
}
