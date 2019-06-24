package com.github.atomicblom.projecttable.client.api;

import com.github.atomicblom.projecttable.api.IProjectTableManager;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.util.ItemStackUtils;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.List;

/**
 * Created by codew on 25/01/2016.
 */
public enum ProjectTableManager implements IProjectTableManager
{
    INSTANCE;

    private List<ProjectTableRecipe> recipes = Lists.newArrayList();

    @Override
    public void addProjectTableRecipe(Collection<ItemStack> output, String displayName, Collection<IIngredient> ingredients)
    {
        recipes.add(new ProjectTableRecipe(output, displayName, ingredients));
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
            if (itemStack == null || itemStack.getItem() == null)
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