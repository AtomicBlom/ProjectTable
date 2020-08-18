package com.github.atomicblom.projecttable.client.mcgui.controls;

import com.github.atomicblom.projecttable.client.mcgui.ControlBase;
import com.github.atomicblom.projecttable.client.mcgui.GuiRenderer;
import com.github.atomicblom.projecttable.client.mcgui.GuiTexture;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import com.mojang.blaze3d.matrix.MatrixStack;

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
    public void draw(MatrixStack matrixStack)
    {
        getGuiRenderer().drawComponentTexture(matrixStack, this, texture);
        super.draw(matrixStack);
    }
}
