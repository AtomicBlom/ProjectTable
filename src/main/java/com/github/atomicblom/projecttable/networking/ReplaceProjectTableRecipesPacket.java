package com.github.atomicblom.projecttable.networking;

import com.github.atomicblom.projecttable.ProjectTableConfig;
import com.github.atomicblom.projecttable.ProjectTableException;
import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.api.ingredient.*;
import com.github.atomicblom.projecttable.client.api.InvalidRecipeException;
import com.github.atomicblom.projecttable.client.api.ProjectTableManager;
import com.github.atomicblom.projecttable.client.api.ProjectTableRecipe;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReplaceProjectTableRecipesPacket
{
    private final Collection<ProjectTableRecipe> recipes;
    private final boolean includeVanillaRecipes;

    public ReplaceProjectTableRecipesPacket(Collection<ProjectTableRecipe> recipes, boolean includeVanillaRecipes)
    {
        this.recipes = recipes;
        this.includeVanillaRecipes = includeVanillaRecipes;
    }

    public Collection<ProjectTableRecipe> getRecipes() {
        return recipes;
    }
    public boolean shouldIncludeVanillaRecipes() { return includeVanillaRecipes; }

    public void serialize(FriendlyByteBuf buf)
    {
        Collection<ProjectTableRecipe> allRecipes = ProjectTableManager.INSTANCE.getRecipes();
        buf.writeInt(allRecipes.size());
        for (ProjectTableRecipe recipe : allRecipes) {
            recipe.writeToBuffer(new FriendlyByteBuf(buf));
        }
        buf.writeBoolean(ProjectTableConfig.COMMON.loadCraftingTableRecipes.get());
    }

    public static ReplaceProjectTableRecipesPacket deserialize(FriendlyByteBuf buf)
    {
        List<ProjectTableRecipe> replacementRecipes = Lists.newArrayList();
        int recipeCount = buf.readInt();
        for (int i = 0; i < recipeCount; i++) {
            replacementRecipes.add(ProjectTableRecipe.readFromBuffer(new FriendlyByteBuf(buf)));
        }
        boolean includeVanillaRecipes = buf.readBoolean();
        return new ReplaceProjectTableRecipesPacket(replacementRecipes, includeVanillaRecipes);
    }

    public static void received(final ReplaceProjectTableRecipesPacket msg, final Supplier<NetworkEvent.Context> ctx)
    {
        ProjectTableMod.logger.info("Replacing client recipe list from server");
        final Collection<ProjectTableRecipe> recipe = msg.getRecipes();
        final NetworkEvent.Context context = ctx.get();
        context.setPacketHandled(true);        context.enqueueWork(() -> {

            ProjectTableManager.INSTANCE.clearRecipes();
            List<IngredientProblem> badRecipes = Lists.newArrayList();
            for (ProjectTableRecipe projectTableRecipe : recipe) {
                try {
                    ProjectTableManager.INSTANCE.addProjectTableRecipe(projectTableRecipe, false, true);
                } catch (InvalidRecipeException e) {
                    badRecipes.addAll(e.getProblems());
                }
            }


            if (msg.shouldIncludeVanillaRecipes()) {

                final Minecraft instance = Minecraft.getInstance();
                final LocalPlayer player = instance.player;
                final ClientPacketListener connection = instance.getConnection();

                assert player != null;
                assert connection != null;

                CraftingContainer tempCraftingInventory = new CraftingContainer(player.containerMenu, 3, 3);

                ProjectTableMod.logger.info("Duplicating mod recipes");

                //noinspection unchecked
                connection
                        .getRecipeManager()
                        .getRecipes()
                        .parallelStream()
                        .filter(r -> !r.isSpecial() && r.getType() == RecipeType.CRAFTING)
                        .map(r ->
                                (Recipe<CraftingContainer>)r
                        )
                        .map(r -> {
                            try {
                                final Stream<ItemStack> returnedItems = Stream.concat(
                                        Stream.of(r.getResultItem()),
                                        r.getRemainingItems(tempCraftingInventory).stream()
                                );
                                final Stream<Ingredient> ingredients = r.getIngredients().stream();
                                return new ProjectTableRecipe(
                                        r.getId().toString(),
                                        "CRAFTING_TABLE",
                                        returnedItems
                                                .filter(i -> !i.isEmpty())
                                                .collect(Collectors.toList()),
                                        r.getResultItem().getHoverName(),
                                        ingredients
                                                .filter(Ingredient::isSimple)
                                                .map(Ingredient::toJson)
                                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                                                .entrySet().stream()
                                                .map(i -> ReplaceProjectTableRecipesPacket.toProjectTableIngredient(i.getKey(), i.getValue().intValue()))
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toList())
                                );
                            } catch (Exception e) {
                                String name = r != null ? r.getId() != null ? r.getId().toString() : null : null;
                                if (name == null) name = "RECIPE WITH NO ID!";
                                return new ProjectTableRecipe(name, "CRAFTING_TABLE", ItemStack.EMPTY, new ItemStackIngredient(ItemStack.EMPTY));
                            }
                        })
                        .forEach(r -> {
                            try {
                                ProjectTableManager.INSTANCE.addProjectTableRecipe(r, false, true);
                            } catch (InvalidRecipeException e) {
                                badRecipes.addAll(e.getProblems());
                            }
                        });
            }

            if (!badRecipes.isEmpty()) {
                throw new ProjectTableException("Errors processing IMC based recipes:\n" +
                        badRecipes.stream()
                                .map(i -> String.format("%s@%s: %s", i.getSource(),i.getId(), i.getMessage()))
                                .collect(Collectors.joining("\n"))
                );
            }
        });
    }

    private static IIngredient toProjectTableIngredient(JsonElement serializedIngredient, int count) {
        if (serializedIngredient.isJsonArray()) {
            final JsonArray compositeIngredients = serializedIngredient.getAsJsonArray();
            if (compositeIngredients.size() == 0) {
                return null;
            }
            IIngredient[] compositeIngredientList = new IIngredient[compositeIngredients.size()];
            for (int index = 0; index < compositeIngredients.size(); index++) {
                compositeIngredientList[index] = getIngredientFromJsonElement(compositeIngredients.get(index), 1);
            }
            return new CompositeIngredient(count, compositeIngredientList);
        }
        return getIngredientFromJsonElement(serializedIngredient, count);
    }

    private static IIngredient getIngredientFromJsonElement(JsonElement serializedIngredient, int count) {
        final JsonObject serialize = serializedIngredient.getAsJsonObject();
        if (serialize.has("item")) {
            @SuppressWarnings("deprecation")
            Item item = Registry.ITEM.get(new ResourceLocation(serialize.get("item").getAsString()));
            final ItemStack itemStack = new ItemStack(item, count);
            if (itemStack.isEmpty()) return null;
            return new ItemStackIngredient(itemStack);
        } else if (serialize.has("tag")) {
            return new ItemTagIngredient(ForgeRegistries.ITEMS.tags().createTagKey(new ResourceLocation(serialize.get("tag").getAsString())), count);
        }

        return null;
    }
}
