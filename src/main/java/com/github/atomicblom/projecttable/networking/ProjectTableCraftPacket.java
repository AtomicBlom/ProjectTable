package com.github.atomicblom.projecttable.networking;

import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
        recipe.writeToBuffer(new PacketBuffer(buf));
    }

    public static ProjectTableCraftPacket deserialize(ByteBuf buf)
    {
        return new ProjectTableCraftPacket(ProjectTableRecipe.readFromBuffer(new PacketBuffer(buf)));
    }

    public static void received(final ProjectTableCraftPacket message, final Supplier<NetworkEvent.Context> ctx)
    {
        final NetworkEvent.Context context = ctx.get();

        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();
            assert sender != null;
            final PlayerInventory playerInventory = sender.inventory;
            final ProjectTableRecipe recipe = message.getRecipe();

            final boolean canCraft = ProjectTableManager.INSTANCE.canCraftRecipe(recipe, playerInventory);
            if (!canCraft) {
                return;
            }

            ProjectTableManager.INSTANCE.craftRecipe(recipe, playerInventory);
        });
    }
}
