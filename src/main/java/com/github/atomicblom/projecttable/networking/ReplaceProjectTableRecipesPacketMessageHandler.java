package com.github.atomicblom.projecttable.networking;


import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.api.ingredient.InvalidIngredientException;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collection;

public class ReplaceProjectTableRecipesPacketMessageHandler implements IMessageHandler<ReplaceProjectTableRecipesPacket, IMessage>
{
    @Override
    public IMessage onMessage(final ReplaceProjectTableRecipesPacket message, final MessageContext ctx)
    {
        final Collection<ProjectTableRecipe> recipe = message.getRecipes();

        ProjectTableMod.logger.info("Replacing client recipe list from server");
        ProjectTableManager.INSTANCE.clearRecipes();
        boolean hasError = false;
        for (ProjectTableRecipe projectTableRecipe : recipe) {

            try {
                ProjectTableManager.INSTANCE.addProjectTableRecipe(projectTableRecipe, false);
            } catch (ProjectTableException | InvalidIngredientException e) {
                hasError = true;
                ProjectTableMod.logger.error(e.getMessage());
            }
        }
        if (hasError) {
            throw new ProjectTableException("Errors processing IMC based recipes");
        }

        return null;
    }
}