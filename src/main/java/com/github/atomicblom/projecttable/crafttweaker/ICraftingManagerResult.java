package com.github.atomicblom.projecttable.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
public interface ICraftingManagerResult
{
    @ZenMethod
    void crafts(IIngredient... outputs);
}