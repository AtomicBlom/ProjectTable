package com.github.atomicblom.projecttable.networking;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Created by codew on 1/07/2016.
 */
public class PacketBufferExtensions
{
    /**
     * An alternative to PacketBuffer.writeItemStackToBuffer, this version allows large stack sizes.
     * Writes the ItemStack's ID (short), then size (int), then damage. (short)
     */
    public static PacketBuffer writeLargeItemStackToBuffer(PacketBuffer packetBuffer, @Nonnull ItemStack stack)
    {
        packetBuffer.writeShort(Item.getIdFromItem(stack.getItem()));
        packetBuffer.writeInt(stack.getCount());
        packetBuffer.writeShort(stack.getMetadata());
        NBTTagCompound nbttagcompound = null;

        if (stack.getItem().isDamageable() || stack.getItem().getShareTag())
        {
            nbttagcompound = stack.getTagCompound();
        }

        packetBuffer.writeCompoundTag(nbttagcompound);

        return packetBuffer;
    }

    /**
     * Reads an ItemStack from this buffer, allowing for large sized stacks.
     */
    @Nullable
    public static ItemStack readLargeItemStackFromBuffer(PacketBuffer packetBuffer) throws IOException
    {
        int itemId = packetBuffer.readShort();
        int itemCount = packetBuffer.readInt();
        int itemMetadata = packetBuffer.readShort();
        ItemStack itemstack = new ItemStack(Item.getItemById(itemId), itemCount, itemMetadata);
        itemstack.setTagCompound(packetBuffer.readCompoundTag());
        return itemstack;
    }
}