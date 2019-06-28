package com.github.atomicblom.projecttable.networking;

import com.github.atomicblom.projecttable.crafting.ProjectTableRecipe;
import com.github.atomicblom.projecttable.crafting.CraftingManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Collection;

public class SendProjectTableRecipesPacket implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {
        Collection<ProjectTableRecipe> recipes = CraftingManager.INSTANCE.projectTableManager.getRecipes();

    }
}
