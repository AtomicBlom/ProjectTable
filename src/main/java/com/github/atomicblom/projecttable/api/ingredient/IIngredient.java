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

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

/**
 * The IIngredient is implemented to submit recipes to Steam and Steel machines.
 *
 * @author Scott Killen
 * @version 1.0
 * @see com.github.atomicblom.projecttable.api.ingredient.ItemStackIngredient
 * @see com.github.atomicblom.projecttable.api.ingredient.OreDictionaryIngredient
 * @since 0.1
 */
public interface IIngredient
{
    /**
     * Returns a list of ItemStack aliases for this ingredient.
     *
     * @return A  list of ItemStack aliases for this ingredient.
     */
    ImmutableList<ItemStack> getItemStacks();

    /**
     * Returns the quantity of this ingredient to be consumed on a successful use.
     *
     * @return The quantity of this ingredient to be consumed on a successful use.
     */
    int getQuantityConsumed();

    /**
     * Crafting with this IIngredient will reduce the durability of the itemstack by this amount
     * @param durabilityCost the durability to remove from the item
     */
    void setDurabilityCost(int durabilityCost);

    int getDurabilityCost();

    /**
     * Sets the ingredient to consume the fluid and return it's container
     * @param fluidContainer if true, the container will be returned
     */
    void setFluidContainer(boolean fluidContainer);

    boolean isFluidContainer();

    void assertValid(String id, String source);
}