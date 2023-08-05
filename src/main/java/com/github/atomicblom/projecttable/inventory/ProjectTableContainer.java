package com.github.atomicblom.projecttable.inventory;

import com.github.atomicblom.projecttable.library.ContainerTypeLibrary;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by codew on 5/01/2016.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProjectTableContainer extends AbstractContainerMenu {

    private final Inventory playerInventory;

    public ProjectTableContainer(int id, Inventory playerInventory) {
        super(ContainerTypeLibrary.projectTableContainer.get(), id);
        this.playerInventory = playerInventory;
        addPlayerInventory(playerInventory, 79, 145);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (clickTypeIn == ClickType.QUICK_MOVE) {
            return;
        }

        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public Inventory getPlayerInventory() {
        return this.playerInventory;
    }

    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLUMNS = 9;

    private static boolean isSlotInRange(int slotIndex, int slotMin, int slotMax, boolean ascending)
    {
        return ascending ? slotIndex >= slotMin : slotIndex < slotMax;
    }

    private static boolean equalsIgnoreStackSize(ItemStack itemStack1, ItemStack itemStack2)
    {
        if (Item.getId(itemStack1.getItem()) - Item.getId(itemStack2.getItem()) == 0)
        {
            //noinspection ObjectEquality
            if (itemStack1.getItem() == itemStack2.getItem())
            {
                return itemStack1.getDamageValue() == itemStack2.getDamageValue() &&
                        areItemStackTagsEqual(itemStack1, itemStack2);
            }
        }

        return false;
    }

    private static boolean areItemStackTagsEqual(ItemStack itemStack1, ItemStack itemStack2)
    {
        if (itemStack1.hasTag() && itemStack2.hasTag())
        {
            return ItemStack.isSameItemSameTags(itemStack1, itemStack2);
        } else
        {
            return true;
        }
    }

    private static ItemStack cloneItemStack(ItemStack itemStack, int stackSize)
    {
        final ItemStack clonedItemStack = itemStack.copy();
        clonedItemStack.setCount(stackSize);
        return clonedItemStack;
    }

    @SuppressWarnings({"MethodWithMultipleLoops", "OverlyLongMethod", "OverlyComplexMethod"})
    @Override
    protected boolean moveItemStackTo(ItemStack itemStack, int slotMin, int slotMax, boolean ascending)
    {
        boolean slotFound = false;

        if (itemStack.isStackable())
        {
            int currentSlotIndex = ascending ? slotMax - 1 : slotMin;
            while (!itemStack.isEmpty() && isSlotInRange(currentSlotIndex, slotMin, slotMax, ascending))
            {
                final Slot slot = slots.get(currentSlotIndex);
                final ItemStack stackInSlot = slot.getItem();

                if (slot.mayPlace(itemStack) && equalsIgnoreStackSize(itemStack, stackInSlot))
                {
                    final int combinedStackSize = stackInSlot.getCount() + itemStack.getCount();
                    final int slotStackSizeLimit = Math.min(stackInSlot.getMaxStackSize(), slot.getMaxStackSize());

                    if (combinedStackSize <= slotStackSizeLimit)
                    {
                        itemStack.setCount(0);
                        stackInSlot.setCount(combinedStackSize);
                        slot.setChanged();
                        slotFound = true;
                    } else if (stackInSlot.getCount() < slotStackSizeLimit)
                    {
                        itemStack.shrink(slotStackSizeLimit - stackInSlot.getCount());
                        stackInSlot.setCount(slotStackSizeLimit);
                        slot.setChanged();
                        slotFound = true;
                    }
                }

                currentSlotIndex += ascending ? -1 : 1;
            }
        }

        if (!itemStack.isEmpty())
        {
            int currentSlotIndex = ascending ? slotMax - 1 : slotMin;

            while (isSlotInRange(currentSlotIndex, slotMin, slotMax, ascending))
            {
                final Slot slot = slots.get(currentSlotIndex);
                final ItemStack stackInSlot = slot.getItem();

                if (slot.mayPlace(itemStack) && stackInSlot.isEmpty())
                {
                    slot.set(cloneItemStack(itemStack, Math.min(itemStack.getCount(), slot.getMaxStackSize())));
                    slot.setChanged();

                    if (!slot.getItem().isEmpty())
                    {
                        itemStack.shrink(slot.getItem().getCount());
                        return true;
                    }
                }

                currentSlotIndex += ascending ? -1 : 1;
            }
        }

        return slotFound;
    }

    void addPlayerInventory(Inventory playerInventory, int xOffset, int yOffset)
    {
        for (int inventoryRowIndex = 0; inventoryRowIndex < PLAYER_INVENTORY_ROWS; ++inventoryRowIndex)
        {
            addInventoryRowSlots(playerInventory, xOffset, yOffset, inventoryRowIndex);
        }

        addActionBarSlots(playerInventory, xOffset, yOffset);
    }

    private void addInventoryRowSlots(Inventory playerInventory, int xOffset, int yOffset, int rowIndex)
    {
        for (int inventoryColumnIndex = 0; inventoryColumnIndex < PLAYER_INVENTORY_COLUMNS; ++inventoryColumnIndex)
        {
            //noinspection ObjectAllocationInLoop

            addSlot(new Slot(playerInventory, inventoryColumnIndex + rowIndex * 9 + 9, xOffset + inventoryColumnIndex * 18, yOffset + rowIndex * 18));

        }
    }

    private void addActionBarSlots(Inventory playerInventory, int xOffset, int yOffset)
    {
        for (int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex)
        {
            //noinspection ObjectAllocationInLoop
            addSlot(new Slot(playerInventory, actionBarSlotIndex, xOffset + actionBarSlotIndex * 18, yOffset + 58));
        }
    }
}