/*
 * Copyright (c) 2014 Rosie Alexander and Scott Killen.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 */

package com.github.atomicblom.projecttable.client.mcgui;

import com.github.atomicblom.projecttable.client.mcgui.util.IReadablePoint;
import com.github.atomicblom.projecttable.client.mcgui.util.Point;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class McGUI<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
{
    private static final int TEXT_COLOR = 4210752;
    private static final String LOCATION = "textures/gui/";
    private static final String FILE_EXTENSION = ".png";
    private static final Component INVENTORY = MutableComponent.create(new TranslatableContents("inventory.inventory"));
    private final String modId = "";
    private ControlBase rootControl = null;

    protected McGUI(T container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.addRenderableOnly(this);
    }

    protected abstract ResourceLocation getResourceLocation(String path);

    protected abstract String getInventoryName();

    public final void setRootControl(ControlBase rootControl) {
        this.rootControl = rootControl;
    }

    protected final void addChild(ControlBase childControl) {
        rootControl.addChild(childControl);
    }

    @Override
    protected void renderLabels(PoseStack PoseStack, int mouseX, int mouseZ)
    {
        final String name = Language.getInstance().getOrDefault(getInventoryName());

        font.draw(PoseStack, name, imageWidth / 2.0f - font.width(name) / 2.0f, 6, TEXT_COLOR);
        font.draw(PoseStack, INVENTORY, (float)8, (float)(imageHeight - 96 + 2), TEXT_COLOR);
    }

    @Override
    protected void renderBg(PoseStack PoseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        final int xStart = (width - imageWidth) / 2;
        final int yStart = (height - imageHeight) / 2;

        rootControl.setLocation(xStart, yStart);
        rootControl.draw(PoseStack);
    }


    /////////////////////////////////////////////////////////////////////////////
    // Control Event handling
    /////////////////////////////////////////////////////////////////////////////
    private final Point lastMouseLocation = new Point();
    private final Point currentMouseLocation = new Point();
    private boolean isDragging;
    private int dragButton;
    private final Rectangle bounds = new Rectangle();
    private int eventButton;
    private int touchValue;
    private long lastMouseEvent;

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButtons, double dragDeltaX, double dragDeltaZ) {
        Point dragDelta = new Point((int) dragDeltaX, (int) dragDeltaZ);

        return this.rootControl.mouseDragged(getGuiMousePointInternal(mouseX, mouseY), dragDelta, mouseButtons) ||
                super.mouseDragged(mouseX, mouseY, mouseButtons, dragDeltaX, dragDeltaZ);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        this.rootControl.mouseMoved(getGuiMousePointInternal(mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButtons) {
        return this.rootControl.mouseClicked(getGuiMousePointInternal(mouseX, mouseY), mouseButtons) ||
                super.mouseClicked(mouseX, mouseY, mouseButtons);
    }

    private IReadablePoint getGuiMousePointInternal(double mouseX, double mouseY) {
        return new Point((int)mouseX - (width - imageWidth) / 2, (int)mouseY - (height - imageHeight) / 2);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButtons) {
        boolean eventOccurred = super.mouseReleased(mouseX, mouseY, mouseButtons);

        eventOccurred |= this.rootControl.mouseReleased(getGuiMousePointInternal(mouseX, mouseY), mouseButtons);

        for (final ControlBase control : MouseCapture.getCapturedControls())
        {
            eventOccurred |= control.mouseReleased(currentMouseLocation, eventButton);
        }
        return eventOccurred;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (scrollAmount > 0) {
            this.rootControl.mouseWheelUp(getGuiMousePointInternal(mouseX, mouseY), (int)scrollAmount);
        } else {
            this.rootControl.mouseWheelDown(getGuiMousePointInternal(mouseX, mouseY), (int)scrollAmount);
        }
        return false;
    }

    /*
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (rootControl == null) {
            return;
        }

        int eventX = Mouse.getEventX() * width / mc.displayWidth;
        int eventY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int eventButton = Mouse.getEventButton();

        bounds.setBounds(rootControl.getBounds());
        if (!bounds.contains(eventX, eventY)) {
            return;
        }
        eventX -= bounds.getX();
        eventY -= bounds.getY();

        currentMouseLocation.setLocation(eventX, eventY);
        if (Mouse.getEventButtonState())
        {
            if (mc.gameSettings.touchscreen && touchValue++ > 0)
            {
                return;
            }

            this.eventButton = eventButton;
            lastMouseEvent = Minecraft.getSystemTime();
            rootControl.mouseClicked(currentMouseLocation, eventButton);
        }
        else if (eventButton != -1)
        {
            if (mc.gameSettings.touchscreen && --touchValue > 0)
            {
                return;
            }

            this.eventButton = -1;
            rootControl.mouseReleased(currentMouseLocation, eventButton);
            for (final ControlBase control : MouseCapture.getCapturedControls())
            {
                control.mouseReleased(currentMouseLocation, eventButton);
            }
            if (isDragging && eventButton == dragButton) {
                //Logger.info("Mouse Drag Ended %s", currentMouseLocation);
                rootControl.mouseDragEnded(currentMouseLocation, eventButton);
                for (final ControlBase control : MouseCapture.getCapturedControls())
                {
                    control.mouseDragEnded(currentMouseLocation, eventButton);
                }
                isDragging = false;
            }
        }

        if (!currentMouseLocation.equals(lastMouseLocation)) {

            //Logger.info("Mouse Moved %s", currentMouseLocation);
            rootControl.mouseMoved(currentMouseLocation);

            if (this.eventButton != -1 && lastMouseEvent > 0L) {
                if (!isDragging) {
                    //Logger.info("Mouse Drag started %s", currentMouseLocation);
                    rootControl.mouseDragStarted(currentMouseLocation, this.eventButton);
                    isDragging = true;
                    dragButton = this.eventButton;
                } else {
                    //Logger.info("Mouse Dragged %s", currentMouseLocation);
                    Point delta = new Point(currentMouseLocation);
                    delta.untranslate(lastMouseLocation);

                    rootControl.mouseDragged(currentMouseLocation, delta, this.eventButton);
                    Point p = new Point();
                    for (final ControlBase control : MouseCapture.getCapturedControls())
                    {
                        final IReadablePoint controlLocation = GuiRenderer.getControlLocation(control);
                        p.setLocation(currentMouseLocation);
                        p.untranslate(controlLocation);
                        p.translate(rootControl.getBounds());
                        control.mouseDragged(p, delta, this.eventButton);
                    }
                }
            }
            lastMouseLocation.setLocation(currentMouseLocation);
        }

        final int scrollAmount = Mouse.getDWheel();
        if (scrollAmount != 0) {
            if (scrollAmount > 0) {
                rootControl.mouseWheelUp(currentMouseLocation, scrollAmount);
            } else {
                rootControl.mouseWheelDown(currentMouseLocation, scrollAmount);
            }
        }
    }*/
}
