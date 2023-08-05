package com.github.atomicblom.projecttable.networking.serialization;

import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ingredient.BlockTagIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockTagIngredientSerializer implements IIngredientSerializer
{
    @Override
    public IIngredient deserialize(FriendlyByteBuf buffer)
    {
        final ResourceLocation tagName = buffer.readResourceLocation();
        final int quantity = buffer.readInt();
        TagKey<Block> tagKey = ForgeRegistries.BLOCKS.tags().createTagKey(tagName);
        BlockTagIngredient blockTagIngredient = new BlockTagIngredient(tagKey, quantity);
        blockTagIngredient.setDurabilityCost(buffer.readInt());
        blockTagIngredient.setFluidContainer(buffer.readBoolean());
        return blockTagIngredient;
    }

    @Override
    public void serialize(IIngredient ingredient, FriendlyByteBuf buffer)
    {
        if (!(ingredient instanceof BlockTagIngredient)) throw new ProjectTableException("Attempt to deserialize an ingredient that is not a BlockTagIngredient");
        final BlockTagIngredient blockTagIngredient = (BlockTagIngredient) ingredient;

        buffer.writeResourceLocation(blockTagIngredient.getTagName().location())
                .writeInt(blockTagIngredient.getQuantityConsumed())
                .writeInt(blockTagIngredient.getDurabilityCost())
                .writeBoolean(blockTagIngredient.isFluidContainer());
    }
}