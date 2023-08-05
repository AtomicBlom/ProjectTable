package com.github.atomicblom.projecttable.networking;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * Created by codew on 1/07/2016.
 */
public class FriendlyByteBufExtensions
{
    /**
     * An alternative to FriendlyByteBuf.writeItemStackToBuffer, this version allows large stack sizes.
     * Writes the ItemStack's ID (short), then size (int), then damage. (short)
     */
    public static FriendlyByteBuf writeLargeItemStackToBuffer(FriendlyByteBuf FriendlyByteBuf, @Nonnull ItemStack stack)
    {
        //FIXME: Verify that this should be false.
        boolean limitedTag = false;

        if (stack.isEmpty()) {
            FriendlyByteBuf.writeBoolean(false);
        } else {
            FriendlyByteBuf.writeBoolean(true);
            Item item = stack.getItem();
            FriendlyByteBuf.writeId(Registry.ITEM, item);
            FriendlyByteBuf.writeInt(stack.getCount());
            CompoundTag compoundtag = null;
            if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
                compoundtag = limitedTag ? stack.getShareTag() : stack.getTag();
            }

            FriendlyByteBuf.writeNbt(compoundtag);
        }

        return FriendlyByteBuf;
    }

    /**
     * Reads an ItemStack from this buffer, allowing for large sized stacks.
     */
    public static ItemStack readLargeItemStackFromBuffer(FriendlyByteBuf FriendlyByteBuf) {
        if (!FriendlyByteBuf.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int i = FriendlyByteBuf.readVarInt();
            int j = FriendlyByteBuf.readInt();
            ItemStack itemstack = new ItemStack(Item.byId(i), j);

            itemstack.setTag(FriendlyByteBuf.readNbt());
            return itemstack;
        }
    }
}