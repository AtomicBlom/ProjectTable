package com.github.atomicblom.projecttable.gui;

import com.github.atomicblom.projecttable.client.ProjectTableGui;
import com.github.atomicblom.projecttable.inventory.ProjectTableContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public enum GuiHandler implements IGuiHandler
{
    INSTANCE;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        final ModGUIs gui = ModGUIs.fromId(id);
        switch(gui)
        {
            case PROJECT_TABLE:
                return new ProjectTableContainer(player.inventory);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        final ModGUIs gui = ModGUIs.fromId(id);
        switch(gui)
        {
            case PROJECT_TABLE:
                return new ProjectTableGui(player.inventory);
        }
        return null;
    }
}
