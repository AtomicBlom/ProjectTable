package com.github.atomicblom.projecttable.client.api;

import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.networking.FriendlyByteBufExtensions;
import com.github.atomicblom.projecttable.networking.SerializationRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by codew on 6/01/2016.
 */
public class ProjectTableRecipe
{
    ImmutableList<ItemStack> output;
    private final String source;
    ImmutableList<IIngredient> input;
    private Component displayName;
    private String renderText;
    private final String id;

    public ProjectTableRecipe(String id, String source, Collection<ItemStack> output, Component displayName, Collection<IIngredient> input)
    {
        this.id = id;
        this.source = source;
        this.input = ImmutableList.copyOf(input);
        this.displayName = displayName;
        this.output = ImmutableList.copyOf(output);
    }

    public ProjectTableRecipe(String id, String source, ItemStack output, Collection<IIngredient> input)
    {
        this(id, source, Lists.newArrayList(output), output.getDisplayName(), input);
    }

    public ProjectTableRecipe(String id, String source, ItemStack output, IIngredient... input)
    {
        this(id, source, Lists.newArrayList(output), output.getDisplayName(), Arrays.asList(input));
    }


    public ImmutableList<ItemStack> getOutput()
    {
        return output;
    }

    public ImmutableList<IIngredient> getInput()
    {
        return input;
    }

    public Component getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(Component displayName)
    {
        this.displayName = displayName;
    }

    public String getRenderText()
    {
        return renderText;
    }

    public void setRenderText(String renderText)
    {
        this.renderText = renderText;
    }

    public static ProjectTableRecipe readFromBuffer(FriendlyByteBuf buf)
    {
        String id = buf.readUtf(255);
        String source = buf.readUtf(255);

        byte inputItemStackCount = buf.readByte();
        List<IIngredient> input = Lists.newArrayList();
        for (int i = 0; i < inputItemStackCount; ++i)
        {
            input.add(readIngredient(buf));
        }

        byte outputItemStackCount = buf.readByte();
        List<ItemStack> output = Lists.newArrayList();
        for (int i = 0; i < outputItemStackCount; ++i)
        {
            output.add(FriendlyByteBufExtensions.readLargeItemStackFromBuffer(buf));
        }

        final Component displayName = EntityDataSerializers.COMPONENT.read(buf);
        return new ProjectTableRecipe(id, source, output, displayName, input);
    }

    private static IIngredient readIngredient(FriendlyByteBuf buf) {
        final String ingredientType = EntityDataSerializers.STRING.read(buf);
        final IIngredientSerializer serializer = SerializationRegistry.INSTANCE.getSerializer(ingredientType);
        if (serializer == null) {
            throw new ProjectTableException("Unknown Ingredient serializer: " + ingredientType);
        }
        return serializer.deserialize(buf);
    }

    public void writeToBuffer(FriendlyByteBuf buf)
    {
        buf.writeUtf(id);
        buf.writeUtf(source);

        buf.writeByte(input.size());
        for (final IIngredient itemStack : input)
        {
            writeIngredient(itemStack, buf);
        }
        buf.writeByte(output.size());
        for (final ItemStack itemStack : output)
        {
            FriendlyByteBufExtensions.writeLargeItemStackToBuffer(buf, itemStack);
        }

        EntityDataSerializers.COMPONENT.write(buf, displayName);
    }

    private void writeIngredient(IIngredient ingredient, FriendlyByteBuf buf)
    {
        final String name = ingredient.getClass().getName();
        buf.writeUtf(name);
        final IIngredientSerializer serializer = SerializationRegistry.INSTANCE.getSerializer(ingredient.getClass().getName());
        if (serializer == null) {
            throw new ProjectTableException("Unknown Ingredient serializer: " + ingredient.getClass().getName());
        }
        serializer.serialize(ingredient, buf);
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }
}