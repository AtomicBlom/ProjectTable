package com.github.atomicblom.projecttable.networking;


import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.api.ingredient.IngredientProblem;
import com.github.atomicblom.projecttable.client.api.InvalidRecipeException;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ReplaceProjectTableRecipesPacketMessageHandler implements IMessageHandler<ReplaceProjectTableRecipesPacket, IMessage>
{
    @Override
    public IMessage onMessage(final ReplaceProjectTableRecipesPacket message, final MessageContext ctx)
    {
        final Collection<ProjectTableRecipe> recipe = message.getRecipes();

        ProjectTableMod.logger.info("Replacing client recipe list from server");
        ProjectTableManager.INSTANCE.clearRecipes();
        List<IngredientProblem> ingredient = Lists.newArrayList();
        for (ProjectTableRecipe projectTableRecipe : recipe) {
            try {
                ProjectTableManager.INSTANCE.addProjectTableRecipe(projectTableRecipe, false);
            } catch (InvalidRecipeException e) {
                ingredient.addAll(e.getProblems());
            }
        }

        if (!ingredient.isEmpty()) {
            throw new ProjectTableException("Errors processing IMC based recipes:\n" +
                    ingredient.stream()
                            .map(i -> String.format("%s@%s: %s", i.getSource(),i.getId(), i.getMessage()))
                            .collect(Collectors.joining("\n"))
            );
        }

        return null;
    }
}