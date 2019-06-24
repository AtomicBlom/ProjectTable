package com.github.atomicblom.projecttable.networking.serialization;


import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.api.ingredient.ItemStackIngredient;
import com.github.atomicblom.projecttable.networking.PacketBufferExtensions;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

/**
 * Created by codew on 26/01/2016.
 */
public class ItemStackIngredientSerializer implements IIngredientSerializer
{
    @Override
    public IIngredient deserialize(PacketBuffer buffer)
    {
        try
        {
            return new ItemStackIngredient(PacketBufferExtensions.readLargeItemStackFromBuffer(buffer));
        } catch (IOException e)
        {
            throw new ProjectTableException(e);
        }
    }

    @Override
    public void serialize(IIngredient ingredient, PacketBuffer buffer)
    {
        if (!(ingredient instanceof ItemStackIngredient)) throw new ProjectTableException("Attempt to deserialize an ingredient that is not an ItemStackIngredient");
        final ItemStackIngredient itemStackIngredient = (ItemStackIngredient) ingredient;
        PacketBufferExtensions.writeLargeItemStackToBuffer(buffer, itemStackIngredient.getItemStack());
    }
}