package com.github.atomicblom.projecttable;

import com.github.atomicblom.projecttable.api.ProjectTableInitializedEvent;
import com.github.atomicblom.projecttable.crafting.CraftingManager;
import com.github.atomicblom.projecttable.gui.GuiHandler;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacket;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacketMessageHandler;
import net.minecraft.nbt.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Mod(modid = ProjectTableMod.MODID, name = ProjectTableMod.NAME, version = ProjectTableMod.VERSION)
public class ProjectTableMod
{
    public static final String MODID = "projecttable";
    public static final String NAME = "Project Table";
    public static final String VERSION = "1.0";

    private static Logger logger;

    @Mod.Instance
    public static ProjectTableMod instance = null;
    public static SimpleNetworkWrapper network;

    private String configDir;


    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        configDir = event.getSuggestedConfigurationFile().getAbsoluteFile().getParent();

        logger = event.getModLog();
        this.network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        network.registerMessage(ProjectTableCraftPacketMessageHandler.class, ProjectTableCraftPacket.class, 0, Side.SERVER);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, GuiHandler.INSTANCE);
        ProjectTableInitializedEvent initializedEvent = new ProjectTableInitializedEvent(CraftingManager.INSTANCE);
        MinecraftForge.EVENT_BUS.post(initializedEvent);
        // Example via NBT.

        /*NBTTagCompound imgRecipe = new NBTTagCompound();
        NBTTagList ingredientList = new NBTTagList();
        ingredientList.appendTag(new ItemStack(Blocks.PUMPKIN, 15).writeToNBT(new NBTTagCompound()));
        ingredientList.appendTag(new ItemStack(Blocks.DIRT, 15).writeToNBT(new NBTTagCompound()));
        imgRecipe.setTag("ingredients", ingredientList);
        NBTTagCompound craftOutput = new ItemStack(Items.WHEAT, 5).writeToNBT(new NBTTagCompound());
        imgRecipe.setTag("crafts", craftOutput);
        imgRecipe.setString("id", "testimc");
        FMLInterModComms.sendMessage(MODID, "ProjectTableRecipe", imgRecipe);

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
        FMLInterModComms.sendMessage(MODID, "ProjectTableRecipe", imcOreDictRecipe);*/

        try {
            if (new File(configDir, "projectTable.nbt").canRead()) {
                NBTTagCompound nbtTagCompound = CompressedStreamTools.read(new File(configDir, "projectTable.nbt"));

                NBTTagList recipeList = nbtTagCompound.getTagList("recipe", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < recipeList.tagCount(); i++) {
                    NBTTagCompound recipeNbt = recipeList.getCompoundTagAt(i);
                    String source = "config:projectTable.nbt[" + i + "]";
                    if (!recipeNbt.hasKey("id", Constants.NBT.TAG_STRING)) {
                        recipeNbt.setString("id", source);
                    }
                    recipeNbt.setString("source", source);
                    FMLInterModComms.sendMessage(MODID, "ProjectTableRecipe", recipeNbt);
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("projectTable.nbt not found: " + e.toString());
        } catch (IOException e) {
            logger.warn("Error reading projectTable.nbt: " + e.toString());
        }
        try {
            Path projectTable = Paths.get(configDir, "projectTable");
            if (Files.isDirectory(projectTable)) {
                Stream<Path> files = Files.walk(projectTable)
                        .filter(file -> Files.isRegularFile(file) && file.getFileName().endsWith(".json"));

                for (Path file : (Iterable<Path>)files::iterator) {
                    String contents = new String(Files.readAllBytes(file));
                    NBTTagCompound tag = JsonToNBT.getTagFromJson(contents);
                    String source = "config:" + projectTable.relativize(file).toString().replace(".json", "");
                    if (!tag.hasKey("id", Constants.NBT.TAG_STRING)) {
                        tag.setString("id", source);
                    }
                    tag.setString("source", source);
                    FMLInterModComms.sendMessage(MODID, "ProjectTableRecipe", tag);
                }
            }
        } catch (NBTException e) {
            e.printStackTrace();
        } catch (IOException e) {
            logger.warn("Error reading projectTable.nbt: " + e.toString());
        }
    }

    @EventHandler
    public void onFMLPostInitialization(FMLInterModComms.IMCEvent event)
    {
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            if ("ProjectTableRecipe".equalsIgnoreCase(message.key) && message.isNBTMessage()) {
                try {
                    NBTTagCompound nbt = message.getNBTValue();
                    if (!nbt.hasKey("source")) {
                        nbt.setString("source", "imc:" + message.getSender());
                    }
                    CraftingManager.INSTANCE.addFromNBT(nbt);
                } catch (ProjectTableException e) {
                    logger.error("Unable to parse recipe", e);
                }
            }
        }
    }
}
