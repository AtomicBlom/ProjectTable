package com.github.atomicblom.projecttable.client.mcgui.controls;

import com.github.atomicblom.projecttable.client.mcgui.ControlBase;
import com.github.atomicblom.projecttable.client.mcgui.GuiRenderer;
import com.github.atomicblom.projecttable.client.mcgui.GuiTexture;
import org.lwjgl.util.Rectangle;

public class TexturedPaneControl extends ControlBase
{
    private final GuiTexture texture;

    public TexturedPaneControl(GuiRenderer guiRenderer, Rectangle componentBounds, GuiTexture texture)
    {
        super(guiRenderer, componentBounds);
        this.texture = texture;
    }

    public TexturedPaneControl(GuiRenderer guiRenderer, int width, int height, GuiTexture texture)
    {
        super(guiRenderer, width, height);
        this.texture = texture;
    }

    @Override
    public void draw()
    {
        getGuiRenderer().drawComponentTexture(this, texture);
        super.draw();
    }
}
