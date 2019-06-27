package com.github.atomicblom.projecttable.crafting;

import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.*;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.ItemStackIngredient;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by codew on 10/04/2016.
 */
@SuppressWarnings({"ClassWithTooManyMethods", "ClassHasNoToStringMethod"})
class ProjectTableRecipeContext implements ICraftingManagerIngredientOrResult, ICraftingManagerIngredientsOrLabel
{
    private final CraftingManager parent;
    private final ProjectTableManager projectTableManager;
    private String label = null;
    private final List<IIngredient> ingredients = Lists.newArrayList();
    private String id = "unidentified";
    private String source = "fluent:no source specified";

    ProjectTableRecipeContext(CraftingManager parent, ProjectTableManager projectTableManager)
    {

        this.parent = parent;
        this.projectTableManager = projectTableManager;
    }

    @Override
    public ICraftingManagerIngredients withLabel(String label)
    {
        this.label = label;
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult withIngredient(Item item)
    {
        withIngredient(item, 1);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult withIngredient(Item item, int amount)
    {
        withIngredient(new ItemStack(item, amount));
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult withIngredient(Block block)
    {
        withIngredient(block, 1);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult withIngredient(Block block, int amount)
    {
        withIngredient(new ItemStack(block, amount));
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult withIngredient(IIngredient ingredient)
    {
        withIngredients(ingredient);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult withIngredient(ItemStack ingredient)
    {
        withIngredients(ingredient);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult withIngredients(ItemStack... ingredients)
    {
        this.ingredients.addAll(wrapItemStacks(Arrays.asList(ingredients)));
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult withIngredients(IIngredient... ingredients)
    {
        this.ingredients.addAll(Arrays.asList(ingredients));
        return this;
    }


    @Override
    public ICraftingManagerIngredientOrResult andIngredient(Item item)
    {
        withIngredient(item, 1);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult andIngredient(Item item, int amount)
    {
        withIngredient(item, amount);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult andIngredient(Block block)
    {
        withIngredient(block, 1);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult andIngredient(Block block, int amount)
    {
        withIngredient(block, amount);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult andIngredient(IIngredient ingredient)
    {
        withIngredients(ingredient);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult andIngredient(ItemStack ingredient)
    {
        withIngredient(ingredient);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult andIngredients(ItemStack... ingredients)
    {
        withIngredients(ingredients);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult andIngredients(IIngredient... ingredients)
    {
        withIngredients(ingredients);
        return this;
    }

    @Override
    public ICraftingManager crafts(Item output)
    {
        return crafts(output, 1);
    }

    @Override
    public ICraftingManager crafts(Item output, int amount)
    {
        return crafts(new ItemStack(output, amount));
    }

    @Override
    public ICraftingManager crafts(Block block)
    {
        return crafts(block, 1);
    }

    @Override
    public ICraftingManager crafts(Block block, int amount)
    {
        return crafts(new ItemStack(block, amount));
    }

    @Override
    public ICraftingManager crafts(ItemStack output)
    {
        return crafts(new ItemStack[]{output});
    }

    @Override
    public ICraftingManager crafts(ItemStack... outputs)
    {
        final List<ItemStack> outputList = Arrays.asList(outputs);
        if (label == null)
        {
            final Optional<ItemStack> first = outputList.stream().findFirst();
            if (first.isPresent())
            {
                label = first.get().getDisplayName();
            } else
            {
                throw new ProjectTableException("Attempt to add a Crafting Project recipe with no result.");
            }
        }

        projectTableManager.addProjectTableRecipe(new ProjectTableRecipe(id, source, outputList, label, ingredients));
        return parent;
    }

    private static List<IIngredient> wrapItemStacks(Collection<ItemStack> input)
    {
        return input.stream().map(ItemStackIngredient::new).collect(Collectors.toList());
    }

    public ProjectTableRecipeContext setId(String id) {
        this.id = id;
        return this;
    }

    public ProjectTableRecipeContext setSource(String source) {
        this.source = source;
        return this;
    }
}