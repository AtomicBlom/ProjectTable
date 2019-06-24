package com.github.atomicblom.projecttable;

import com.github.atomicblom.projecttable.api.ProjectTableInitializedEvent;
import com.github.atomicblom.projecttable.crafting.CraftingManager;
import com.github.atomicblom.projecttable.gui.GuiHandler;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacket;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacketMessageHandler;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

        NBTTagCompound exampleRecipe = new NBTTagCompound();
        NBTTagList ingredientList = new NBTTagList();
        ingredientList.appendTag(new ItemStack(Blocks.PUMPKIN, 15).writeToNBT(new NBTTagCompound()));
        ingredientList.appendTag(new ItemStack(Blocks.DIRT, 15).writeToNBT(new NBTTagCompound()));
        exampleRecipe.setTag("ingredients", ingredientList);
        NBTTagCompound craftOutput = new ItemStack(Items.WHEAT, 5).writeToNBT(new NBTTagCompound());
        exampleRecipe.setTag("crafts", craftOutput);
        FMLInterModComms.sendMessage(MODID, "ProjectTableRecipe", exampleRecipe);

        try {
            NBTTagCompound nbtTagCompound = CompressedStreamTools.read(new File(configDir, "projectTable.nbt"));
            for (NBTBase recipes : nbtTagCompound.getTagList("recipes", Constants.NBT.TAG_COMPOUND)) {
                FMLInterModComms.sendMessage(MODID, "ProjectTableRecipe", (NBTTagCompound)recipes);
            }
        } catch (FileNotFoundException e) {
            com.github.atomicblom.projecttable.Logger.warning("projectTable.nbt not found: " + e.toString());
        } catch (IOException e) {
            com.github.atomicblom.projecttable.Logger.warning("Error reading projectTable.nbt: " + e.toString());
        }
    }

    @EventHandler
    public void onFMLPostInitialization(FMLInterModComms.IMCEvent event)
    {
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            if ("ProjectTableRecipe".equalsIgnoreCase(message.key) && message.isNBTMessage()) {
                CraftingManager.INSTANCE.addFromNBT(message.getNBTValue());
            }
        }
    }
}
