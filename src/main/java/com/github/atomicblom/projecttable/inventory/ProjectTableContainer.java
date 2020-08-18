package com.github.atomicblom.projecttable.inventory;

import com.github.atomicblom.projecttable.library.ContainerTypeLibrary;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by codew on 5/01/2016.
 */
public class ProjectTableContainer extends Container {

    private final PlayerInventory playerInventory;
    /** The crafting matrix inventory (3x3). */
    public CraftingInventory craftMatrix = new CraftingInventory(this, 1, 32);
    public IInventory craftResult = new CraftResultInventory();
    private World worldObj;
    /** Position of the workbench */
    private BlockPos pos;

    public ProjectTableContainer(int id, PlayerInventory playerInventory) {
        super(ContainerTypeLibrary.projectTableContainer, id);
        this.playerInventory = playerInventory;
        addPlayerInventory(playerInventory, 79, 145);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn == ClickType.QUICK_MOVE) {
            return null;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    public PlayerInventory getPlayerInventory() {
        return this.playerInventory;
    }

    class ProjectTableCraftingSlot extends CraftingResultSlot
    {
        private final PlayerEntity player;
        private final CraftingInventory craftMatrix;

        public ProjectTableCraftingSlot(PlayerEntity player, CraftingInventory craftingMaterials, IInventory craftingOutput, int slotIndex)
        {
            super(player, craftingMaterials, craftingOutput, slotIndex, 0, 0);
            this.player = player;
            craftMatrix = craftingMaterials;
        }

        public void onPickupFromSlot(PlayerEntity playerIn, ItemStack stack)
        {
            //FMLCommonHandler.instance().firePlayerCraftingEvent(playerIn, stack, ProjectTableContainer.this.craftMatrix);
            onCrafting(stack);
        }
    }

    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLUMNS = 9;

    private static boolean isSlotInRange(int slotIndex, int slotMin, int slotMax, boolean ascending)
    {
        return ascending ? slotIndex >= slotMin : slotIndex < slotMax;
    }

    private static boolean equalsIgnoreStackSize(ItemStack itemStack1, ItemStack itemStack2)
    {
        if (itemStack1 != null && itemStack2 != null)
        {
            if (Item.getIdFromItem(itemStack1.getItem()) - Item.getIdFromItem(itemStack2.getItem()) == 0)
            {
                //noinspection ObjectEquality
                if (itemStack1.getItem() == itemStack2.getItem())
                {
                    return itemStack1.getDamage() == itemStack2.getDamage() &&
                            areItemStackTagsEqual(itemStack1, itemStack2);
                }
            }
        }

        return false;
    }

    private static boolean areItemStackTagsEqual(ItemStack itemStack1, ItemStack itemStack2)
    {
        if (itemStack1.hasTag() && itemStack2.hasTag())
        {
            return ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
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
    protected boolean mergeItemStack(ItemStack itemStack, int slotMin, int slotMax, boolean ascending)
    {
        boolean slotFound = false;

        if (itemStack.isStackable())
        {
            int currentSlotIndex = ascending ? slotMax - 1 : slotMin;
            while (!itemStack.isEmpty() && isSlotInRange(currentSlotIndex, slotMin, slotMax, ascending))
            {
                final Slot slot = inventorySlots.get(currentSlotIndex);
                final ItemStack stackInSlot = slot.getStack();

                if (slot.isItemValid(itemStack) && equalsIgnoreStackSize(itemStack, stackInSlot))
                {
                    final int combinedStackSize = stackInSlot.getCount() + itemStack.getCount();
                    final int slotStackSizeLimit = Math.min(stackInSlot.getMaxStackSize(), slot.getSlotStackLimit());

                    if (combinedStackSize <= slotStackSizeLimit)
                    {
                        itemStack.setCount(0);
                        stackInSlot.setCount(combinedStackSize);
                        slot.onSlotChanged();
                        slotFound = true;
                    } else if (stackInSlot.getCount() < slotStackSizeLimit)
                    {
                        itemStack.shrink(slotStackSizeLimit - stackInSlot.getCount());
                        stackInSlot.setCount(slotStackSizeLimit);
                        slot.onSlotChanged();
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
                final Slot slot = inventorySlots.get(currentSlotIndex);
                final ItemStack stackInSlot = slot.getStack();

                if (slot.isItemValid(itemStack) && stackInSlot == null)
                {
                    slot.putStack(cloneItemStack(itemStack, Math.min(itemStack.getCount(), slot.getSlotStackLimit())));
                    slot.onSlotChanged();

                    if (slot.getStack() != null)
                    {
                        itemStack.shrink(slot.getStack().getCount());
                        return true;
                    }
                }

                currentSlotIndex += ascending ? -1 : 1;
            }
        }

        return slotFound;
    }

    void addPlayerInventory(PlayerInventory playerInventory, int xOffset, int yOffset)
    {
        for (int inventoryRowIndex = 0; inventoryRowIndex < PLAYER_INVENTORY_ROWS; ++inventoryRowIndex)
        {
            addInventoryRowSlots(playerInventory, xOffset, yOffset, inventoryRowIndex);
        }

        addActionBarSlots(playerInventory, xOffset, yOffset);
    }

    private void addInventoryRowSlots(PlayerInventory playerInventory, int xOffset, int yOffset, int rowIndex)
    {
        for (int inventoryColumnIndex = 0; inventoryColumnIndex < PLAYER_INVENTORY_COLUMNS; ++inventoryColumnIndex)
        {
            //noinspection ObjectAllocationInLoop

            addSlot(new Slot(playerInventory, inventoryColumnIndex + rowIndex * 9 + 9, xOffset + inventoryColumnIndex * 18, yOffset + rowIndex * 18));

        }
    }

    private void addActionBarSlots(PlayerInventory playerInventory, int xOffset, int yOffset)
    {
        for (int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex)
        {
            //noinspection ObjectAllocationInLoop
            addSlot(new Slot(playerInventory, actionBarSlotIndex, xOffset + actionBarSlotIndex * 18, yOffset + 58));
        }
    }

    boolean didTransferStackInStandardSlot(int slotIndex, ItemStack slotItemStack, int indexFirstStdSlot)
    {
        if (slotIndex >= indexFirstStdSlot && slotIndex < inventorySlots.size() - 9)
        {
            return !mergeItemStack(slotItemStack, inventorySlots.size() - 9, inventorySlots.size(), false);
        } else if (slotIndex >= inventorySlots.size() - 9 && slotIndex < inventorySlots.size())
        {
            return !mergeItemStack(slotItemStack, indexFirstStdSlot, inventorySlots.size() - 9, false);
        }
        return false;
    }
}