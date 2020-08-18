package com.github.atomicblom.projecttable.client.mcgui;

import com.github.atomicblom.projecttable.client.mcgui.util.IReadableRectangle;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import net.minecraft.util.ResourceLocation;

public class GuiTexture
{
    private final ResourceLocation textureLocation;
    private final int width;
    private final int height;
    private final IReadableRectangle bounds;

    public GuiTexture(ResourceLocation textureLocation, int width, int height)
    {
        this.textureLocation = textureLocation;
        bounds = new Rectangle(0, 0, width, height);
        this.width = width;
        this.height = height;
    }

    public ResourceLocation getTextureLocation()
    {
        return textureLocation;
    }

    public IReadableRectangle getBounds() {
        return bounds;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}

