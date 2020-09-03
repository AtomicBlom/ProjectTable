package com.github.atomicblom.projecttable;

import com.github.atomicblom.projecttable.api.ProjectTableInitializedEvent;
import com.github.atomicblom.projecttable.api.ingredient.IngredientProblem;
import com.github.atomicblom.projecttable.client.ProjectTableGui;
import com.github.atomicblom.projecttable.client.api.InvalidRecipeException;
import com.github.atomicblom.projecttable.crafting.CraftingManager;
import com.github.atomicblom.projecttable.library.ContainerTypeLibrary;
import com.github.atomicblom.projecttable.networking.ProjectTableCraftPacket;
import com.github.atomicblom.projecttable.networking.ReplaceProjectTableRecipesPacket;
import com.github.atomicblom.projecttable.registration.ModCrafting;
import com.google.common.collect.Lists;
import com.mojang.bridge.game.GameVersion;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.MinecraftVersion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.StartupMessageManager;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Mod(ProjectTableMod.MODID)
public class ProjectTableMod
{
    public static final String MODID = "projecttable";
    public static final String NAME = "Project Table";
    public static final String VERSION = "1.0";

    public static Logger logger = LogManager.getLogger(MODID);
    public static boolean IS_CI_BUILD = false;
    public static boolean USE_DOT_TWO_GUI = false;

    public ProjectTableMod() {
        if (Boolean.getBoolean("@IS_CI_BUILD@")) {
            IS_CI_BUILD = true;
        }

        GameVersion gameVersion = MinecraftVersion.GAME_VERSION;
        USE_DOT_TWO_GUI = gameVersion.getReleaseTarget().equals("1.16.2");
        instance = this;
        final FMLJavaModLoadingContext javaModLoadingContext = FMLJavaModLoadingContext.get();
        IEventBus eventBus = javaModLoadingContext.getModEventBus();
        eventBus.addListener(this::setup);
        eventBus.addListener(this::onIMCEvent);
        eventBus.addListener(this::onEnqueueIMCEvent);
        eventBus.addListener(this::onClientSetup);
        eventBus.register(ProjectTableConfig.class);

        final ModLoadingContext modLoadingContext1 = ModLoadingContext.get();
        modLoadingContext1.registerConfig(ModConfig.Type.COMMON, ProjectTableConfig.commonSpec);
    }

    public static ProjectTableMod instance = null;
    public static SimpleChannel network;

    public void setup(final FMLCommonSetupEvent event)
    {
        int packetId = 0;
        network = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MODID, "main"),
                () -> ProjectTableMod.VERSION,
                ProjectTableMod.VERSION::equals, // TODO: Probably should let same versions match each other
                ProjectTableMod.VERSION::equals
        );
        network.registerMessage(packetId++, ReplaceProjectTableRecipesPacket.class, ReplaceProjectTableRecipesPacket::serialize, ReplaceProjectTableRecipesPacket::deserialize, ReplaceProjectTableRecipesPacket::received);
        network.registerMessage(packetId++, ProjectTableCraftPacket.class, ProjectTableCraftPacket::serialize, ProjectTableCraftPacket::deserialize, ProjectTableCraftPacket::received);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        logger.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
        ScreenManager.registerFactory(ContainerTypeLibrary.projectTableContainer, ProjectTableGui::new);
    }

    public void onEnqueueIMCEvent(InterModEnqueueEvent event) {
        ModCrafting.onProjectTableInitialized(new ProjectTableInitializedEvent(CraftingManager.INSTANCE));
    }

    public void onIMCEvent(InterModProcessEvent event)
    {
        StartupMessageManager.addModMessage("Project Table Recipes");
        try {
            List<InterModComms.IMCMessage> messages = event
                    .getIMCStream()
                    .filter(message -> "ProjectTableRecipe".equalsIgnoreCase(message.getMethod()))
                    .collect(Collectors.toList());

            List<IngredientProblem> ingredient = Lists.newArrayList();
            for (InterModComms.IMCMessage message : messages) {
                try {
                    Object o = message.getMessageSupplier().get();
                    if (!(o instanceof CompoundNBT)) continue;

                    CompoundNBT nbt = (CompoundNBT)o;
                    if (!nbt.contains("source")) {
                        nbt.putString("source", "imc:" + message.getSenderModId());
                    }

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
            StartupMessageManager.addModMessage("Completed Project Table Recipes");
        }
    }
}
