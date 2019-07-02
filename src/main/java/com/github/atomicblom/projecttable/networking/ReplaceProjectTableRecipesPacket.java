package com.github.atomicblom.projecttable.networking;

import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Collection;
import java.util.List;

public class ReplaceProjectTableRecipesPacket implements IMessage
{
    private Collection<ProjectTableRecipe> recipes;

    public ReplaceProjectTableRecipesPacket()
    {
    }

    public ReplaceProjectTableRecipesPacket(Collection<ProjectTableRecipe> recipes)
    {
        this.recipes = recipes;
    }

    public Collection<ProjectTableRecipe> getRecipes() {
        return recipes;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        List<ProjectTableRecipe> replacementRecipes = Lists.newArrayList();
        int recipeCount = buf.readInt();
        for (int i = 0; i < recipeCount; i++) {
            replacementRecipes.add(ProjectTableRecipe.readFromBuffer(new PacketBuffer(buf)));
        }
        this.recipes = replacementRecipes;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        Collection<ProjectTableRecipe> allRecipes = ProjectTableManager.INSTANCE.getRecipes();
        buf.writeInt(allRecipes.size());
        for (ProjectTableRecipe recipe : allRecipes) {
            recipe.writeToBuffer(new PacketBuffer(buf));
        }
    }
}
