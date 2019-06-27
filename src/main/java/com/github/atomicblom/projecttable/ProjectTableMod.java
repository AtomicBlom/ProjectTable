package com.github.atomicblom.projecttable;

import com.github.atomicblom.projecttable.api.ProjectTableInitializedEvent;
import com.github.atomicblom.projecttable.api.ingredient.InvalidIngredientException;
import com.github.atomicblom.projecttable.crafting.CraftingManager;
import com.github.atomicblom.projecttable.gui.GuiHandler;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacket;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacketMessageHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = ProjectTableMod.MODID, name = ProjectTableMod.NAME, version = ProjectTableMod.VERSION)
public class ProjectTableMod
{
    public static final String MODID = "projecttable";
    public static final String NAME = "Project Table";
    public static final String VERSION = "1.0";

    public static Logger logger;

    @Mod.Instance
    public static ProjectTableMod instance = null;
    public static SimpleNetworkWrapper network;

    public String configDir;


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
    }

    @EventHandler
    public void onIMCEvent(FMLInterModComms.IMCEvent event)
    {
        boolean hasError = false;
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            if ("ProjectTableRecipe".equalsIgnoreCase(message.key) && message.isNBTMessage()) {
                try {
                    NBTTagCompound nbt = message.getNBTValue();
                    if (!nbt.hasKey("source")) {
                        nbt.setString("source", "imc:" + message.getSender());
                    }
                    CraftingManager.INSTANCE.addFromNBT(nbt);
                } catch (ProjectTableException | InvalidIngredientException e) {
                    hasError = true;
                    logger.error(e.getMessage());
                }
            }
        }
        if (hasError) {
            throw new ProjectTableException("Errors processing IMC based recipes");
        }
    }
}
