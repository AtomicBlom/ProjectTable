package com.github.atomicblom.projecttable.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
public interface ICraftingManagerIngredientOrResult extends ICraftingManagerResult
{
    @ZenMethod
    ICraftingManagerIngredientOrResult andIngredient(IIngredient ingredient);
    @ZenMethod
    ICraftingManagerIngredientOrResult andIngredients(IIngredient... ingredients);
}