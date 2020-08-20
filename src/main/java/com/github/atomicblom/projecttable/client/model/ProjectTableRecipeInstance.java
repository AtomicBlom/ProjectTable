package com.github.atomicblom.projecttable.client.model;

import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;

/**
 * Created by codew on 30/01/2016.
 */
public class ProjectTableRecipeInstance
{

    private final ProjectTableRecipe recipe;
    private final String recipeName;
    private boolean canCraft;
    private boolean isLocked;

    public ProjectTableRecipeInstance(ProjectTableRecipe recipe)
    {
        this.recipe = recipe;
        this.recipeName = recipe != null ? recipe.getDisplayName().getString() : "";
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

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public String getRecipeName() {
        return recipeName;
    }
}