package com.github.atomicblom.projecttable.networking;


import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

public class ProjectTableCraftPacketMessageHandler implements IMessageHandler<ProjectTableCraftPacket, IMessage>
{
    @Override
    public IMessage onMessage(final ProjectTableCraftPacket message, final MessageContext ctx)
    {
        final InventoryPlayer playerInventory = ctx.getServerHandler().player.inventory;
        final ProjectTableRecipe recipe = message.getRecipe();

        final boolean canCraft = ProjectTableManager.INSTANCE.canCraftRecipe(recipe, playerInventory);
        if (!canCraft) {
            return null;
        }

        IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
        mainThread.addScheduledTask(() -> {
            for (final IIngredient ingredient : recipe.getInput())
            {
                int quantityToConsume = ingredient.getQuantityConsumed();
                int durabilityToConsume = ingredient.getDurabilityCost();
                final ImmutableList<ItemStack> itemStacks = ingredient.getItemStacks();
                for (final ItemStack itemStack : itemStacks)
                {
                    int metadata = itemStack.getMetadata();
                    metadata = metadata == OreDictionary.WILDCARD_VALUE ? -1 : metadata;

                    if (ingredient.isFluidContainer()) {
                        //TODO
                    }

                    if (quantityToConsume <= 0 && durabilityToConsume <= 0) {
                        return;
                    }

                    if (durabilityToConsume > 0) {
                        durabilityToConsume -= clearMatchingDurability(playerInventory, itemStack.getItem(), metadata, durabilityToConsume, itemStack.getTagCompound());
                        playerInventory.markDirty();
                    }

                    if (quantityToConsume > 0) {
                        quantityToConsume -= playerInventory.clearMatchingItems(itemStack.getItem(), metadata, quantityToConsume, itemStack.getTagCompound());
                        playerInventory.markDirty();
                    }
                }
            }

            for (final ItemStack itemStack : recipe.getOutput())
            {
                final ItemStack copy = itemStack.copy();

                if (!playerInventory.addItemStackToInventory(copy))
                {
                    ctx.getServerHandler().player.dropItem(copy, true);
                }
                else
                {
                    playerInventory.markDirty();
                }
            }
        });

        return null; // no response in this case
    }

    public int clearMatchingDurability(InventoryPlayer playerInventory, @Nullable Item itemIn, int metadataIn, int removeCount, @Nullable NBTTagCompound itemNBT)
    {
        int i = 0;

        for (int j = 0; j < playerInventory.getSizeInventory(); ++j)
        {
            ItemStack itemstack = playerInventory.getStackInSlot(j);

            if (!itemstack.isEmpty() && (itemIn == null || itemstack.getItem() == itemIn) && (metadataIn <= -1 || itemstack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.areNBTEquals(itemNBT, itemstack.getTagCompound(), true)))
            {
                int k = removeCount <= 0 ? itemstack.getItemDamage() : Math.min(removeCount - i, itemstack.getItemDamage());
                i += k;

                if (removeCount != 0)
                {
                    itemstack.damageItem(k, playerInventory.player);

                    if (removeCount > 0 && i >= removeCount)
                    {
                        return i;
                    }
                }
            }
        }

        ItemStack mouseHeldItemStack = playerInventory.getItemStack();
        if (!mouseHeldItemStack.isEmpty())
        {
            if (itemIn != null && mouseHeldItemStack.getItem() != itemIn)
            {
                return i;
            }

            if (metadataIn > -1 && mouseHeldItemStack.getMetadata() != metadataIn)
            {
                return i;
            }

            if (itemNBT != null && !NBTUtil.areNBTEquals(itemNBT, mouseHeldItemStack.getTagCompound(), true))
            {
                return i;
            }

            int l = removeCount <= 0 ? mouseHeldItemStack.getCount() : Math.min(removeCount - i, mouseHeldItemStack.getCount());
            i += l;

            if (removeCount != 0)
            {
                mouseHeldItemStack.damageItem(l, playerInventory.player);

                if (removeCount > 0 && i >= removeCount)
                {
                    return i;
                }
            }
        }

        return i;
    }
}