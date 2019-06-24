package com.github.atomicblom.projecttable.api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ICraftingManagerResult
{
    ICraftingManager crafts(Item output);
    ICraftingManager crafts(Item output, int amount);
    ICraftingManager crafts(Block block);
    ICraftingManager crafts(Block block, int amount);
    ICraftingManager crafts(ItemStack output);
    ICraftingManager crafts(ItemStack... outputs);
}