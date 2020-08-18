package com.github.atomicblom.projecttable.client.mcgui;

import com.github.atomicblom.projecttable.client.mcgui.util.IReadablePoint;
import com.github.atomicblom.projecttable.client.mcgui.util.IReadableRectangle;
import com.github.atomicblom.projecttable.client.mcgui.util.Point;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Stack;

public class GuiRenderer
{
    private final Minecraft client;
    private final McGUI<?> gui;
    private final TextureManager textureManager;
    private final FontRenderer fontRenderer;
    private final ItemRenderer itemRenderer;

    @Nullable
    private GuiTexture currentTexture = null;

    public GuiRenderer(McGUI<?> gui)
    {
        this.client = gui.getMinecraft();
        this.gui = gui;
        this.textureManager = this.client.textureManager;
        this.fontRenderer = this.client.fontRenderer;
        this.itemRenderer = this.client.getItemRenderer();
    }

    public TextureManager getTextureManager()
    {
        return textureManager;
    }

    public FontRenderer getFontRenderer()
    {
        return fontRenderer;
    }

    public ItemRenderer getItemRenderer()
    {
        return itemRenderer;
    }

    /////////////////////////////////////////////////////////////////////////////
    // Texture Management
    /////////////////////////////////////////////////////////////////////////////
    private void verifyTexture(GuiTexture texture)
    {
        if (currentTexture == null || !texture.equals(currentTexture)) {
            currentTexture = texture;
            textureManager.bindTexture(texture.getTextureLocation());
        }
    }

    public void notifyTextureChanged() {
        currentTexture = null;
    }

    /////////////////////////////////////////////////////////////////////////////
    // Image rendering
    /////////////////////////////////////////////////////////////////////////////
    public void drawModelRectWithCustomSizedTexture(MatrixStack matrixStack, ControlBase control, GuiTexture texture)
    {
        drawModelRectWithCustomSizedTexture(matrixStack, control, texture, 0, 0);
    }

    public void drawModelRectWithCustomSizedTexture(MatrixStack matrixStack, ControlBase control, GuiTexture texture, int offsetX, int offsetY)
    {
        final IReadablePoint controlLocation = getControlLocation(control);
        verifyTexture(texture);
        final IReadableRectangle componentSubtexture = texture.getBounds();
        AbstractGui.blit(
                matrixStack,
                controlLocation.getX() + offsetX, controlLocation.getY() + offsetY,
                componentSubtexture.getX(), componentSubtexture.getY(),
                componentSubtexture.getWidth(), componentSubtexture.getHeight(),
                texture.getWidth(), texture.getHeight());
    }

    public void drawComponentTexture(MatrixStack matrixStack, ControlBase control, GuiTexture texture)
    {
        verifyTexture(texture);
        drawModelRectWithCustomSizedTexture(matrixStack, control, texture, 0, 0);
    }

    public void drawComponentTextureWithOffset(MatrixStack matrixStack, ControlBase control, GuiTexture texture, int offsetX, int offsetY)
    {
        drawModelRectWithCustomSizedTexture(matrixStack, control, texture, offsetX, offsetY);
    }

    /////////////////////////////////////////////////////////////////////////////
    // Item Rendering
    /////////////////////////////////////////////////////////////////////////////
    public void renderItem(ControlBase control, ItemStack itemStack, int x, int y)
    {
        final IReadablePoint controlLocation = getControlLocation(control);
        RenderHelper.enableStandardItemLighting();
        itemRenderer.renderItemIntoGUI(itemStack, controlLocation.getX() + x, controlLocation.getY() + y);
        RenderHelper.disableStandardItemLighting();
        notifyTextureChanged();
    }

    /////////////////////////////////////////////////////////////////////////////
    // Text Rendering
    /////////////////////////////////////////////////////////////////////////////
    public void drawStringWithShadow(MatrixStack matrixStack, ControlBase control, String text, int x, int y, int colour)
    {
        final IReadablePoint controlLocation = getControlLocation(control);
        fontRenderer.drawStringWithShadow(matrixStack, text, controlLocation.getX() + x, controlLocation.getY() + y, colour);
        notifyTextureChanged();
    }

    public int getStringWidth(String text)
    {
        return fontRenderer.getStringWidth(text);
    }


    /////////////////////////////////////////////////////////////////////////////
    // Viewport Management
    /////////////////////////////////////////////////////////////////////////////
    private final Stack<Rectangle> viewportStack = new Stack<Rectangle>();

    public void startViewport(ControlBase control, Rectangle bounds) {
        final IReadablePoint controlLocation = getControlLocation(control);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        final double scaleW = client.getMainWindow().getGuiScaleFactor();
        final double scaleH = client.getMainWindow().getGuiScaleFactor();
        final int displayHeight = client.getMainWindow().getHeight();

        final int x = (int) ((controlLocation.getX() + bounds.getX()) * scaleW);
        final int y = displayHeight - ((int) (controlLocation.getY() * scaleH) + (int) (bounds.getHeight() * scaleH));
        final int width = (int) (bounds.getWidth() * scaleW);
        final int height = (int) (bounds.getHeight() * scaleH);
        GL11.glScissor(x, y, width, height);
    }

    public void endViewport() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /////////////////////////////////////////////////////////////////////////////
    // Utilities
    /////////////////////////////////////////////////////////////////////////////
    public static IReadablePoint getControlLocation(ControlBase control) {
        ControlBase parent = control;
        int offsetX = 0;
        int offsetY = 0;
        while (parent != null) {
            final IReadableRectangle bounds = parent.getBounds();
            offsetX += bounds.getX();
            offsetY += bounds.getY();
            parent = parent.getParent();
        }
        return new Point(offsetX, offsetY);
    }
}
