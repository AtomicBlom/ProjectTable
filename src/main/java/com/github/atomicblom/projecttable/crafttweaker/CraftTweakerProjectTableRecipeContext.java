package com.github.atomicblom.projecttable.crafttweaker;

import com.github.atomicblom.projecttable.crafting.CraftingManager;
import com.github.atomicblom.projecttable.crafting.ProjectTableManager;
import com.google.common.collect.Lists;
import crafttweaker.api.item.IIngredient;

import java.util.List;

/**
 * Created by codew on 10/04/2016.
 */
@SuppressWarnings({"ClassWithTooManyMethods", "ClassHasNoToStringMethod"})
class CraftTweakerProjectTableRecipeContext implements ICraftingManagerIngredientOrResult, ICraftingManagerIngredientsOrLabel
{
    private final CraftingManager parent;
    private final ProjectTableManager projectTableManager;
    private String label = null;
    private final List<com.github.atomicblom.projecttable.api.ingredient.IIngredient> ingredients = Lists.newArrayList();
    private String id = "unidentified";
    private String source = "fluent:no source specified";

    CraftTweakerProjectTableRecipeContext(CraftingManager parent, ProjectTableManager projectTableManager)
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
    public ICraftingManagerIngredientOrResult withIngredient(IIngredient ingredient)
    {
        this.withIngredients(ingredient);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult withIngredients(IIngredient... ingredients)
    {
        //this.ingredients.addAll(Arrays.asList(ingredients));
        //FIXME: Convert to ProjectTable objects
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult andIngredient(IIngredient ingredient)
    {
        withIngredients(ingredient);
        return this;
    }

    @Override
    public ICraftingManagerIngredientOrResult andIngredients(IIngredient... ingredients)
    {
        withIngredients(ingredients);
        return this;
    }

    @Override
    public void crafts(IIngredient... outputs)
    {
        /*final List<IIngredient> outputList = Arrays.asList(outputs);
        if (label == null)
        {
            final Optional<IIngredient> first = outputList.stream().findFirst();
            if (first.isPresent())
            {
                label = first.get().getDisplayName();
            } else
            {
                throw new ProjectTableException("Attempt to add a Crafting Project recipe with no result.");
            }
        }

        projectTableManager.addProjectTableRecipe(new ProjectTableRecipe(id, source, outputList, label, ingredients));

         */
    }

    public CraftTweakerProjectTableRecipeContext setId(String id) {
        this.id = id;
        return this;
    }

    public CraftTweakerProjectTableRecipeContext setSource(String source) {
        this.source = source;
        return this;
    }
}