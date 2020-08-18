package com.github.atomicblom.projecttable.registration;

import com.github.atomicblom.projecttable.Reference;
import com.github.atomicblom.projecttable.block.ProjectTableBlock;
import com.github.atomicblom.projecttable.library.BlockLibrary;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class BlockRegistration {
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(
                new ProjectTableBlock(AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                        .harvestLevel(1)
                        .harvestTool(ToolType.AXE)
                ).setRegistryName(Reference.Block.PROJECT_TABLE)
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(configureBlockItem(BlockLibrary.project_table));
    }

    static <B extends Block> BlockItem configureBlockItem(B block) {

        BlockItem itemBlock = new BlockItem(block, new Item.Properties().group(ItemGroup.MISC));
        itemBlock.setRegistryName(block.getRegistryName());
        return itemBlock;
    }
}
