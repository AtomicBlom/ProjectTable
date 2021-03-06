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

package com.github.atomicblom.projecttable.crafting;

import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ICraftingManager;
import com.github.atomicblom.projecttable.api.ICraftingManagerIngredientsOrLabel;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.api.ingredient.ItemStackIngredient;
import com.github.atomicblom.projecttable.api.ingredient.OreDictionaryIngredient;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.networking.SerializationRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 * The class that accesses the singleton crafting managers..
 *
 * @author Scott Killen
 * @version 1.0
 * @since 0.1
 */
public enum CraftingManager implements ICraftingManager
{
    INSTANCE;

    public final ProjectTableManager projectTableManager = ProjectTableManager.INSTANCE;
    public final SerializationRegistry serializationRegistry = SerializationRegistry.INSTANCE;

    @Override
    public ICraftingManager registerInventorySerializer(Class<? extends IIngredient> ingredientClass, IIngredientSerializer serializer) {
        serializationRegistry.addSerializer(ingredientClass, serializer);
        return this;
    }

    @Override
    public ICraftingManagerIngredientsOrLabel addProjectTableRecipe(String modId, String recipeId) {
        return new ProjectTableRecipeContext(this, projectTableManager)
                .setId(recipeId)
                .setSource("mod:" + modId);
    }

    public void addFromNBT(NBTTagCompound nbtValue) {
        if (!nbtValue.hasKey("id", Constants.NBT.TAG_STRING)) {
            throw new ProjectTableException("NBT did not contain an 'id' tag: " + nbtValue.toString());
        }

        if (!nbtValue.hasKey("ingredients", Constants.NBT.TAG_LIST)) {
            throw new ProjectTableException("NBT did not contain an 'ingredients' tag: " + nbtValue.toString());
        }

        ProjectTableRecipeContext context = new ProjectTableRecipeContext(this, projectTableManager)
                .setId(nbtValue.getString("id"))
                .setSource(nbtValue.getString("source"));

        if (nbtValue.hasKey("label", Constants.NBT.TAG_STRING)) {
            context.withLabel(nbtValue.getString("label"));
        }

        IIngredient[] ingredientList;
        if (nbtValue.hasKey("ingredients", Constants.NBT.TAG_LIST)) {
            NBTTagList ingredients = nbtValue.getTagList("ingredients", Constants.NBT.TAG_COMPOUND);
            ingredientList = new IIngredient[ingredients.tagCount()];
            for (int i = 0; i < ingredients.tagCount(); i++) {
                NBTTagCompound ingredient = (NBTTagCompound)ingredients.get(i);
                ingredientList[i] = getIngredientFromNBT(ingredient);
            }
        } else if (nbtValue.hasKey("ingredients", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound ingredient = nbtValue.getCompoundTag("ingredients");
            ingredientList = new IIngredient[1];
            ingredientList[0] = getIngredientFromNBT(ingredient);
        } else {
            throw new ProjectTableException("NBT had a malformed 'ingredients' tag: " + nbtValue.toString());
        }

        if (ingredientList.length == 0) {
            throw new ProjectTableException("NBT was missing ingredients: " + nbtValue.toString());
        }

        context.withIngredients(ingredientList);

        if (nbtValue.hasKey("crafts", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound crafts = nbtValue.getCompoundTag("crafts");
            context.crafts(new ItemStack(crafts));
        } else if (nbtValue.hasKey("crafts", Constants.NBT.TAG_LIST)) {
            NBTTagList crafts = nbtValue.getTagList("crafts", Constants.NBT.TAG_COMPOUND);
            ItemStack[] outputs = new ItemStack[crafts.tagCount()];
            for (int item = 0; item < crafts.tagCount(); item++) {
                outputs[item] = new ItemStack(crafts.getCompoundTagAt(item));
            }
            if (outputs.length == 0) {
                throw new ProjectTableException("NBT was missing craft outputs: " + nbtValue.toString());
            }
            context.crafts(outputs);
        } else {
            throw new ProjectTableException("NBT had a malformed 'crafts' tag: " + nbtValue.toString());
        }
    }

    private IIngredient getIngredientFromNBT(NBTTagCompound ingredient) {
        int count = ingredient.getInteger("Count");
        if (count < 0) count = 0;
        if (ingredient.getBoolean("tool")) count = 0;

        IIngredient result;

        if (ingredient.hasKey("id", Constants.NBT.TAG_STRING)) {
            // Item stacks can't have a count > 64 or it gets defaulted to 0 and becomes empty.
            ingredient.setInteger("Count", 1);
            ItemStackIngredient itemStackIngredient = new ItemStackIngredient(new ItemStack(ingredient));
            if (ingredient.hasKey("Count")) {
                itemStackIngredient.overrideAmountConsumed(count);
            }
            result = itemStackIngredient;
        } else if (ingredient.hasKey("oredict", Constants.NBT.TAG_STRING)) {
            // Ore Dictionary ingredients aren't based on itemstacks and aren't subject to the 64 item limit.
            result = new OreDictionaryIngredient(
                    ingredient.getString("oredict"),
                    count
            );
        } else {
            throw new ProjectTableException("Unexpected ingredient tag type: " + ingredient.toString());
        }

        if (ingredient.hasKey("durabilityCost")) {
            result.setDurabilityCost(ingredient.getInteger("durabilityCost"));
        }

        if (ingredient.hasKey("fluidContainer")) {
            result.setFluidContainer(ingredient.getBoolean("fluidContainer"));
        }

        return result;
    }
}
