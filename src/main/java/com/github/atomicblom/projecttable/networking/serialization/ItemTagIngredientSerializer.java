package com.github.atomicblom.projecttable.networking.serialization;

import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.api.ingredient.ItemTagIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class ItemTagIngredientSerializer implements IIngredientSerializer
{
    @Override
    public IIngredient deserialize(PacketBuffer buffer)
    {
        final ResourceLocation tagName = buffer.readResourceLocation();
        final int quantity = buffer.readInt();
        ItemTagIngredient itemTagIngredient = new ItemTagIngredient(tagName, quantity);
        itemTagIngredient.setDurabilityCost(buffer.readInt());
        itemTagIngredient.setFluidContainer(buffer.readBoolean());
        return itemTagIngredient;
    }

    @Override
    public void serialize(IIngredient ingredient, PacketBuffer buffer)
    {
        if (!(ingredient instanceof ItemTagIngredient)) throw new ProjectTableException("Attempt to deserialize an ingredient that is not an ItemTagIngredient");
        final ItemTagIngredient itemTagIngredient = (ItemTagIngredient) ingredient;

        buffer.writeResourceLocation(itemTagIngredient.getTagName())
                .writeInt(itemTagIngredient.getQuantityConsumed())
                .writeInt(itemTagIngredient.getDurabilityCost())
                .writeBoolean(itemTagIngredient.isFluidContainer());
    }
}