package com.github.atomicblom.projecttable.api.ingredient;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import java.util.stream.Collectors;

public class ItemTagIngredient implements IIngredient {
    private final ResourceLocation tagName;
    private final int amount;
    private int durabilityCost;
    private boolean fluidContainer;

    public ItemTagIngredient(ResourceLocation tagName, int amount) {
        this.tagName = tagName;
        this.amount = amount;
    }

    public ItemTagIngredient(ITag.INamedTag<Item> tag, int amount) {
        this.tagName = tag.getName();
        this.amount = amount;
    }

    @Override
    public ImmutableList<ItemStack> getItemStacks() {

        final ITag<Item> itemITag = ItemTags.getCollection().get(tagName);
        if (itemITag == null) return ImmutableList.of();
        return itemITag
                .getAllElements()
                .stream()
                .map(i -> new ItemStack(i, 1))
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
        final ITag<Item> itemITag = ItemTags.getCollection().get(tagName);
        if (itemITag == null) {
            return new IngredientProblem(id, source, "Invalid Item Tag: " + this.tagName.toString());
        }
        return null;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("tag", tagName)
                .add("quantityConsumed", getQuantityConsumed())
                .toString();
    }

    public ResourceLocation getTagName() {
        return tagName;
    }
}