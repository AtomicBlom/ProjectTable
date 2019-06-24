package com.github.atomicblom.projecttable.client.mcgui.client.gui;

public interface IGuiTemplate<TControl extends ControlBase>
{
    TControl construct();

}
