package com.github.atomicblom.projecttable.api.ingredient;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CompositeIngredient implements IIngredient {
    private final int amount;
    private final IIngredient[] childIngredients;
    private int durabilityCost;
    private boolean fluidContainer;

    public CompositeIngredient(int amount, IIngredient... ingredients) {
        this.childIngredients = ingredients;
        this.amount = amount;
    }

    @Override
    public ImmutableList<ItemStack> getItemStacks() {
        return Arrays.stream(childIngredients)
                .flatMap(i -> i.getItemStacks().stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    @Override
    public int getQuantityConsumed() {
        return amount;
    }

    @Override
    public void setDurabilityCost(int durabilityCost) {
        this.durabilityCost = durabilityCost;
    }

    @Override
    public int getDurabilityCost() {
        return durabilityCost;
    }

    @Override
    public void setFluidContainer(boolean fluidContainer) {
        this.fluidContainer = fluidContainer;
    }

    @Override
    public boolean isFluidContainer() {
        return fluidContainer;
    }

    @Override
    public IngredientProblem assertValid(String id, String source) {
        for (IIngredient childIngredient : childIngredients) {
            IngredientProblem problem = childIngredient.assertValid(id, source);
            if (problem != null) return problem;
        }
        return null;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("childIngredients", childIngredients)
                .add("quantityConsumed", getQuantityConsumed())
                .toString();
    }

    public IIngredient[] getChildIngredients() {
        return childIngredients;
    }
}
