package com.github.atomicblom.projecttable.library;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemLibrary {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ProjectTableMod.MODID);

    public static final RegistryObject<Item> project_table = ITEMS.register(
            Reference.Block.PROJECT_TABLE.getPath(),
            () -> configureBlockItem(BlockLibrary.project_table)
    );

    static <B extends Block> BlockItem configureBlockItem(RegistryObject<B> block) {

        BlockItem itemBlock = new BlockItem(block.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC));
        final ResourceLocation registryName = block.getId();
        assert registryName != null;
        return itemBlock;
    }
}
