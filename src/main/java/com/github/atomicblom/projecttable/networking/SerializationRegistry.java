package com.github.atomicblom.projecttable.networking;

import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.google.common.collect.Maps;

import java.util.Map;

public enum SerializationRegistry
{
    INSTANCE;

    private final Map<String, IIngredientSerializer> ingredientSerializers = Maps.newHashMap();


    public void addSerializer(Class<? extends IIngredient> ingredientClass, IIngredientSerializer serializer)
    {
        ingredientSerializers.put(ingredientClass.getName(), serializer);
    }

    public IIngredientSerializer getSerializer(String name)
    {
        return ingredientSerializers.get(name);
    }
}