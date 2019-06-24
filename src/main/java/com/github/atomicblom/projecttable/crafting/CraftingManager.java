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
import com.github.atomicblom.projecttable.api.IProjectTableManager;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.networking.SerializationRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
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

    public final IProjectTableManager projectTableManager = ProjectTableManager.INSTANCE;
    public final SerializationRegistry serializationRegistry = SerializationRegistry.INSTANCE;

    @Override
    public ICraftingManager registerInventorySerializer(Class<? extends IIngredient> ingredientClass, IIngredientSerializer serializer) {
        serializationRegistry.addSerializer(ingredientClass, serializer);
        return this;
    }

    @Override
    public ICraftingManagerIngredientsOrLabel addProjectTableRecipe() {
        return new ProjectTableRecipeContext(this, projectTableManager);
    }

    public void addFromNBT(NBTTagCompound nbtValue) {
        if (!nbtValue.hasKey("ingredients", Constants.NBT.TAG_LIST)) {
            throw new ProjectTableException("NBT did not contain an 'ingredients' tag");
        }

        ProjectTableRecipeContext context = new ProjectTableRecipeContext(this, projectTableManager);
        if (!nbtValue.hasKey("label", Constants.NBT.TAG_STRING)) {
            context.withLabel(nbtValue.getString("label"));
        }

        NBTTagList ingredients = nbtValue.getTagList("ingredients", Constants.NBT.TAG_COMPOUND);
        for (NBTBase ingredientBase : ingredients) {
            NBTTagCompound ingredient = (NBTTagCompound)ingredientBase;
            if (ingredient.hasKey("id", Constants.NBT.TAG_STRING)) {
                context.withIngredient(new ItemStack(ingredient));
            } else {
                throw new ProjectTableException("Unexpected ingredient tag type");
            }
        }

        if (nbtValue.hasKey("crafts", Constants.NBT.TAG_COMPOUND)) {

            NBTTagCompound crafts = nbtValue.getCompoundTag("crafts");
            context.crafts(new ItemStack(crafts));
        } else if (nbtValue.hasKey("crafts", Constants.NBT.TAG_LIST)) {
            NBTTagList crafts = nbtValue.getTagList("crafts", Constants.NBT.TAG_COMPOUND);
            ItemStack[] outputs = new ItemStack[crafts.tagCount()];
            for (int item = 0; item < crafts.tagCount(); item++) {
                outputs[item] = new ItemStack(crafts.getCompoundTagAt(item));
            }
            context.crafts(outputs);
        }
    }
}