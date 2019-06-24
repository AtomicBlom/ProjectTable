package com.github.atomicblom.projecttable.client.mcgui.client.gui.events;

import com.github.atomicblom.projecttable.client.mcgui.client.gui.ControlBase;

public interface ICurrentValueChangedEventListener<TValue>
{
    void onCurrentValueChanged(ControlBase control, TValue previousValue, TValue newValue);
}
