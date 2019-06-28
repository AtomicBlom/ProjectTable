package com.github.atomicblom.projecttable.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Created by codew on 9/04/2016.
 */
@ZenRegister
public interface ICraftingManagerIngredients
{
    @ZenMethod
    ICraftingManagerIngredientOrResult withIngredient(IIngredient ingredient);
    @ZenMethod
    ICraftingManagerIngredientOrResult withIngredients(IIngredient... ingredients);
}