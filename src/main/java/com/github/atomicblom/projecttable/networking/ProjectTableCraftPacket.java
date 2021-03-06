package com.github.atomicblom.projecttable.networking;

import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ProjectTableCraftPacket implements IMessage
{
    private ProjectTableRecipe recipe;

    public ProjectTableCraftPacket()
    {
    }

    @SideOnly(Side.CLIENT)
    public ProjectTableCraftPacket(ProjectTableRecipe recipe)
    {
        this.recipe = recipe;
    }

    public ProjectTableRecipe getRecipe() {
        return recipe;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        recipe = ProjectTableRecipe.readFromBuffer(new PacketBuffer(buf));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        recipe.writeToBuffer(new PacketBuffer(buf));
    }
}
