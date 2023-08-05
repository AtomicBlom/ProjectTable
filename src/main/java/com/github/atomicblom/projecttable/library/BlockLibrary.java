package com.github.atomicblom.projecttable.library;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.Reference;
import com.github.atomicblom.projecttable.block.ProjectTableBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockLibrary {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ProjectTableMod.MODID);

    public static final RegistryObject<Block> project_table = BLOCKS.register(Reference.Block.PROJECT_TABLE.getPath(),
            () ->
                    new ProjectTableBlock(BlockBehaviour.Properties.of(Material.WOOD, DyeColor.BROWN)
                            .strength(1)
    ));

}
