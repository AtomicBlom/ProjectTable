package com.github.atomicblom.projecttable.networking.serialization;

import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ingredient.BlockTagIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.api.ingredient.ItemTagIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class BlockTagIngredientSerializer implements IIngredientSerializer
{
    @Override
    public IIngredient deserialize(PacketBuffer buffer)
    {
        final ResourceLocation tagName = buffer.readResourceLocation();
        final int quantity = buffer.readInt();
        BlockTagIngredient blockTagIngredient = new BlockTagIngredient(tagName, quantity);
        blockTagIngredient.setDurabilityCost(buffer.readInt());
        blockTagIngredient.setFluidContainer(buffer.readBoolean());
        return blockTagIngredient;
    }

    @Override
    public void serialize(IIngredient ingredient, PacketBuffer buffer)
    {
        if (!(ingredient instanceof BlockTagIngredient)) throw new ProjectTableException("Attempt to deserialize an ingredient that is not a BlockTagIngredient");
        final BlockTagIngredient blockTagIngredient = (BlockTagIngredient) ingredient;

        buffer.writeResourceLocation(blockTagIngredient.getTagName())
                .writeInt(blockTagIngredient.getQuantityConsumed())
                .writeInt(blockTagIngredient.getDurabilityCost())
                .writeBoolean(blockTagIngredient.isFluidContainer());
    }
}