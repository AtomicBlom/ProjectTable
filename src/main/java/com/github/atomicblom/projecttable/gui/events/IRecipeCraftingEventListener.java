package com.github.atomicblom.projecttable.gui.events;

import com.github.atomicblom.projecttable.crafting.ProjectTableRecipe;

/**
 * Created by codew on 16/01/2016.
 */
public interface IRecipeCraftingEventListener
{
    void onRecipeCrafting(ProjectTableRecipe recipe);
}