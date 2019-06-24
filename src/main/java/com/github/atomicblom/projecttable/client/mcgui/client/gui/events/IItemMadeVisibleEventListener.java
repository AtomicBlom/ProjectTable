package com.github.atomicblom.projecttable.client.mcgui.client.gui.events;

import com.github.atomicblom.projecttable.client.mcgui.client.gui.ControlBase;
import com.github.atomicblom.projecttable.client.mcgui.client.gui.IGuiTemplate;
import com.github.atomicblom.projecttable.client.mcgui.client.gui.IModelView;
import com.github.atomicblom.projecttable.client.mcgui.client.gui.controls.ScrollPaneControl;

public interface IItemMadeVisibleEventListener<TModel, TChildComponent extends ControlBase & IGuiTemplate<TChildComponent> & IModelView<TModel>>
{
     void onItemMadeVisible(ScrollPaneControl scrollPaneControl, TChildComponent childComponent, TModel model);
}
