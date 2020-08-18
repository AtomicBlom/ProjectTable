package com.github.atomicblom.projecttable.networking;

import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.api.ingredient.IngredientProblem;
import com.github.atomicblom.projecttable.client.api.InvalidRecipeException;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ReplaceProjectTableRecipesPacket
{
    private final Collection<ProjectTableRecipe> recipes;

    public ReplaceProjectTableRecipesPacket(Collection<ProjectTableRecipe> recipes)
    {
        this.recipes = recipes;
    }

    public Collection<ProjectTableRecipe> getRecipes() {
        return recipes;
    }

    public void serialize(PacketBuffer buf)
    {
        Collection<ProjectTableRecipe> allRecipes = ProjectTableManager.INSTANCE.getRecipes();
        buf.writeInt(allRecipes.size());
        for (ProjectTableRecipe recipe : allRecipes) {
            recipe.writeToBuffer(new PacketBuffer(buf));
        }
    }

    public static ReplaceProjectTableRecipesPacket deserialize(PacketBuffer buf)
    {
        List<ProjectTableRecipe> replacementRecipes = Lists.newArrayList();
        int recipeCount = buf.readInt();
        for (int i = 0; i < recipeCount; i++) {
            replacementRecipes.add(ProjectTableRecipe.readFromBuffer(new PacketBuffer(buf)));
        }
        return new ReplaceProjectTableRecipesPacket(replacementRecipes);
    }

    public static void received(final ReplaceProjectTableRecipesPacket msg, final Supplier<NetworkEvent.Context> ctx)
    {
        final Collection<ProjectTableRecipe> recipe = msg.getRecipes();

        ProjectTableMod.logger.info("Replacing client recipe list from server");
        ProjectTableManager.INSTANCE.clearRecipes();
        List<IngredientProblem> ingredient = Lists.newArrayList();
        for (ProjectTableRecipe projectTableRecipe : recipe) {
            try {
                ProjectTableManager.INSTANCE.addProjectTableRecipe(projectTableRecipe, false, true);
            } catch (InvalidRecipeException e) {
                ingredient.addAll(e.getProblems());
            }
        }

        ProjectTableMod.logger.info("Duplicating mod recipes");

        if (!ingredient.isEmpty()) {
            throw new ProjectTableException("Errors processing IMC based recipes:\n" +
                    ingredient.stream()
                            .map(i -> String.format("%s@%s: %s", i.getSource(),i.getId(), i.getMessage()))
                            .collect(Collectors.joining("\n"))
            );
        }
    }
}
