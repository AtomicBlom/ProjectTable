package com.github.atomicblom.projecttable.registration;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.client.opengex.OpenGEXModelLoader;
import com.github.atomicblom.projecttable.library.BlockLibrary;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientRegistration {
    @SubscribeEvent
    public static void onRenderingReady(ModelRegistryEvent event) {
        OpenGEXModelLoader.INSTANCE.addDomain(ProjectTableMod.MODID);
        ModelLoaderRegistry.registerLoader(OpenGEXModelLoader.INSTANCE);

        registerBlockItemModel(BlockLibrary.project_table);
    }

    private static void registerBlockItemModel(Block block) {
        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(block),
                0,
                new ModelResourceLocation(block.getRegistryName(), "inventory")
        );
    }

}
