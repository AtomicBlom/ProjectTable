package com.github.atomicblom.projecttable.api;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

/**
 * This event is fired during PostInit on the MinecraftForge.EVENT_BUS to notify dependant mods that SteamNSteel's
 * capabilities are available for use.
 */
@SuppressWarnings("unused") //This is an API class
public class ProjectTableInitializedEvent extends Event implements IModBusEvent {
    private final ICraftingManager craftingManager;

    public ProjectTableInitializedEvent(ICraftingManager craftingManager)
    {
        this.craftingManager = craftingManager;
    }

    public ICraftingManager getCraftingManager()
    {
        return craftingManager;
    }
}