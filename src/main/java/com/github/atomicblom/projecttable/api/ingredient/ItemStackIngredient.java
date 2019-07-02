/*
 * Copyright (c) 2014 Rosie Alexander and Scott Killen.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 */

package com.github.atomicblom.projecttable.api.ingredient;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.RegistryManager;

/**
 * An implementation of IIngredient that allows ItemStack instances to be submitted as ingredients.
 *
 * @author Scott Killen
 * @version 1.0
 * @see com.github.atomicblom.projecttable.api.ingredient.IIngredient
 * @since 0.1
 */
public class ItemStackIngredient implements IIngredient
{
    private final ItemStack itemStack;
    private Integer overriddenAmount = null;
    private int durabilityCost = 0;
    private boolean fluidContainer = false;

    /**
     * Class constructor specifying an ItemStack. The quantity consumed is deduced from the stackSize of the ItemStack.
     *
     * @param itemStack The ore name to use as an ingredient. This must not be <code>null</code> nor an empty stack. A
     *                  reference to itemStack is <i>not</i> maintained.
     */
    public ItemStackIngredient(ItemStack itemStack)
    {

        this.itemStack = itemStack.copy();
        if (itemStack.getCount() > 64) {
            overriddenAmount = itemStack.getCount();
            this.itemStack.setCount(1);
        }
    }

    /**
     * Returns a list of ItemStack aliases for this ingredient.
     *
     * @return A  list of ItemStack aliases for this ingredient.
     */
    @Override
    public ImmutableList<ItemStack> getItemStacks()
    {
        return ImmutableList.of(itemStack);
    }

    /**
     * Return the quantity of this ingredient to be consumed on a successful use.
     *
     * @return The quantity of this ingredient to be consumed on a successful use.
     */
    @Override
    public int getQuantityConsumed()
    {
        return overriddenAmount != null ? overriddenAmount : itemStack.getCount();
    }

    @Override
    public void setDurabilityCost(int durabilityCost) {

        this.durabilityCost = durabilityCost;
    }

    @Override
    public int getDurabilityCost() {
        return this.durabilityCost;
    }

    @Override
    public void setFluidContainer(boolean fluidContainer) {

        this.fluidContainer = fluidContainer;
    }

    @Override
    public boolean isFluidContainer() {
        return this.fluidContainer;
    }

    @Override
    public void assertValid(String id, String source) throws InvalidIngredientException {
        if (!RegistryManager.ACTIVE.getRegistry(Item.class).containsValue(this.itemStack.getItem()) || this.itemStack.isEmpty()) {
            throw new InvalidIngredientException(id, source, "Invalid ItemStack: " + this.itemStack.toString());
        }
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("itemStack", itemStack)
                .add("quantityConsumed", itemStack.getCount())
                .toString();
    }

    public ItemStack getItemStack()
    {
        return itemStack.copy();
    }

    public void overrideAmountConsumed(int count) {
        overriddenAmount = count;
    }
}