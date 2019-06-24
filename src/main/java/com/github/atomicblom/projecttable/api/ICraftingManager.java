package com.github.atomicblom.projecttable.api;

import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;

/**
 * Created by codew on 26/01/2016.
 */
public interface ICraftingManager
{
    ICraftingManager registerInventorySerializer(Class<? extends IIngredient> inventoryClass, IIngredientSerializer serializer);
    ICraftingManagerIngredientsOrLabel addProjectTableRecipe();
}
