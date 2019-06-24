package com.github.atomicblom.projecttable.api;

public interface ICraftingManagerIngredientsOrLabel extends ICraftingManagerIngredients
{
    ICraftingManagerIngredients withLabel(String label);
}