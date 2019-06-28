package com.github.atomicblom.projecttable.client.model;

import com.github.atomicblom.projecttable.crafting.ProjectTableRecipe;

/**
 * Created by codew on 30/01/2016.
 */
public class ProjectTableRecipeInstance
{

    private final ProjectTableRecipe recipe;
    private boolean canCraft;

    public ProjectTableRecipeInstance(ProjectTableRecipe recipe)
    {

        this.recipe = recipe;
    }

    public ProjectTableRecipe getRecipe()
    {
        return recipe;
    }

    public void setCanCraft(boolean canCraft)
    {
        this.canCraft = canCraft;
    }

    public boolean canCraft()
    {
        return canCraft;
    }
}