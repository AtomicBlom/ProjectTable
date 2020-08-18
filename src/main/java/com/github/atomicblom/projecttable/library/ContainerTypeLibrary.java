package com.github.atomicblom.projecttable.library;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.inventory.ProjectTableContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.ObjectHolder;

@SuppressWarnings("Duplicates")
@ObjectHolder(ProjectTableMod.MODID)
public class ContainerTypeLibrary {
    @ObjectHolder("projecttablecontainer")
    public static final ContainerType<ProjectTableContainer> projectTableContainer;

    static {
        projectTableContainer = null;
    }
}