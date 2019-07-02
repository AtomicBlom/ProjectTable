package com.github.atomicblom.projecttable.client.mcgui.controls;

import com.github.atomicblom.projecttable.client.mcgui.ControlBase;
import com.github.atomicblom.projecttable.client.mcgui.GuiRenderer;
import org.lwjgl.util.Rectangle;

public class CheckboxControl extends ControlBase {
    public CheckboxControl(GuiRenderer guiRenderer)
    {
        super(guiRenderer);
    }

    public CheckboxControl(GuiRenderer guiRenderer, Rectangle componentBounds)
    {
        super(guiRenderer, componentBounds);
    }

    public CheckboxControl(GuiRenderer guiRenderer, int width, int height)
    {
        super(guiRenderer, width, height);
    }

    @Override
    public void draw()
    {
        super.draw();
        /*if (isDisabled) {
            getGuiRenderer().drawComponentTexture(this, disabledTexture);
        } else if (currentTexture != null)
        {
            getGuiRenderer().drawComponentTexture(this, currentTexture);
        }*/
    }
}
