package com.github.atomicblom.projecttable.api;

import net.minecraft.util.text.ITextComponent;

public interface ICraftingManagerIngredientsOrLabel extends ICraftingManagerIngredients
{
    ICraftingManagerIngredients withLabel(ITextComponent label);
}