package com.github.atomicblom.projecttable.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
public interface ICraftingManagerIngredientsOrLabel extends ICraftingManagerIngredients
{
    @ZenMethod
    ICraftingManagerIngredients withLabel(String label);
}