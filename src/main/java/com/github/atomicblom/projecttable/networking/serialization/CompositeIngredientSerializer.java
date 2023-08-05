package com.github.atomicblom.projecttable.networking.serialization;


import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ingredient.CompositeIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.networking.SerializationRegistry;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Created by codew on 26/01/2016.
 */
public class CompositeIngredientSerializer implements IIngredientSerializer
{
    @Override
    public IIngredient deserialize(FriendlyByteBuf buffer)
    {
        int compositeIngredientCount = buffer.readInt();
        IIngredient[] childIngredients = new IIngredient[compositeIngredientCount];
        for (int i = 0; i < compositeIngredientCount; i++) {
            String serializerName = buffer.readUtf(32767);
            IIngredientSerializer serializer = SerializationRegistry.INSTANCE.getSerializer(serializerName);
            childIngredients[i] = serializer.deserialize(buffer);
        }

        int amount = buffer.readInt();
        CompositeIngredient compositeIngredient = new CompositeIngredient(amount, childIngredients);
        compositeIngredient.setDurabilityCost(buffer.readInt());
        compositeIngredient.setFluidContainer(buffer.readBoolean());
        return compositeIngredient;
    }

    @Override
    public void serialize(IIngredient ingredient, FriendlyByteBuf buffer)
    {
        if (!(ingredient instanceof CompositeIngredient)) throw new ProjectTableException("Attempt to deserialize an ingredient that is not an CompositeIngredient");
        final CompositeIngredient compositeIngredient = (CompositeIngredient) ingredient;

        final IIngredient[] childIngredients = compositeIngredient.getChildIngredients();

        buffer.writeInt(childIngredients.length);
        for (IIngredient childIngredient : childIngredients) {
            final String name = childIngredient.getClass().getName();
            buffer.writeUtf(name);
            IIngredientSerializer serializer = SerializationRegistry.INSTANCE.getSerializer(name);
            serializer.serialize(childIngredient, buffer);
        }

        buffer.writeInt(compositeIngredient.getQuantityConsumed())
            .writeInt(compositeIngredient.getDurabilityCost())
            .writeBoolean(compositeIngredient.isFluidContainer());
    }
}