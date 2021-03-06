package com.github.atomicblom.projecttable.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import org.jline.utils.Log;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Blatantly stolen from Mezz
//https://github.com/mezz/JustEnoughItems/blob/1.8.9/src/main/java/mezz/jei/util/StackUtil.java

public class ItemStackUtils
{
    /**
     * Returns all the subtypes of itemStack if it has a wildcard meta value.
     */
    @Nonnull
    public static List<ItemStack> getSubtypes(@Nonnull ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item == null) {
            return Collections.emptyList();
        }

        if (itemStack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
            return Collections.singletonList(itemStack);
        }

        return getSubtypes(item, itemStack.getCount());
    }

    @Nonnull
    public static List<ItemStack> getSubtypes(@Nonnull Item item, int stackSize) {
        List<ItemStack> itemStacks = new ArrayList<>();

        for (CreativeTabs itemTab : item.getCreativeTabs()) {
            NonNullList<ItemStack> subItems = NonNullList.create();
            item.getSubItems(itemTab, subItems);
            for (ItemStack subItem : subItems) {
                if (subItem.getCount() != stackSize) {
                    ItemStack subItemCopy = subItem.copy();
                    subItemCopy.setCount(stackSize);
                    itemStacks.add(subItemCopy);
                } else {
                    itemStacks.add(subItem);
                }
            }
        }

        return itemStacks;
    }

    public static List<ItemStack> getAllSubtypes(Iterable stacks) {
        List<ItemStack> allSubtypes = new ArrayList<>();
        getAllSubtypes(allSubtypes, stacks);
        return allSubtypes;
    }

    private static void getAllSubtypes(List<ItemStack> subtypesList, Iterable stacks) {
        for (Object obj : stacks) {
            if (obj instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) obj;
                List<ItemStack> subtypes = getSubtypes(itemStack);
                subtypesList.addAll(subtypes);
            } else if (obj instanceof Iterable) {
                getAllSubtypes(subtypesList, (Iterable) obj);
            } else if (obj != null) {
                Log.error("Unknown object found: {}", obj);
            }
        }
    }
}