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
import com.github.atomicblom.projecttable.api.ingredient.*;
import com.github.atomicblom.projecttable.client.api.InvalidRecipeException;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.networking.SerializationRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;

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

    public void addFromNBT(CompoundNBT nbtValue) {
        if (!nbtValue.contains("id", Constants.NBT.TAG_STRING)) {
            throw new ProjectTableException("NBT did not contain an 'id' tag: " + nbtValue.toString());
        }

        if (!nbtValue.contains("ingredients", Constants.NBT.TAG_LIST)) {
            throw new ProjectTableException("NBT did not contain an 'ingredients' tag: " + nbtValue.toString());
        }

        ProjectTableRecipeContext context = new ProjectTableRecipeContext(this, projectTableManager)
                .setId(nbtValue.getString("id"))
                .setSource(nbtValue.getString("source"));

        if (nbtValue.contains("label", Constants.NBT.TAG_STRING)) {
            context.withLabel(new StringTextComponent(nbtValue.getString("label")));
        }

        IIngredient[] ingredientList;
        if (nbtValue.contains("ingredients", Constants.NBT.TAG_LIST)) {
            ListNBT ingredients = nbtValue.getList("ingredients", Constants.NBT.TAG_COMPOUND);
            ingredientList = new IIngredient[ingredients.size()];
            for (int i = 0; i < ingredients.size(); i++) {
                CompoundNBT ingredient = (CompoundNBT)ingredients.get(i);
                ingredientList[i] = getIngredientFromNBT(ingredient);
            }
        } else if (nbtValue.contains("ingredients", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT ingredient = nbtValue.getCompound("ingredients");
            ingredientList = new IIngredient[1];
            ingredientList[0] = getIngredientFromNBT(ingredient);
        } else {
            throw new ProjectTableException("NBT had a malformed 'ingredients' tag: " + nbtValue.toString());
        }

        if (ingredientList.length == 0) {
            throw new ProjectTableException("NBT was missing ingredients: " + nbtValue.toString());
        }

        context.withIngredients(ingredientList);

        if (nbtValue.contains("crafts", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT crafts = nbtValue.getCompound("crafts");
            context.crafts(ItemStack.read(crafts));
        } else if (nbtValue.contains("crafts", Constants.NBT.TAG_LIST)) {
            ListNBT crafts = nbtValue.getList("crafts", Constants.NBT.TAG_COMPOUND);
            ItemStack[] outputs = new ItemStack[crafts.size()];
            for (int item = 0; item < crafts.size(); item++) {
                outputs[item] = ItemStack.read(crafts.getCompound(item));
            }
            if (outputs.length == 0) {
                throw new ProjectTableException("NBT was missing craft outputs: " + nbtValue.toString());
            }
            context.crafts(outputs);
        } else {
            throw new ProjectTableException("NBT had a malformed 'crafts' tag: " + nbtValue.toString());
        }
    }

    private IIngredient getIngredientFromNBT(CompoundNBT ingredient) {
        int count = ingredient.getInt("Count");
        if (count < 0) count = 0;
        if (ingredient.getBoolean("tool")) count = 0;

        IIngredient result;

        if (ingredient.contains("id", Constants.NBT.TAG_STRING)) {
            // Item stacks can't have a count > 64 or it gets defaulted to 0 and becomes empty.
            ingredient.putInt("Count", 1);
            ItemStackIngredient itemStackIngredient = new ItemStackIngredient(ItemStack.read(ingredient));
            if (ingredient.contains("Count")) {
                itemStackIngredient.overrideAmountConsumed(count);
            }
            result = itemStackIngredient;
        }
        else if (ingredient.contains("itemTag", Constants.NBT.TAG_STRING)) {
            // Tag ingredients aren't based on item stacks and aren't subject to the 64 item limit.
            result = new ItemTagIngredient(
                    new ResourceLocation(ingredient.getString("itemTag")),
                    count
            );
        }
        else if (ingredient.contains("blockTag", Constants.NBT.TAG_STRING)) {
            // Tag ingredients aren't based on item stacks and aren't subject to the 64 item limit.
            result = new BlockTagIngredient(
                    new ResourceLocation(ingredient.getString("blockTag")),
                    count
            );
        }
        else if (ingredient.contains("compound", Constants.NBT.TAG_LIST)) {
            ListNBT childIngredients = ingredient.getList("compound", Constants.NBT.TAG_COMPOUND);
            IIngredient[] ingredientList = new IIngredient[childIngredients.size()];
            for (int i = 0; i < childIngredients.size(); i++) {
                ingredientList[i] = getIngredientFromNBT(childIngredients.getCompound(i));
            }
            result = new CompositeIngredient(count, ingredientList);
        }
        else {
            throw new InvalidRecipeException("Unexpected ingredient tag type: " + ingredient.toString(), new ArrayList<>());
        }

        if (ingredient.contains("durabilityCost")) {
            result.setDurabilityCost(ingredient.getInt("durabilityCost"));
        }

        if (ingredient.contains("fluidContainer")) {
            result.setFluidContainer(ingredient.getBoolean("fluidContainer"));
        }

        return result;
    }
}
