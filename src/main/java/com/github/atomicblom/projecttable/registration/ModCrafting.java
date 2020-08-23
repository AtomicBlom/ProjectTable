package com.github.atomicblom.projecttable.registration;

import com.github.atomicblom.projecttable.ProjectTableConfig;
import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.api.ICraftingManager;
import com.github.atomicblom.projecttable.api.ProjectTableInitializedEvent;
import com.github.atomicblom.projecttable.api.ingredient.BlockTagIngredient;
import com.github.atomicblom.projecttable.api.ingredient.CompositeIngredient;
import com.github.atomicblom.projecttable.api.ingredient.ItemStackIngredient;
import com.github.atomicblom.projecttable.api.ingredient.ItemTagIngredient;
import com.github.atomicblom.projecttable.networking.serialization.BlockTagIngredientSerializer;
import com.github.atomicblom.projecttable.networking.serialization.CompositeIngredientSerializer;
import com.github.atomicblom.projecttable.networking.serialization.ItemStackIngredientSerializer;
import com.github.atomicblom.projecttable.networking.serialization.ItemTagIngredientSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Mod.EventBusSubscriber()
public class ModCrafting
{
    @SubscribeEvent
    public static void onProjectTableInitialized(ProjectTableInitializedEvent event) {
        final ICraftingManager craftingManager = event.getCraftingManager();
        craftingManager
                .registerInventorySerializer(ItemTagIngredient.class, new ItemTagIngredientSerializer())
                .registerInventorySerializer(BlockTagIngredient.class, new BlockTagIngredientSerializer())
                .registerInventorySerializer(CompositeIngredient.class, new CompositeIngredientSerializer())
                .registerInventorySerializer(ItemStackIngredient.class, new ItemStackIngredientSerializer());

        readRecipesFromConfigDirectory();
        readRecipesFromNBTFile();

        if (ProjectTableConfig.COMMON.useExampleVanillaRecipes.get()) {
            addExampleFluentRecipes(craftingManager);
        }

        if (ProjectTableConfig.COMMON.useDevRecipes != null && ProjectTableConfig.COMMON.useDevRecipes.get()) {
            addExampleNBTRecipes();
        }

        //readRecipesFromDatapacks();
    }

    public static void readRecipesFromConfigDirectory() {
        try {
            Path projectTable = Paths.get(ProjectTableConfig.CONFIG_DIR.getAbsolutePath(), "projectTable");
            if (Files.isDirectory(projectTable)) {
                Stream<Path> files = Files.walk(projectTable)
                        .filter(file -> Files.isRegularFile(file) && file.toString().endsWith(".json"));

                for (Path file : (Iterable<Path>)files::iterator) {
                    try {
                        String contents = new String(Files.readAllBytes(file));
                        CompoundNBT tag = JsonToNBT.getTagFromJson(contents);
                        String source = "config:" + projectTable.relativize(file).toString();
                        if (!tag.contains("id", Constants.NBT.TAG_STRING)) {
                            tag.putString("id", source.replace(".json", ""));
                        }
                        tag.putString("source", source);
                        InterModComms.sendTo(ProjectTableMod.MODID, "ProjectTableRecipe", () -> tag);
                    } catch (CommandSyntaxException e) {
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

    public static void readRecipesFromNBTFile() {
        try {

            if (new File(ProjectTableConfig.CONFIG_DIR, "projectTable.nbt").canRead()) {
                CompoundNBT nbtTagCompound = CompressedStreamTools.read(new File(ProjectTableConfig.CONFIG_DIR, "projectTable.nbt"));
                if (nbtTagCompound == null) {
                    ProjectTableMod.logger.warn("projectTable.nbt found, but had no elements inside");
                    return;
                }

                ListNBT recipeList = nbtTagCompound.getList("recipe", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < recipeList.size(); i++) {
                    CompoundNBT recipeNbt = recipeList.getCompound(i);
                    String source = "config:projectTable.nbt[" + i + "]";
                    if (!recipeNbt.contains("id", Constants.NBT.TAG_STRING)) {
                        recipeNbt.putString("id", source);
                    }
                    recipeNbt.putString("source", source);
                    InterModComms.sendTo(ProjectTableMod.MODID, "ProjectTableRecipe", () -> recipeNbt);
                }
            }
        } catch (FileNotFoundException e) {
            ProjectTableMod.logger.warn("projectTable.nbt not found: " + e.toString());
        } catch (IOException e) {
            ProjectTableMod.logger.warn("Error reading projectTable.nbt: " + e.toString());
        }
    }

    private static void addExampleNBTRecipes() {
        CompoundNBT imgRecipe = new CompoundNBT();
        ListNBT ingredientList = new ListNBT();
        ingredientList.add(new ItemStack(Blocks.PUMPKIN, 15).write(new CompoundNBT()));
        ingredientList.add(new ItemStack(Blocks.DIRT, 15).write(new CompoundNBT()));

        CompoundNBT compoundIngredient = new CompoundNBT();
        compoundIngredient.putInt("Count", 10);
        ListNBT switchIngredients = new ListNBT();
        switchIngredients.add(new ItemStack(Blocks.ACACIA_FENCE).write(new CompoundNBT()));
        switchIngredients.add(new ItemStack(Blocks.ACACIA_FENCE_GATE).write(new CompoundNBT()));
        compoundIngredient.put("compound", switchIngredients);

        ingredientList.add(compoundIngredient);
        imgRecipe.put("ingredients", ingredientList);
        CompoundNBT craftOutput = new ItemStack(Items.WHEAT, 5).write(new CompoundNBT());
        imgRecipe.put("crafts", craftOutput);
        imgRecipe.putString("id", "testimc");
        InterModComms.sendTo(ProjectTableMod.MODID, "ProjectTableRecipe", () -> imgRecipe);

        CompoundNBT imcOreDictRecipe = new CompoundNBT();
        ListNBT ingredientList2 = new ListNBT();
        CompoundNBT itemTag = new CompoundNBT();
        itemTag.putString("itemTag", "minecraft:planks");
        itemTag.putInt("Count", 16);
        ingredientList2.add(itemTag);
        imcOreDictRecipe.put("ingredients", ingredientList2);
        CompoundNBT craftOutput2 = new ItemStack(Blocks.OAK_PLANKS, 10).write(new CompoundNBT());
        imcOreDictRecipe.put("crafts", craftOutput2);
        imcOreDictRecipe.putString("id", "testoredictimc");

        InterModComms.sendTo(ProjectTableMod.MODID, "ProjectTableRecipe", () -> imcOreDictRecipe);
    }

    private static void addExampleFluentRecipes(ICraftingManager craftingManager) {
        craftingManager
                .addProjectTableRecipe(ProjectTableMod.MODID, "Torches")
                    .withIngredient(Items.STICK, 16)
                    .andIngredient(
                        new CompositeIngredient(16,
                            new ItemStackIngredient(new ItemStack(Items.COAL)),
                            new ItemStackIngredient(new ItemStack(Items.CHARCOAL))
                        )
                    )
                    .crafts(Items.TORCH, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "SoulTorches")
                    .withIngredient(Items.STICK, 16)
                    .andIngredient(
                        new CompositeIngredient(16,
                            new ItemStackIngredient(new ItemStack(Items.COAL)),
                            new ItemStackIngredient(new ItemStack(Items.CHARCOAL))
                        )
                    )
                    .andIngredient(Blocks.SOUL_SAND, 16)
                    .crafts(Items.SOUL_TORCH, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "RedstoneTorches")
                    .withIngredient(Items.STICK, 64)
                    .andIngredient(Items.REDSTONE, 64)
                    .crafts(Items.REDSTONE_TORCH, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "Dispensers")
                    .withIngredient(Items.COBBLESTONE, 56)
                    .andIngredient(Items.REDSTONE, 8)
                    .andIngredient(Items.BOW, 8)
                    .crafts(Items.DISPENSER, 8)

                .addProjectTableRecipe(ProjectTableMod.MODID, "DispensersFromRaw")
                    .withIngredient(Items.COBBLESTONE, 448)
                    .andIngredient(Items.REDSTONE, 64)
                    .andIngredient(Items.STICK, 192)
                    .andIngredient(Items.STRING, 192)
                    .crafts(Items.DISPENSER, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "TNT")
                    .withIngredient(Blocks.SAND, 256)
                    .andIngredient(Items.GUNPOWDER, 320)
                    .crafts(Items.TNT, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "PoweredRails")
                    .withIngredient(Items.STICK, 10)
                    .andIngredient(Items.GOLD_INGOT, 6)
                    .andIngredient(Items.REDSTONE, 10)
                    .crafts(Items.POWERED_RAIL, 60)

                .addProjectTableRecipe(ProjectTableMod.MODID, "DetectorRails")
                    .withIngredient(Items.STONE_PRESSURE_PLATE, 10)
                    .andIngredient(Items.IRON_INGOT, 6)
                    .andIngredient(Items.REDSTONE, 10)
                    .crafts(Items.DETECTOR_RAIL, 60)

                .addProjectTableRecipe(ProjectTableMod.MODID, "ActivatorRails")
                    .withIngredient(Items.STICK, 20)
                    .andIngredient(Items.IRON_INGOT, 6)
                    .andIngredient(Items.REDSTONE_TORCH, 10)
                    .crafts(Items.ACTIVATOR_RAIL, 60)

                .addProjectTableRecipe(ProjectTableMod.MODID, "Rails")
                    .withIngredient(Items.STICK, 4)
                    .andIngredient(Items.IRON_INGOT, 24)
                    .crafts(Items.RAIL, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "ConcretePowders")
                    .withIngredient(new ItemTagIngredient(new ResourceLocation("forge:dyes/white"), 8))
                    .andIngredient(Blocks.SAND, 32)
                    .andIngredient(Blocks.GRAVEL, 32)
                    .crafts(Items.WHITE_CONCRETE_POWDER, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "Levers")
                    .withIngredient(Items.STICK, 64)
                    .andIngredient(Blocks.COBBLESTONE, 64)
                    .crafts(Items.LEVER, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "Arrows")
                    .withIngredient(Items.FLINT, 16)
                    .andIngredient(Items.STICK, 16)
                    .andIngredient(Items.FEATHER, 16)
                    .crafts(Items.ARROW, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "SpectralArrowsFromArrows")
                    .withIngredient(Items.ARROW, 32)
                    .andIngredient(Blocks.GLOWSTONE, 128)
                    .crafts(Items.SPECTRAL_ARROW, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "SpectralArrowsFromRaw")
                    .withIngredient(Items.FLINT, 8)
                    .andIngredient(Items.STICK, 8)
                    .andIngredient(Items.FEATHER, 8)
                    .andIngredient(Blocks.GLOWSTONE, 128)
                    .crafts(Items.SPECTRAL_ARROW, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "Pistons")
                    .withIngredient(new ItemTagIngredient(ItemTags.PLANKS, 192))
                    .andIngredient(Blocks.COBBLESTONE, 256)
                    .andIngredient(Items.IRON_INGOT, 64)
                    .andIngredient(Items.REDSTONE, 64)
                    .crafts(Items.PISTON, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "StickyPistons")
                    .withIngredient(new BlockTagIngredient(new ResourceLocation("minecraft:planks"), 192))
                    .andIngredient(Blocks.COBBLESTONE, 256)
                    .andIngredient(Items.IRON_INGOT, 64)
                    .andIngredient(Items.REDSTONE, 64) //FIXME: TAGS?
                    .andIngredient(Items.SLIME_BALL, 64)
                    .crafts(Items.STICKY_PISTON, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "ItemFrames")
                    .withIngredient(Items.LEATHER, 512)
                    .andIngredient(Items.STICK, 64)
                    .crafts(Items.ITEM_FRAME, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "PumpkinPies")
                    .withIngredient(Blocks.PUMPKIN, 64)
                    .andIngredient(Items.SUGAR, 64)
                    .andIngredient(Items.EGG, 64)
                    .crafts(Items.PUMPKIN_PIE, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "Cake")
                    .withIngredient(Items.MILK_BUCKET, 3)
                    .andIngredient(Items.SUGAR, 2)
                    .andIngredient(Items.EGG, 1)
                    .andIngredient(Items.WHEAT, 3)
                    .crafts(Items.CAKE)

                .addProjectTableRecipe(ProjectTableMod.MODID, "Cookies")
                    .withIngredient(Items.COCOA_BEANS, 8)
                    .andIngredient(Items.WHEAT, 16)
                    .crafts(Items.COOKIE, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "Hoppers")
                    .withIngredient(new BlockTagIngredient(new ResourceLocation("forge:chests/wooden"), 64))
                    .andIngredient(Items.IRON_INGOT, 320)
                    .crafts(Items.HOPPER, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "HoppersFromRaw")
                    .withIngredient(new BlockTagIngredient(new ResourceLocation("minecraft:planks"), 512))
                    .andIngredient(Items.IRON_INGOT, 320)
                    .crafts(Items.HOPPER, 64)

                .addProjectTableRecipe(ProjectTableMod.MODID, "Bookshelves")
                    .withIngredient(new BlockTagIngredient(new ResourceLocation("minecraft:planks"), 24))
                    .andIngredient(Items.BOOK, 12)
                    .crafts(Items.BOOKSHELF, 4)

                .addProjectTableRecipe(ProjectTableMod.MODID, "BookshelvesFromRaw")
                    .withIngredient(new BlockTagIngredient(new ResourceLocation("minecraft:planks"), 24))
                    .andIngredient(Items.LEATHER, 12)
                    .andIngredient(Items.PAPER, 36)
                    .crafts(Items.BOOKSHELF, 4)
        ;
    }
}