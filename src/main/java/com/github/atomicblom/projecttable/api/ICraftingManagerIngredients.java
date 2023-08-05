package com.github.atomicblom.projecttable.api;

import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Created by codew on 9/04/2016.
 */
public interface ICraftingManagerIngredients
{
    ICraftingManagerIngredientOrResult withIngredient(Item item);
    ICraftingManagerIngredientOrResult withIngredient(Item item, int amount);
    ICraftingManagerIngredientOrResult withIngredient(Block block);
    ICraftingManagerIngredientOrResult withIngredient(Block block, int amount);
    ICraftingManagerIngredientOrResult withIngredient(IIngredient ingredient);
    ICraftingManagerIngredientOrResult withIngredient(ItemStack ingredient);
    ICraftingManagerIngredientOrResult withIngredients(ItemStack... ingredients);
    ICraftingManagerIngredientOrResult withIngredients(IIngredient... ingredient);
}