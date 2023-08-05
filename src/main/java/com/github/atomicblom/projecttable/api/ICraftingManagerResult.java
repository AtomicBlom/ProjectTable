package com.github.atomicblom.projecttable.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public interface ICraftingManagerResult
{
    ICraftingManager crafts(Item output);
    ICraftingManager crafts(Item output, int amount);
    ICraftingManager crafts(Block block);
    ICraftingManager crafts(Block block, int amount);
    ICraftingManager crafts(ItemStack output);
    ICraftingManager crafts(ItemStack... outputs);
}