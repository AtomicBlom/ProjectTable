package com.github.atomicblom.projecttable.registration;


import com.github.atomicblom.projecttable.api.ICraftingManager;
import com.github.atomicblom.projecttable.api.ProjectTableInitializedEvent;
import com.github.atomicblom.projecttable.api.ingredient.ItemStackIngredient;
import com.github.atomicblom.projecttable.api.ingredient.OreDictionaryIngredient;
import com.github.atomicblom.projecttable.networking.serialization.ItemStackIngredientSerializer;
import com.github.atomicblom.projecttable.networking.serialization.OreDictionaryIngredientSerializer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModCrafting
{
    @SubscribeEvent
    public static void onProjectTableInitialized(ProjectTableInitializedEvent event) {
        final ICraftingManager craftingManager = event.getCraftingManager();
        craftingManager
                .registerInventorySerializer(OreDictionaryIngredient.class, new OreDictionaryIngredientSerializer())
                .registerInventorySerializer(ItemStackIngredient.class, new ItemStackIngredientSerializer())

                /*.addProjectTableRecipe()
                    .withIngredient(Blocks.DIRT, 64 * 3)
                    .crafts(Items.DIAMOND, 10)

                .addProjectTableRecipe()
                    .withIngredient(Blocks.GOLD_BLOCK, 64)
                    .andIngredient(Blocks.GOLD_ORE, 64)
                    .andIngredient(Blocks.BEACON, 64)
                    .andIngredient(Blocks.BROWN_MUSHROOM_BLOCK, 64)
                    .crafts(Items.GOLD_NUGGET)

                .addProjectTableRecipe()
                    .withIngredient(Blocks.DIRT, 2 * 64)
                    .crafts(Items.DIAMOND)

                .addProjectTableRecipe()
                    .withIngredient(new OreDictionaryIngredient("plankWood", 64))
                    .crafts(Blocks.ANVIL)

                .addProjectTableRecipe()
                    .withIngredient(Blocks.DIAMOND_BLOCK, 2 * 50)
                    .andIngredient(Blocks.EMERALD_BLOCK, 2 * 49)
                    .andIngredient(Items.ELYTRA, 64)
                    .andIngredient(Items.CLAY_BALL, 1)
                    .crafts(Items.CLAY_BALL)*/
                ;
    }
}