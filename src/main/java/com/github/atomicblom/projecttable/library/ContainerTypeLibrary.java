package com.github.atomicblom.projecttable.library;

import com.github.atomicblom.projecttable.ProjectTableMod;
import com.github.atomicblom.projecttable.Reference;
import com.github.atomicblom.projecttable.inventory.ProjectTableContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ContainerTypeLibrary {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ProjectTableMod.MODID);

    public static final RegistryObject<MenuType<ProjectTableContainer>> projectTableContainer = MENUS.register(
            Reference.Container.PROJECT_TABLE.getPath(),
            () -> new MenuType<>(ProjectTableContainer::new)
    );
}