package com.github.atomicblom.projecttable.client.mcgui.events;

import com.github.atomicblom.projecttable.client.mcgui.ControlBase;

public interface ICurrentValueChangedEventListener<TValue>
{
    void onCurrentValueChanged(ControlBase control, TValue previousValue, TValue newValue);
}
