package com.github.atomicblom.projecttable.client.mcgui.controls;

import com.github.atomicblom.projecttable.client.mcgui.ControlBase;
import com.github.atomicblom.projecttable.client.mcgui.GuiLogger;
import com.github.atomicblom.projecttable.client.mcgui.GuiRenderer;
import com.github.atomicblom.projecttable.client.mcgui.GuiTexture;
import com.github.atomicblom.projecttable.client.mcgui.events.IButtonPressedEventListener;
import com.github.atomicblom.projecttable.client.mcgui.util.IReadablePoint;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class ButtonControl extends ControlBase
{
    private GuiTexture defaultTexture;
    private GuiTexture pressedTexture;
    private GuiTexture hoverTexture;
    private GuiTexture disabledTexture;
    private GuiTexture currentTexture;

    private boolean isDisabled = false;

    public ButtonControl(GuiRenderer guiRenderer)
    {
        super(guiRenderer);
    }

    public ButtonControl(GuiRenderer guiRenderer, Rectangle componentBounds)
    {
        super(guiRenderer, componentBounds);
    }

    public ButtonControl(GuiRenderer guiRenderer, int width, int height)
    {
        super(guiRenderer, width, height);
    }

    @Override
    public void draw(MatrixStack matrixStack)
    {
        super.draw(matrixStack);
        if (isDisabled) {
            getGuiRenderer().drawComponentTexture(matrixStack, this, disabledTexture);
        } else if (currentTexture != null)
        {
            getGuiRenderer().drawComponentTexture(matrixStack, this, currentTexture);
        }
    }

    @Override
    protected boolean onMouseRelease(IReadablePoint point, int mouseButton)
    {
        if (!isDisabled && mouseButton == 0)
        {
            onButtonPressedInternal();
            currentTexture = defaultTexture;
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseClick(IReadablePoint point, int mouseButton)
    {
        if (!isDisabled && mouseButton == 0)
        {
            currentTexture = pressedTexture;
            return true;
        }
        return false;
    }

    /////////////////////////////////////////////////////////////////////////////
    // Button Pressed Event Handling
    /////////////////////////////////////////////////////////////////////////////

    private void onButtonPressedInternal() {
        onButtonPressed();

        fireButtonPressedEvent();
    }

    protected void onButtonPressed() {
    }

    private void fireButtonPressedEvent()
    {
        for (final IButtonPressedEventListener currentValueChangedEventListener : buttonPressedEventListeners)
        {
            try {
                currentValueChangedEventListener.onButtonPressed(this);
            } catch (RuntimeException e) {
                GuiLogger.warning("Exception in an ICurrentValueChangedEventListener %s", e);
            }
        }
    }

    private final List<IButtonPressedEventListener> buttonPressedEventListeners = new ArrayList<IButtonPressedEventListener>();

    @SuppressWarnings("unused")
    public void addOnButtonPressedEventListener(IButtonPressedEventListener listener) {
        buttonPressedEventListeners.add(listener);
    }
    @SuppressWarnings("unused")
    public void removeOnButtonPressedEventListener(IButtonPressedEventListener listener) {
        buttonPressedEventListeners.remove(listener);
    }


    public GuiTexture getDefaultTexture()
    {
        return defaultTexture;
    }

    public void setDefaultTexture(GuiTexture defaultTexture)
    {
        if (currentTexture == this.defaultTexture) {
            currentTexture = defaultTexture;
        }
        this.defaultTexture = defaultTexture;
    }

    public GuiTexture getPressedTexture()
    {
        return pressedTexture;
    }

    public void setPressedTexture(GuiTexture pressedTexture)
    {
        this.pressedTexture = pressedTexture;
    }

    public GuiTexture getHoverTexture()
    {
        return hoverTexture;
    }

    public void setHoverTexture(GuiTexture hoverTexture)
    {
        this.hoverTexture = hoverTexture;
    }

    public GuiTexture getDisabledTexture()
    {
        return disabledTexture;
    }

    public void setDisabledTexture(GuiTexture disabledTexture)
    {
        this.disabledTexture = disabledTexture;
    }

    public boolean isDisabled()
    {
        return isDisabled;
    }

    public void setDisabled(boolean disabled)
    {
        isDisabled = disabled;
    }
}
