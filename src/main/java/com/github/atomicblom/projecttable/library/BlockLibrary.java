package com.github.atomicblom.projecttable.library;

import com.github.atomicblom.projecttable.ProjectTableMod;
import net.minecraft.block.Block;
import net.minecraftforge.registries.ObjectHolder;

@SuppressWarnings("Duplicates")
@ObjectHolder(ProjectTableMod.MODID)
public class BlockLibrary {
    public static final Block project_table;

    static {
        project_table = null;
    }
}
