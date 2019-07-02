package com.github.atomicblom.projecttable.registration;


import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.api.ICraftingManager;
import com.github.atomicblom.projecttable.api.ProjectTableInitializedEvent;
import com.github.atomicblom.projecttable.api.ingredient.ItemStackIngredient;
import com.github.atomicblom.projecttable.api.ingredient.OreDictionaryIngredient;
import com.github.atomicblom.projecttable.networking.serialization.ItemStackIngredientSerializer;
import com.github.atomicblom.projecttable.networking.serialization.OreDictionaryIngredientSerializer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
public class ModCrafting
{
    @SubscribeEvent
    public static void onProjectTableInitialized(ProjectTableInitializedEvent event) {
        final ICraftingManager craftingManager = event.getCraftingManager();
        craftingManager
                .registerInventorySerializer(OreDictionaryIngredient.class, new OreDictionaryIngredientSerializer())
                .registerInventorySerializer(ItemStackIngredient.class, new ItemStackIngredientSerializer());

        readRecipesFromNBTFile();
        readRecipesFromConfigDirectory();
        //readRecipesFromModAssets();

        //addExampleFluentRecipes(craftingManager);
        //addExampleNBTRecipes();
    }

    private static void readRecipesFromConfigDirectory() {
        String configDir = ProjectTableMod.instance.configDir;

        try {
            Path projectTable = Paths.get(configDir, "projectTable");
            if (Files.isDirectory(projectTable)) {
                Stream<Path> files = Files.walk(projectTable)
                        .filter(file -> Files.isRegularFile(file) && file.toString().endsWith(".json"));

                for (Path file : (Iterable<Path>)files::iterator) {
                    try {
                        String contents = new String(Files.readAllBytes(file));
                        NBTTagCompound tag = JsonToNBT.getTagFromJson(contents);
                        String source = "config:" + projectTable.relativize(file).toString();
                        if (!tag.hasKey("id", Constants.NBT.TAG_STRING)) {
                            tag.setString("id", source.replace(".json", ""));
                        }
                        tag.setString("source", source);
                        FMLInterModComms.sendMessage(ProjectTableMod.MODID, "ProjectTableRecipe", tag);
                    } catch (NBTException e) {
                        ProjectTableMod.logger.warn("Error parsing NBT projectTable: " + e.toString());
                    } catch (IOException e) {
                        ProjectTableMod.logger.warn("Error reading projectTable file: " + e.toString());
                    }
                }
            }
        } catch (IOException e) {
            ProjectTableMod.logger.warn("Error reading projectTable directory: " + e.toString());
        }
    }

    private static void readRecipesFromNBTFile() {
        String configDir = ProjectTableMod.instance.configDir;
        try {

            if (new File(configDir, "projectTable.nbt").canRead()) {
                NBTTagCompound nbtTagCompound = CompressedStreamTools.read(new File(configDir, "projectTable.nbt"));
                if (nbtTagCompound == null) {
                    ProjectTableMod.logger.warn("projectTable.nbt found, but had no elements inside");
                    return;
                }

                NBTTagList recipeList = nbtTagCompound.getTagList("recipe", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < recipeList.tagCount(); i++) {
                    NBTTagCompound recipeNbt = recipeList.getCompoundTagAt(i);
                    String source = "config:projectTable.nbt[" + i + "]";
                    if (!recipeNbt.hasKey("id", Constants.NBT.TAG_STRING)) {
                        recipeNbt.setString("id", source);
                    }
                    recipeNbt.setString("source", source);
                    FMLInterModComms.sendMessage(ProjectTableMod.MODID, "ProjectTableRecipe", recipeNbt);
                }
            }
        } catch (FileNotFoundException e) {
            ProjectTableMod.logger.warn("projectTable.nbt not found: " + e.toString());
        } catch (IOException e) {
            ProjectTableMod.logger.warn("Error reading projectTable.nbt: " + e.toString());
        }
    }

    private static void addExampleNBTRecipes() {
        NBTTagCompound imgRecipe = new NBTTagCompound();
        NBTTagList ingredientList = new NBTTagList();
        ingredientList.appendTag(new ItemStack(Blocks.PUMPKIN, 15).writeToNBT(new NBTTagCompound()));
        ingredientList.appendTag(new ItemStack(Blocks.DIRT, 15).writeToNBT(new NBTTagCompound()));
        imgRecipe.setTag("ingredients", ingredientList);
        NBTTagCompound craftOutput = new ItemStack(Items.WHEAT, 5).writeToNBT(new NBTTagCompound());
        imgRecipe.setTag("crafts", craftOutput);
        imgRecipe.setString("id", "testimc");
        FMLInterModComms.sendMessage(ProjectTableMod.MODID, "ProjectTableRecipe", imgRecipe);

        NBTTagCompound imcOreDictRecipe = new NBTTagCompound();
        NBTTagList ingredientList2 = new NBTTagList();
        NBTTagCompound oredict = new NBTTagCompound();
        oredict.setString("oredict", "logWood");
        oredict.setInteger("Count", 16);
        ingredientList2.appendTag(oredict);
        imcOreDictRecipe.setTag("ingredients", ingredientList2);
        NBTTagCompound craftOutput2 = new ItemStack(Blocks.PLANKS, 10).writeToNBT(new NBTTagCompound());
        imcOreDictRecipe.setTag("crafts", craftOutput2);
        imcOreDictRecipe.setString("id", "testoredictimc");
        FMLInterModComms.sendMessage(ProjectTableMod.MODID, "ProjectTableRecipe", imcOreDictRecipe);
    }

    private static void addExampleFluentRecipes(ICraftingManager craftingManager) {
        craftingManager
                .addProjectTableRecipe(ProjectTableMod.MODID, "testA")
                .withIngredient(Blocks.DIRT, 64 * 3)
                .crafts(Items.DIAMOND, 10)

                .addProjectTableRecipe(ProjectTableMod.MODID, "testB")
                .withIngredient(Blocks.GOLD_BLOCK, 64)
                .andIngredient(Blocks.GOLD_ORE, 64)
                .andIngredient(Blocks.BEACON, 64)
                .andIngredient(Blocks.BROWN_MUSHROOM_BLOCK, 64)
                .crafts(Items.GOLD_NUGGET)

                .addProjectTableRecipe(ProjectTableMod.MODID, "testC")
                .withIngredient(Blocks.DIRT, 2 * 64)
                .crafts(Items.DIAMOND)

                .addProjectTableRecipe(ProjectTableMod.MODID, "testD")
                .withIngredient(new OreDictionaryIngredient("plankWood", 64))
                .crafts(Blocks.ANVIL)

                .addProjectTableRecipe(ProjectTableMod.MODID, "testE")
                .withIngredient(Blocks.DIAMOND_BLOCK, 2 * 50)
                .andIngredient(Blocks.EMERALD_BLOCK, 2 * 49)
                .andIngredient(Items.ELYTRA, 64)
                .andIngredient(Items.CLAY_BALL, 1)
                .crafts(Items.CLAY_BALL)
        ;
    }
}