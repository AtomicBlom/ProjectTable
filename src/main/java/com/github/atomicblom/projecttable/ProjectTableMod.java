package com.github.atomicblom.projecttable;

import com.github.atomicblom.projecttable.api.ProjectTableInitializedEvent;
import com.github.atomicblom.projecttable.api.ingredient.IngredientProblem;
import com.github.atomicblom.projecttable.client.api.InvalidRecipeException;
import com.github.atomicblom.projecttable.crafting.CraftingManager;
import com.github.atomicblom.projecttable.gui.GuiHandler;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacket;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacketMessageHandler;
import com.github.atomicblom.projecttable.networking.ReplaceProjectTableRecipesPacket;
import com.github.atomicblom.projecttable.networking.ReplaceProjectTableRecipesPacketMessageHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

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
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        network.registerMessage(ProjectTableCraftPacketMessageHandler.class, ProjectTableCraftPacket.class, 0, Side.SERVER);
        network.registerMessage(ReplaceProjectTableRecipesPacketMessageHandler.class, ReplaceProjectTableRecipesPacket.class, 1, Side.CLIENT);
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
        ProgressManager.ProgressBar progressBar = null;
        try {

            ImmutableList<FMLInterModComms.IMCMessage> allMessages = event.getMessages();
            List<FMLInterModComms.IMCMessage> messages = allMessages.stream().filter(message -> "ProjectTableRecipe".equalsIgnoreCase(message.key) && message.isNBTMessage()).collect(Collectors.toList());
            progressBar = ProgressManager.push("Project Table Recipes", messages.size());
            List<IngredientProblem> ingredient = Lists.newArrayList();
            for (FMLInterModComms.IMCMessage message : messages) {
                try {
                    NBTTagCompound nbt = message.getNBTValue();
                    if (!nbt.hasKey("source")) {
                        nbt.setString("source", "imc:" + message.getSender());
                    }
                    progressBar.step(nbt.getString("source"));

                    CraftingManager.INSTANCE.addFromNBT(nbt);
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

        } finally {
            if (progressBar != null) {
                ProgressManager.pop(progressBar);
            }
        }
    }
}
