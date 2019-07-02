package com.github.atomicblom.projecttable.client.mcgui.events;

import com.github.atomicblom.projecttable.client.mcgui.controls.CheckboxControl;

public interface ICheckboxPressedEventListener
{
    void onCheckboxPressed(CheckboxControl button, boolean value);
}
