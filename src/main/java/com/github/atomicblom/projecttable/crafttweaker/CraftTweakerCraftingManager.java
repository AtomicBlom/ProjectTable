package com.github.atomicblom.projecttable.crafttweaker;

import com.github.atomicblom.projecttable.crafting.CraftingManager;
import com.github.atomicblom.projecttable.crafting.ProjectTableManager;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.projecttable.recipes")
public class CraftTweakerCraftingManager {
    @ZenMethod("addProjectTableRecipe")
    public static ICraftingManagerIngredientsOrLabel addCraftTweakerProjectTableRecipe(String recipeId) {
        return new CraftTweakerProjectTableRecipeContext(CraftingManager.INSTANCE, ProjectTableManager.INSTANCE)
                .setId(recipeId)
                .setSource("crafttweaker");
    }
}
