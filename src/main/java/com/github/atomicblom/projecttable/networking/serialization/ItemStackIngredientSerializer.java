package com.github.atomicblom.projecttable.networking.serialization;


import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.api.ingredient.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Created by codew on 26/01/2016.
 */
public class ItemStackIngredientSerializer implements IIngredientSerializer
{
    @Override
    public IIngredient deserialize(FriendlyByteBuf buffer)
    {
        ItemStackIngredient itemStackIngredient = new ItemStackIngredient(buffer.readItem());
        int consumedSize = buffer.readInt();
        if (consumedSize != itemStackIngredient.getQuantityConsumed()) {
            itemStackIngredient.overrideAmountConsumed(consumedSize);
        }
        itemStackIngredient.setDurabilityCost(buffer.readInt());
        itemStackIngredient.setFluidContainer(buffer.readBoolean());
        return itemStackIngredient;
    }

    @Override
    public void serialize(IIngredient ingredient, FriendlyByteBuf buffer)
    {
        if (!(ingredient instanceof ItemStackIngredient)) throw new ProjectTableException("Attempt to deserialize an ingredient that is not an ItemStackIngredient");
        final ItemStackIngredient itemStackIngredient = (ItemStackIngredient) ingredient;
        buffer.writeItem(itemStackIngredient.getItemStack())
                .writeInt(itemStackIngredient.getQuantityConsumed())
                .writeInt(itemStackIngredient.getDurabilityCost())
                .writeBoolean(itemStackIngredient.isFluidContainer());
    }
}