package com.github.atomicblom.projecttable.client.api;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IngredientProblem;
import com.github.atomicblom.projecttable.util.ItemStackUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by codew on 25/01/2016.
 */
public enum ProjectTableManager
{
    INSTANCE;

    private final List<ProjectTableRecipe> recipes = Lists.newArrayList();
    private final List<ProjectTableRecipe> initialSet = Lists.newArrayList();

    public void addProjectTableRecipe(ProjectTableRecipe recipe, boolean isDefaultSet, boolean checkForProblems) {
        List<IngredientProblem> problems = Lists.newArrayList();
        if (recipe.getInput().isEmpty()) {
            problems.add(new IngredientProblem(recipe.getId(), recipe.getSource(), "No inputs to create recipe, or all filtered out as too complex"));
        }

        for (ItemStack itemStack : recipe.output) {
            if (!ForgeRegistries.ITEMS.containsValue(itemStack.getItem()) || itemStack.isEmpty()) {
                problems.add(new IngredientProblem(recipe.getId(), recipe.getSource(), "Invalid ItemStack: " + itemStack.toString()));
            }
        }

        if (checkForProblems) {
            for (IIngredient ingredient : recipe.input) {
            IngredientProblem ingredientProblem = ingredient.assertValid(recipe.getId(), recipe.getSource());
                if (ingredientProblem != null) {
                    problems.add(ingredientProblem);
                }
            }
        }



        if (!problems.isEmpty()) {
            throw new InvalidRecipeException("There was an issue loading the recipe", problems);
        }

        recipes.add(recipe);
        if (isDefaultSet) {
            initialSet.add(recipe);
        }
    }

    public boolean canCraftRecipe(ProjectTableRecipe recipe, List<ItemStack> compactedInventoryItems) {
        for (final IIngredient recipeIngredient : recipe.getInput())
        {
            boolean itemMatched = false;
            int itemsAvailable = 0;
            final List<ItemStack> itemStacks = ItemStackUtils.getAllSubtypes(recipeIngredient.getItemStacks());
            int durabilityRequired = recipeIngredient.getDurabilityCost();
            for (final ItemStack recipeInput : itemStacks)
            {
                for (final ItemStack playerItem : compactedInventoryItems) {
                    if (durabilityRequired > 0) { //Item is damageable, ignore metadata, take into account durability
                        if (recipeInput.getItem() == playerItem.getItem() && ItemStack.isSameItemSameTags(recipeInput, playerItem)) {
                            durabilityRequired -= (recipeInput.getMaxDamage() - recipeInput.getDamageValue());
                            itemMatched = true;
                            itemsAvailable += playerItem.getCount();
                        }
                    } else {
                        //FIXME: Adjust for Tags
                        //if (recipeInput.getItem() == playerItem.getItem() && recipeInput.getMetadata() == playerItem.getMetadata() && ItemStack.areItemStackTagsEqual(recipeInput, playerItem)) {
                        if (recipeInput.getItem() == playerItem.getItem() && ItemStack.isSameItemSameTags(recipeInput, playerItem)) {
                            itemMatched = true;
                            itemsAvailable += playerItem.getCount();
                        }
                    }
                }
            }

            if (durabilityRequired > 0 || itemsAvailable < recipeIngredient.getQuantityConsumed() || !itemMatched) {
                return false;
            }
        }

        return true;
    }

    public boolean canCraftRecipe(ProjectTableRecipe recipe, Inventory playerInventory)
    {
        final List<ItemStack> compactedInventoryItems = getCompactedInventoryItems(playerInventory);
        return canCraftRecipe(recipe, compactedInventoryItems);
    }

    public List<ItemStack> getCompactedInventoryItems(Inventory inventorySlots) {
        List<ItemStack> usableItems = Lists.newArrayList();

        Stream<ItemStack> stream = Stream.concat(inventorySlots.items.stream(), Stream.of(inventorySlots.getSelected()));
        for (final ItemStack itemStack : (Iterable<ItemStack>)stream::iterator)
        {
            if (itemStack == null || itemStack.isEmpty())
            {
                continue;
            }

            boolean itemMatched = false;
            for (final ItemStack existingItemStack : usableItems) {
                //FIXME: Adjust for tags
                if (existingItemStack.getItem() == itemStack.getItem() && ItemStack.isSameItemSameTags(existingItemStack, itemStack))
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

    public void clearRecipes() {
        recipes.clear();
    }

    public void resetRecipesToInitial() {
        recipes.clear();
        recipes.addAll(initialSet);
        ProjectTableMod.logger.info("Reset client recipe list to {} entries", initialSet.size());
    }

    public void craftRecipe(ProjectTableRecipe recipe, Inventory playerInventory) {
        for (final IIngredient ingredient : recipe.getInput())
        {
            int quantityToConsume = ingredient.getQuantityConsumed();
            int durabilityToConsume = ingredient.getDurabilityCost();
            final ImmutableList<ItemStack> itemStacks = ingredient.getItemStacks();
            for (final ItemStack itemStack : itemStacks)
            {
                if (ingredient.isFluidContainer()) {
                    //TODO
                }

                if (quantityToConsume <= 0 && durabilityToConsume <= 0) {
                    break;
                }

                if (durabilityToConsume > 0) {
                    durabilityToConsume -= clearMatchingDurability(playerInventory, itemStack.getItem(), durabilityToConsume, itemStack.getTag());
                    playerInventory.setChanged();
                }

                if (quantityToConsume > 0) {
                    quantityToConsume -= ContainerHelper.clearOrCountMatchingItems(playerInventory, itemStack1 -> itemStack1.sameItem(itemStack), quantityToConsume, false);
                    playerInventory.setChanged();
                }
            }
        }

        for (final ItemStack itemStack : recipe.getOutput())
        {
            final ItemStack copy = itemStack.copy();

            if (!playerInventory.add(copy))
            {
                playerInventory.player.drop(copy, true);
            }
            else
            {
                playerInventory.setChanged();
            }
        }
    }

    private int clearMatchingDurability(Inventory playerInventory, @Nullable Item itemIn, int durability, @Nullable CompoundTag itemNBT)
    {
        if (durability <= 0) return 0;

        int durabilityConsumed = 0;

        for (int j = 0; j < playerInventory.getContainerSize(); ++j)
        {
            ItemStack itemstack = playerInventory.getItem(j);

            if (!itemstack.isEmpty() && (itemIn == null || itemstack.getItem() == itemIn) && (itemNBT == null || itemNBT.equals(itemstack.getTag())))
            {
                int thisItemDurabilityToRemove = Math.min(durability - durabilityConsumed, itemstack.getMaxDamage() - itemstack.getDamageValue() + 1);
                durabilityConsumed += thisItemDurabilityToRemove;

                itemstack.hurtAndBreak(thisItemDurabilityToRemove, playerInventory.player, (t) -> {});

                if (durabilityConsumed >= durability)
                {
                    return durability - durabilityConsumed;
                }
            }
        }

        ItemStack mouseHeldItemStack = playerInventory.getSelected();
        if (!mouseHeldItemStack.isEmpty() && (itemIn == null || mouseHeldItemStack.getItem() == itemIn) && (itemNBT == null || itemNBT.equals(mouseHeldItemStack.getTag())))
        {
            int heldItemDurabilityToRemove = Math.min(durability - durabilityConsumed, mouseHeldItemStack.getMaxDamage() - mouseHeldItemStack.getDamageValue());
            durabilityConsumed += heldItemDurabilityToRemove;

            mouseHeldItemStack.hurtAndBreak(heldItemDurabilityToRemove, playerInventory.player, (t) -> {});
            playerInventory.setPickedItem(mouseHeldItemStack);

            if (durabilityConsumed >= durability) {
                return durability - durabilityConsumed;
            }

        }

        return durability - durabilityConsumed;
    }
}