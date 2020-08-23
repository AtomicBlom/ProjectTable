package com.github.atomicblom.projecttable.registration;

import com.github.atomicblom.projecttable.inventory.ProjectTableContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class GuiRegistration {
    @SubscribeEvent
    public static void onContainerTypeRegistry(final RegistryEvent.Register<ContainerType<?>> containerTypeRegistryEvent) {
        IForgeRegistry<ContainerType<?>> registry = containerTypeRegistryEvent.getRegistry();
        registry.register(new ContainerType<>(ProjectTableContainer::new).setRegistryName("projecttable", "projecttablecontainer"));
    }
}
