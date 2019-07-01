package com.github.atomicblom.projecttable.client.mcgui.events;

import com.github.atomicblom.projecttable.client.mcgui.ControlBase;
import com.github.atomicblom.projecttable.client.mcgui.IGuiTemplate;
import com.github.atomicblom.projecttable.client.mcgui.IModelView;
import com.github.atomicblom.projecttable.client.mcgui.controls.ScrollPaneControl;

public interface IItemMadeVisibleEventListener<TModel, TChildComponent extends ControlBase & IGuiTemplate<TChildComponent> & IModelView<TModel>>
{
     void onItemMadeVisible(ScrollPaneControl scrollPaneControl, TChildComponent childComponent, TModel model);
}
