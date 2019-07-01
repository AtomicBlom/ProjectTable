package com.github.atomicblom.projecttable.client.mcgui;

public interface IGuiTemplate<TControl extends ControlBase>
{
    TControl construct();

}
