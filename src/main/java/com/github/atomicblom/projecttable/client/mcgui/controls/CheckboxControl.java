package com.github.atomicblom.projecttable.client.mcgui.controls;

import com.github.atomicblom.projecttable.client.mcgui.ControlBase;
import com.github.atomicblom.projecttable.client.mcgui.GuiLogger;
import com.github.atomicblom.projecttable.client.mcgui.GuiRenderer;
import com.github.atomicblom.projecttable.client.mcgui.GuiTexture;
import com.github.atomicblom.projecttable.client.mcgui.events.ICheckboxPressedEventListener;
import com.github.atomicblom.projecttable.client.mcgui.util.IReadablePoint;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class CheckboxControl extends ControlBase {
    private GuiTexture defaultTexture;
    private GuiTexture pressedTexture;
    private GuiTexture disabledTexture;
    private GuiTexture currentTexture;
    private GuiTexture activeOverlayTexture;
    private boolean isDisabled;
    private boolean value;


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
    public void draw(MatrixStack matrixStack)
    {
        super.draw(matrixStack);
        if (isDisabled) {
            getGuiRenderer().drawComponentTexture(matrixStack, this, disabledTexture);
        } else if (currentTexture != null)
        {
            getGuiRenderer().drawComponentTexture(matrixStack, this, currentTexture);
        }

        if (this.value) {
            getGuiRenderer().drawComponentTextureWithOffset(matrixStack, this, activeOverlayTexture, 0, -2);
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
        this.value = !this.value;
        onButtonPressed();

        fireButtonPressedEvent();
    }

    protected void onButtonPressed() {
    }

    private void fireButtonPressedEvent()
    {
        for (final ICheckboxPressedEventListener currentValueChangedEventListener : buttonPressedEventListeners)
        {
            try {
                currentValueChangedEventListener.onCheckboxPressed(this, value);
            } catch (RuntimeException e) {
                GuiLogger.warning("Exception in an ICurrentValueChangedEventListener %s", e);
            }
        }
    }

    private final List<ICheckboxPressedEventListener> buttonPressedEventListeners = new ArrayList<>();

    public void addOnButtonPressedEventListener(ICheckboxPressedEventListener listener) {
        buttonPressedEventListeners.add(listener);
    }
    public void removeOnButtonPressedEventListener(ICheckboxPressedEventListener listener) {
        buttonPressedEventListeners.remove(listener);
    }

    /////////////////////////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////////////////////////

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

    public GuiTexture getDisabledTexture()
    {
        return disabledTexture;
    }

    public void setDisabledTexture(GuiTexture disabledTexture)
    {
        this.disabledTexture = disabledTexture;
    }

    public GuiTexture getActiveOverlayTexture() {
        return activeOverlayTexture;
    }

    public void setActiveOverlayTexture(GuiTexture activeOverlayTexture) {
        this.activeOverlayTexture = activeOverlayTexture;
    }

    public boolean isDisabled()
    {
        return isDisabled;
    }

    public void setDisabled(boolean disabled)
    {
        isDisabled = disabled;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public void setPressedTexture(GuiTexture pressedTexture) {
        this.pressedTexture = pressedTexture;
    }
}
