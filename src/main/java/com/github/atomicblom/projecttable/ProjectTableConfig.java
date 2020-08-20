package com.github.atomicblom.projecttable;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ProjectTableConfig {
    public static class Common {
        public final ForgeConfigSpec.BooleanValue loadCraftingTableRecipes;
        public final ForgeConfigSpec.BooleanValue useExampleVanillaRecipes;
        public final ForgeConfigSpec.BooleanValue useDevRecipes;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Server configuration settings")
                    .push("server");

            // TODO: add support for reading from the crafting table as well.
            loadCraftingTableRecipes = builder
                    .comment("Causes any recipes from the normal crafting table to be made available in the table")
                    .translation("projecttable.configgui.loadCraftingTableRecipes")
                    .worldRestart()
                    .define("loadCraftingTableRecipes", false);


            useExampleVanillaRecipes = builder
                    .comment("Loads an example set of bulk recipes appropriate for Vanilla")
                    .translation("projecttable.configgui.useExampleVanillaRecipes")
                    .worldRestart()
                    .define("useExampleVanillaRecipes", true);

            if (!ProjectTableMod.IS_CI_BUILD) {
                useDevRecipes = builder
                        .comment("Exercises all the various ways a mod can add recipes to the mod.")
                        .translation("projecttable.configgui.useDevRecipes")
                        .worldRestart()
                        .define("useDevRecipes", true);
            } else {
                useDevRecipes = null;
            }

            builder.pop();
        }
    }

    static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static File CONFIG_DIR;

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        ProjectTableMod.logger.debug("Loaded project table config file {}", configEvent.getConfig().getFileName());
        CONFIG_DIR =  configEvent.getConfig().getFullPath().getParent().toFile();
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
        ProjectTableMod.logger.debug("Forge config just got changed on the file system!");
    }
}
