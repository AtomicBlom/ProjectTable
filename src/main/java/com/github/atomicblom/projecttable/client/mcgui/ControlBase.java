package com.github.atomicblom.projecttable.client.mcgui;

import com.github.atomicblom.projecttable.client.mcgui.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.ArrayList;
import java.util.List;

public class ControlBase implements GuiEventListener
{
    private final Rectangle componentBounds = new Rectangle();
    private final GuiRenderer guiRenderer;
    private ControlBase parent = null;
    private final List<ControlBase> children = new ArrayList<ControlBase>(10);

    public ControlBase(GuiRenderer guiRenderer) {
        this(guiRenderer, new Rectangle());
    }

    public ControlBase(GuiRenderer guiRenderer, Rectangle componentBounds)
    {
        this.guiRenderer = guiRenderer;
        this.componentBounds.setBounds(componentBounds);
        onResizeInternal();
    }
    public ControlBase(GuiRenderer guiRenderer, int width, int height) {
        this(guiRenderer, new Rectangle(0, 0, width, height));
    }

    public void setLocation(int x, int y)
    {
        componentBounds.setLocation(x, y);
        onResizeInternal();
    }
    public void setLocation(IReadablePoint point) {
        componentBounds.setLocation(point);
        onResizeInternal();
    }
    public void setSize(int width, int height) {
        componentBounds.setSize(width, height);
        onResizeInternal();
    }
    public void setSize(IReadableDimension dimensions) {
        componentBounds.setSize(dimensions);
        onResizeInternal();
    }

    public void draw(PoseStack PoseStack) {
        for (final ControlBase child : children)
        {
            child.draw(PoseStack);
        }
    }

    public IReadableRectangle getBounds() {
        return componentBounds;
    }

    public void addChild(ControlBase child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(ControlBase child) {
        children.remove(child);
        child.setParent(null);
    }

    public ControlBase getParent() {
        return parent;
    }

    public void setParent(ControlBase parent)
    {
        this.parent = parent;
    }

    public boolean mouseClicked(IReadablePoint point, final int mouseButton)
    {
        IMouseCallback mouseCallback = new IMouseCallback() {
            @Override
            public boolean checkChild(ControlBase child, IReadablePoint localPoint) {
                return child.mouseClicked(localPoint, mouseButton);
            }

            @Override
            public boolean checkCurrent(final IReadablePoint point) {
                return onMouseClickInternal(point, mouseButton);
            }
        };
        return checkMouseBoundsAndPropagate(point, mouseCallback);
    }

    public boolean mouseReleased(final IReadablePoint point, final int mouseButton)
    {
        IMouseCallback mouseCallback = new IMouseCallback() {
            @Override
            public boolean checkChild(ControlBase child, IReadablePoint localPoint) {
                return child.mouseReleased(localPoint, mouseButton);
            }

            @Override
            public boolean checkCurrent(IReadablePoint point) {
                return onMouseReleasedInternal(point, mouseButton);
            }
        };
        return checkMouseBoundsAndPropagate(point, mouseCallback);
    }

    public boolean mouseMoved(final IReadablePoint point) {
        IMouseCallback mouseCallback = new IMouseCallback() {
            @Override
            public boolean checkChild(ControlBase child, IReadablePoint localPoint) {
                return child.mouseMoved(localPoint);
            }

            @Override
            public boolean checkCurrent(IReadablePoint point) {
                return onMouseMovedInternal(point);
            }
        };
        return checkMouseBoundsAndPropagate(point, mouseCallback);
    }

    public boolean mouseDragged(final IReadablePoint point, final IReadablePoint delta, final int buttons) {
        IMouseCallback mouseCallback = new IMouseCallback() {
            @Override
            public boolean checkChild(ControlBase child, IReadablePoint localPoint) {
                return child.mouseDragged(localPoint, delta, buttons);
            }

            @Override
            public boolean checkCurrent(IReadablePoint point) {
                return onMouseDraggedInternal(point, delta, buttons);
            }
        };
        return checkMouseBoundsAndPropagate(point, mouseCallback);
    }

    public boolean mouseDragStarted(final IReadablePoint point, final int buttons) {
        IMouseCallback mouseCallback = new IMouseCallback() {
            @Override
            public boolean checkChild(ControlBase child, IReadablePoint localPoint) {
                return child.mouseDragStarted(localPoint, buttons);
            }

            @Override
            public boolean checkCurrent(IReadablePoint point) {
                return onMouseDragStartedInternal(point, buttons);
            }
        };
        return checkMouseBoundsAndPropagate(point, mouseCallback);
    }

    public boolean mouseDragEnded(final IReadablePoint point, final int buttons) {
        IMouseCallback mouseCallback = new IMouseCallback() {
            @Override
            public boolean checkChild(ControlBase child, IReadablePoint localPoint) {
                return child.mouseDragEnded(localPoint, buttons);
            }

            @Override
            public boolean checkCurrent(IReadablePoint point) {
                return onMouseDragEndedInternal(point, buttons);
            }
        };
        return checkMouseBoundsAndPropagate(point, mouseCallback);
    }

    public boolean mouseWheelUp(final IReadablePoint point, final int scrollAmount)
    {
        IMouseCallback mouseCallback = new IMouseCallback() {
            @Override
            public boolean checkChild(ControlBase child, IReadablePoint localPoint) {
                return child.mouseWheelUp(localPoint, scrollAmount);
            }

            @Override
            public boolean checkCurrent(IReadablePoint point) {
                return mouseWheelUpInternal(point, scrollAmount);
            }
        };
        return checkMouseBoundsAndPropagate(point, mouseCallback);
    }

    public boolean mouseWheelDown(final IReadablePoint point, final int scrollAmount)
    {
        IMouseCallback mouseCallback = new IMouseCallback() {
            @Override
            public boolean checkChild(ControlBase child, IReadablePoint localPoint) {
                return child.mouseWheelDown(localPoint, scrollAmount);
            }

            @Override
            public boolean checkCurrent(IReadablePoint point) {
                return mouseWheelDownInternal(point, scrollAmount);
            }
        };
        return checkMouseBoundsAndPropagate(point, mouseCallback);
    }

    private boolean checkMouseBoundsAndPropagate(final IReadablePoint point, final IMouseCallback callback) {
        final Rectangle realControlBounds = new Rectangle();

        Point localPoint = new Point();
        //ProjectTableMod.logger.info("event triggered in {} @ {} - {}", this.getClass().getSimpleName(), this.getBounds(), point);

        boolean handled = false;
        for (final ControlBase child : children)
        {
            realControlBounds.setSize(child.getBounds());

            localPoint.setLocation(point);
            localPoint.untranslate(child.getBounds()); //Untranslate by the bounding location
            //localPoint will now be relative to the child.
            if (realControlBounds.contains(localPoint)) {
                if (callback.checkChild(child, localPoint)) {
                    handled = true;
                    break;
                }
            }
        }

        if (!handled) {
            handled = callback.checkCurrent(point);
        }
        return handled;
    }

    public GuiRenderer getGuiRenderer()
    {
        return guiRenderer;
    }

    private interface IMouseCallback {
        boolean checkChild(ControlBase child, IReadablePoint localPoint);

        boolean checkCurrent(IReadablePoint point);
    }

    protected void captureMouse() {
        MouseCapture.register(this);
    }

    protected void releaseMouse() {
        MouseCapture.unregister(this);
    }

    /////////////////////////////////////////////////////////////////////////////
    // Internal event handling
    /////////////////////////////////////////////////////////////////////////////
    private boolean onMouseClickInternal(IReadablePoint point, int mouseButton)
    {
        return onMouseClick(point, mouseButton);
    }

    private boolean onMouseReleasedInternal(IReadablePoint point, int mouseButton) {
        return onMouseRelease(point, mouseButton);
    }

    private boolean onMouseMovedInternal(IReadablePoint point) {
        return onMouseMoved(point);
    }

    private boolean onMouseDraggedInternal(IReadablePoint point, IReadablePoint delta, int mouseButton) {
        return onMouseDragged(point, delta, mouseButton);
    }

    private boolean onMouseDragStartedInternal(IReadablePoint point, int mouseButton) {
        return onMouseDragStarted(point, mouseButton);
    }

    private boolean onMouseDragEndedInternal(IReadablePoint point, int mouseButton) {
        return onMouseDragEnded(point, mouseButton);
    }

    private boolean mouseWheelUpInternal(IReadablePoint point, int scrollAmount)
    {
        return onMouseWheelUp(point, scrollAmount);
    }

    private boolean mouseWheelDownInternal(IReadablePoint point, int scrollAmount)
    {
        return onMouseWheelDown(point, scrollAmount);
    }

    private void onResizeInternal() {
        onResized(componentBounds);
    }



    /////////////////////////////////////////////////////////////////////////////
    // Events for subclasses
    /////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("UnusedParameters")
    protected boolean onMouseClick(IReadablePoint point, int mouseButton)
    {
        return false;
    }
    @SuppressWarnings("UnusedParameters")
    protected boolean onMouseRelease(IReadablePoint point, int mouseButton) {
        return false;
    }
    @SuppressWarnings("UnusedParameters")
    protected boolean onMouseMoved(IReadablePoint point) {
        return false;
    }
    @SuppressWarnings("UnusedParameters")
    protected boolean onMouseDragged(IReadablePoint point, IReadablePoint delta, int mouseButton) {
        return false;
    }
    @SuppressWarnings("UnusedParameters")
    protected boolean onMouseDragStarted(IReadablePoint point, int mouseButton) {
        return false;
    }
    @SuppressWarnings("UnusedParameters")
    protected boolean onMouseDragEnded(IReadablePoint point, int mouseButton) {
        return false;
    }
    @SuppressWarnings("UnusedParameters")
    protected boolean onMouseWheelUp(IReadablePoint point, int scrollAmount) {
        return false;
    }
    @SuppressWarnings("UnusedParameters")
    protected boolean onMouseWheelDown(IReadablePoint point, int scrollAmount) {
        return false;
    }

    @SuppressWarnings("UnusedParameters")
    protected void onResized(IReadableRectangle componentBounds) { }

}
