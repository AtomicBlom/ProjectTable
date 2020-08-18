package com.github.atomicblom.projecttable.client.mcgui.events;

import com.github.atomicblom.projecttable.client.controls.ProjectTableRecipeControl;
import com.github.atomicblom.projecttable.client.mcgui.ControlBase;
import com.github.atomicblom.projecttable.client.mcgui.IGuiTemplate;
import com.github.atomicblom.projecttable.client.mcgui.IModelView;
import com.github.atomicblom.projecttable.client.mcgui.controls.ScrollPaneControl;
import com.github.atomicblom.projecttable.client.model.ProjectTableRecipeInstance;

public interface IItemMadeVisibleEventListener<TModel, TChildComponent extends ControlBase & IGuiTemplate<TChildComponent> & IModelView<TModel>>
{
     void onItemMadeVisible(ScrollPaneControl<TModel, TChildComponent> scrollPaneControl, TChildComponent childComponent, TModel model);
}
