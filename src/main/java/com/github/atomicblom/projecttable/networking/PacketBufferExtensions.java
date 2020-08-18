package com.github.atomicblom.projecttable.networking;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
        //FIXME: Verify that this should be false.
        boolean limitedTag = false;
        if (stack.isEmpty()) {
            packetBuffer.writeBoolean(false);
        } else {
            packetBuffer.writeBoolean(true);
            Item item = stack.getItem();
            packetBuffer.writeVarInt(Item.getIdFromItem(item));
            packetBuffer.writeInt(stack.getCount());
            CompoundNBT compoundnbt = null;
            if (item.isDamageable() || item.shouldSyncTag()) {
                compoundnbt = limitedTag ? stack.getShareTag() : stack.getTag();
            }

            packetBuffer.writeCompoundTag(compoundnbt);
        }

        return packetBuffer;
    }

    /**
     * Reads an ItemStack from this buffer, allowing for large sized stacks.
     */
    @Nullable
    public static ItemStack readLargeItemStackFromBuffer(PacketBuffer packetBuffer) throws IOException
    {
        if (!packetBuffer.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int i = packetBuffer.readVarInt();
            int j = packetBuffer.readInt();
            ItemStack itemstack = new ItemStack(Item.getItemById(i), j);
            //itemstack.readShareTag(packetBuffer.readCompoundTag());

            itemstack.setTag(packetBuffer.readCompoundTag());
            return itemstack;
        }
    }
}