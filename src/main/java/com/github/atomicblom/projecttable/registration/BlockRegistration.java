package com.github.atomicblom.projecttable.registration;

import com.github.atomicblom.projecttable.Reference;
import com.github.atomicblom.projecttable.block.ProjectTableBlock;
import com.github.atomicblom.projecttable.library.BlockLibrary;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class BlockRegistration {
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(configure(new ProjectTableBlock(), Reference.Block.PROJECT_TABLE));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(configureBlockItem(BlockLibrary.project_table));
    }

    static <B extends Block> B configure(B block, ResourceLocation registryName) {
        block.setRegistryName(registryName)
                .setTranslationKey(registryName.toString())
                .setCreativeTab(CreativeTabs.MISC);

        return block;
    }

    static <B extends Block> ItemBlock configureBlockItem(B block) {

        ItemBlock itemBlock = new ItemBlock(block);
        itemBlock.setRegistryName(block.getRegistryName())
                .setTranslationKey(block.getTranslationKey())
                .setCreativeTab(CreativeTabs.MISC);

        return itemBlock;
    }
}
