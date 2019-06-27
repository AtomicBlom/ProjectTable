package com.github.atomicblom.projecttable.client.api;

import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.InvalidIngredientException;
import com.github.atomicblom.projecttable.util.ItemStackUtils;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.RegistryManager;

import java.util.Collection;
import java.util.List;

/**
 * Created by codew on 25/01/2016.
 */
public enum ProjectTableManager
{
    INSTANCE;

    private List<ProjectTableRecipe> recipes = Lists.newArrayList();

    public void addProjectTableRecipe(ProjectTableRecipe recipe) {
        for (ItemStack itemStack : recipe.output) {
            if (!RegistryManager.ACTIVE.getRegistry(Item.class).containsValue(itemStack.getItem()) || itemStack.isEmpty()) {
                throw new InvalidIngredientException(recipe.getId(), recipe.getSource(), "Invalid ItemStack: " + itemStack.toString());
            }
        }

        for (IIngredient ingredient : recipe.input) {
            ingredient.assertValid(recipe.getId(), recipe.getSource());
        }

        recipes.add(recipe);
    }

    public boolean canCraftRecipe(ProjectTableRecipe recipe, InventoryPlayer playerInventory)
    {
        final List<ItemStack> compactedInventoryItems = getCompactedInventoryItems(playerInventory);

        for (final IIngredient recipeIngredient : recipe.getInput())
        {
            boolean itemMatched = false;
            int itemsAvailable = 0;
            final List<ItemStack> itemStacks = ItemStackUtils.getAllSubtypes(recipeIngredient.getItemStacks());
            for (final ItemStack recipeInput : itemStacks)
            {
                for (final ItemStack playerItem : compactedInventoryItems) {
                    if (recipeInput.getItem() == playerItem.getItem() && recipeInput.getMetadata() == playerItem.getMetadata() && ItemStack.areItemStackTagsEqual(recipeInput, playerItem)) {
                        itemMatched = true;
                        itemsAvailable += playerItem.getCount();
                    }
                }
            }

            if (itemsAvailable < recipeIngredient.getQuantityConsumed() || !itemMatched) {
                return false;
            }
        }

        return true;
    }

    private List<ItemStack> getCompactedInventoryItems(InventoryPlayer inventorySlots) {
        List<ItemStack> usableItems = Lists.newArrayList();
        for (final ItemStack itemStack : inventorySlots.mainInventory)
        {
            if (itemStack == null || itemStack.isEmpty())
            {
                continue;
            }

            boolean itemMatched = false;
            for (final ItemStack existingItemStack : usableItems) {
                if (existingItemStack.getItem() == itemStack.getItem() && existingItemStack.getMetadata() == itemStack.getMetadata() && ItemStack.areItemStackTagsEqual(existingItemStack, itemStack))
                {
                    itemMatched = true;
                    existingItemStack.grow(itemStack.getCount());
                }
            }
            if (!itemMatched) {
                final ItemStack copy = itemStack.copy();
                usableItems.add(copy);
            }
        }
        return usableItems;
    }

    public Collection<ProjectTableRecipe> getRecipes()
    {
        return recipes;
    }


}