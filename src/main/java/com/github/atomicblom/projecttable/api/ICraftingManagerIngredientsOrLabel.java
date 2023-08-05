package com.github.atomicblom.projecttable.api;

import net.minecraft.network.chat.Component;

public interface ICraftingManagerIngredientsOrLabel extends ICraftingManagerIngredients
{
    ICraftingManagerIngredients withLabel(Component label);
}