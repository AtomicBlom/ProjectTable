package com.github.atomicblom.projecttable.networking.serialization;

import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.api.ingredient.IIngredient;
import com.github.atomicblom.projecttable.api.ingredient.IIngredientSerializer;
import com.github.atomicblom.projecttable.api.ingredient.OreDictionaryIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Created by codew on 26/01/2016.
 */
public class OreDictionaryIngredientSerializer implements IIngredientSerializer
{
    @Override
    public IIngredient deserialize(PacketBuffer buffer)
    {
        final String oreDictionaryName = ByteBufUtils.readUTF8String(buffer);
        final int quantity = buffer.readInt();
        OreDictionaryIngredient oreDictionaryIngredient = new OreDictionaryIngredient(oreDictionaryName, quantity);
        oreDictionaryIngredient.setDurabilityCost(buffer.readInt());
        oreDictionaryIngredient.setFluidContainer(buffer.readBoolean());
        return oreDictionaryIngredient;
    }

    @Override
    public void serialize(IIngredient ingredient, PacketBuffer buffer)
    {
        if (!(ingredient instanceof OreDictionaryIngredient)) throw new ProjectTableException("Attempt to deserialize an ingredient that is not an OreDictionaryIngredient");
        final OreDictionaryIngredient oreDictionaryIngredient = (OreDictionaryIngredient) ingredient;

        buffer.writeString(oreDictionaryIngredient.getName())
                .writeInt(oreDictionaryIngredient.getQuantityConsumed())
                .writeInt(oreDictionaryIngredient.getDurabilityCost())
                .writeBoolean(oreDictionaryIngredient.isFluidContainer());
    }
}