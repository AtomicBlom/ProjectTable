package com.github.atomicblom.projecttable.api;

import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public interface ICraftingManagerIngredientOrResult extends ICraftingManagerResult
{
    ICraftingManagerIngredientOrResult andIngredient(Item item);
    ICraftingManagerIngredientOrResult andIngredient(Item item, int amount);
    ICraftingManagerIngredientOrResult andIngredient(Block block);
    ICraftingManagerIngredientOrResult andIngredient(Block block, int amount);
    ICraftingManagerIngredientOrResult andIngredient(IIngredient ingredient);
    ICraftingManagerIngredientOrResult andIngredient(ItemStack ingredient);
    ICraftingManagerIngredientOrResult andIngredients(ItemStack... ingredients);
    ICraftingManagerIngredientOrResult andIngredients(IIngredient... ingredients);
}