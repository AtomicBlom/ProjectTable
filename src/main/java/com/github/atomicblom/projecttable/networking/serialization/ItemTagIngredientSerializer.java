package com.github.atomicblom.projecttable.networking.serialization;

import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.api.ingredient.ItemTagIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemTagIngredientSerializer implements IIngredientSerializer
{
    @Override
    public IIngredient deserialize(FriendlyByteBuf buffer)
    {
        final ResourceLocation tagName = buffer.readResourceLocation();
        final int quantity = buffer.readInt();
        TagKey<Item> tagKey = ForgeRegistries.ITEMS.tags().createTagKey(tagName);
        ItemTagIngredient itemTagIngredient = new ItemTagIngredient(tagKey, quantity);
        itemTagIngredient.setDurabilityCost(buffer.readInt());
        itemTagIngredient.setFluidContainer(buffer.readBoolean());
        return itemTagIngredient;
    }

    @Override
    public void serialize(IIngredient ingredient, FriendlyByteBuf buffer)
    {
        if (!(ingredient instanceof ItemTagIngredient)) throw new ProjectTableException("Attempt to deserialize an ingredient that is not an ItemTagIngredient");
        final ItemTagIngredient itemTagIngredient = (ItemTagIngredient) ingredient;

        buffer.writeResourceLocation(itemTagIngredient.getTagName().location())
                .writeInt(itemTagIngredient.getQuantityConsumed())
                .writeInt(itemTagIngredient.getDurabilityCost())
                .writeBoolean(itemTagIngredient.isFluidContainer());
    }
}