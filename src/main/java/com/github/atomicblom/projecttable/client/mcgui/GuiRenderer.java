package com.github.atomicblom.projecttable.client.mcgui;

import com.github.atomicblom.projecttable.client.mcgui.util.IReadablePoint;
import com.github.atomicblom.projecttable.client.mcgui.util.IReadableRectangle;
import com.github.atomicblom.projecttable.client.mcgui.util.Point;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Stack;

public class GuiRenderer
{
    private final Minecraft client;
    private final TextureManager textureManager;
    private final Font fontRenderer;
    private final ItemRenderer itemRenderer;

    @Nullable
    private GuiTexture currentTexture = null;

    public GuiRenderer(McGUI<?> gui)
    {
        this.client = gui.getMinecraft();
        this.textureManager = this.client.textureManager;
        this.fontRenderer = this.client.font;
        this.itemRenderer = this.client.getItemRenderer();
    }

    public TextureManager getTextureManager()
    {
        return textureManager;
    }

    public Font getFontRenderer()
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
        currentTexture = texture;
        RenderSystem.setShaderTexture(0, texture.getTextureLocation());
    }

    public void notifyTextureChanged() {
        currentTexture = null;
    }

    /////////////////////////////////////////////////////////////////////////////
    // Image rendering
    /////////////////////////////////////////////////////////////////////////////
    public void drawModelRectWithCustomSizedTexture(PoseStack PoseStack, ControlBase control, GuiTexture texture)
    {
        drawModelRectWithCustomSizedTexture(PoseStack, control, texture, 0, 0);
    }

    public void drawModelRectWithCustomSizedTexture(PoseStack PoseStack, ControlBase control, GuiTexture texture, int offsetX, int offsetY)
    {
        final IReadablePoint controlLocation = getControlLocation(control);
        verifyTexture(texture);
        final IReadableRectangle componentSubtexture = texture.getBounds();
        GuiComponent.blit(
                PoseStack,
                controlLocation.getX() + offsetX, controlLocation.getY() + offsetY,
                componentSubtexture.getX(), componentSubtexture.getY(),
                componentSubtexture.getWidth(), componentSubtexture.getHeight(),
                texture.getWidth(), texture.getHeight());
    }

    public void drawComponentTexture(PoseStack PoseStack, ControlBase control, GuiTexture texture)
    {
        verifyTexture(texture);
        drawModelRectWithCustomSizedTexture(PoseStack, control, texture, 0, 0);
    }

    public void drawComponentTextureWithOffset(PoseStack PoseStack, ControlBase control, GuiTexture texture, int offsetX, int offsetY)
    {
        drawModelRectWithCustomSizedTexture(PoseStack, control, texture, offsetX, offsetY);
    }

    /////////////////////////////////////////////////////////////////////////////
    // Item Rendering
    /////////////////////////////////////////////////////////////////////////////
    public void renderItem(ControlBase control, ItemStack itemStack, int x, int y)
    {
        final IReadablePoint controlLocation = getControlLocation(control);
        itemRenderer.renderAndDecorateItem(itemStack, controlLocation.getX() + x, controlLocation.getY() + y);
        notifyTextureChanged();
    }

    /////////////////////////////////////////////////////////////////////////////
    // Text Rendering
    /////////////////////////////////////////////////////////////////////////////
    public void drawStringWithShadow(PoseStack PoseStack, ControlBase control, String text, int x, int y, int colour)
    {
        final IReadablePoint controlLocation = getControlLocation(control);
        fontRenderer.drawShadow(PoseStack, text, controlLocation.getX() + x, controlLocation.getY() + y, colour);
        notifyTextureChanged();
    }

    public int getStringWidth(String text)
    {
        return fontRenderer.width(text);
    }


    /////////////////////////////////////////////////////////////////////////////
    // Viewport Management
    /////////////////////////////////////////////////////////////////////////////
    private final Stack<Rectangle> viewportStack = new Stack<Rectangle>();

    public void startViewport(ControlBase control, Rectangle bounds) {
        final IReadablePoint controlLocation = getControlLocation(control);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        final double scaleW = client.getWindow().getGuiScale();
        final double scaleH = client.getWindow().getGuiScale();
        final int displayHeight = client.getWindow().getHeight();

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
