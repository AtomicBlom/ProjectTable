package com.github.atomicblom.projecttable.client.mcgui.controls;

import com.github.atomicblom.projecttable.client.mcgui.*;
import com.github.atomicblom.projecttable.client.mcgui.events.ICurrentValueChangedEventListener;
import com.github.atomicblom.projecttable.client.mcgui.events.IItemMadeVisibleEventListener;
import com.github.atomicblom.projecttable.client.mcgui.util.IReadablePoint;
import com.github.atomicblom.projecttable.client.mcgui.util.IReadableRectangle;
import com.github.atomicblom.projecttable.client.mcgui.util.Rectangle;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScrollPaneControl<TModel, TChildComponentTemplate extends ControlBase & IGuiTemplate<TChildComponentTemplate> & IModelView<TModel>> extends ControlBase
{
    private ControlBase[] itemRenderers = new ControlBase[0];
    private int lastItemsListCount = 0;
    private List<TModel> items = Lists.newArrayList();
    private TChildComponentTemplate template = null;
    private ScrollbarControl scrollbar = null;
    private final ScrollbarChangedEventListener scrollbarListener = new ScrollbarChangedEventListener();
    private int scrollbarOffset;
    private int visibleItemCount;
    private int previousItemIndex = Integer.MIN_VALUE;

    public ScrollPaneControl(GuiRenderer guiRenderer, Rectangle componentBounds)
    {
        super(guiRenderer, componentBounds);
    }

    public ScrollPaneControl(GuiRenderer guiRenderer, int width, int height) {
        this(guiRenderer, new Rectangle(0, 0, width, height));
    }

    public ScrollPaneControl<TModel, TChildComponentTemplate> setItemRendererTemplate(TChildComponentTemplate guiComponentTemplate) {
        template = guiComponentTemplate;
        if (scrollbar != null) {
            scrollbar.setScrollSize(template.getBounds().getHeight() / 2);
        }
        return this;
    }

    public ScrollPaneControl<TModel, TChildComponentTemplate> setVisibleItemCount(int visibleItems) {
        if (template == null) {
            throw new McGUIException("Can't set the visible item count, a template hasn't been defined yet");
        }
        visibleItemCount = visibleItems;
        final int actualItems = visibleItemCount + 1;
        itemRenderers = new ControlBase[actualItems];
        for (int i = 0; i < actualItems; ++i) {
            itemRenderers[i] = template.construct();
            addChild(itemRenderers[i]);
        }
        return this;
    }

    public ScrollPaneControl<TModel, TChildComponentTemplate> setScrollbar(ScrollbarControl scrollbar)
    {
        if (this.scrollbar != null) {
            this.scrollbar.removeOnCurrentValueChangedEventListener(scrollbarListener);
        }

        this.scrollbar = scrollbar;
        if (scrollbar != null) {
            this.scrollbar.addOnCurrentValueChangedEventListener(scrollbarListener);
            if (template != null) {
                scrollbar.setScrollSize(template.getBounds().getHeight() / 2);
            }
        }
        return this;
    }

    public ScrollPaneControl<TModel, TChildComponentTemplate> setItems(List<TModel> items) {
        this.items = items == null ? new ArrayList<>(0) : items;
        final int maximumValue = Math.max(0, (this.items.size() - visibleItemCount) * template.getBounds().getHeight());
        scrollbar.setMaximumValue(maximumValue);

        for (int i = 0; i < itemRenderers.length; i++) {
            if (i < items.size()) {
                if (itemRenderers[i].getParent() == null) {
                    addChild(itemRenderers[i]);
                }
            } else {
                if (itemRenderers[i].getParent() == this) {
                    removeChild(itemRenderers[i]);
                }
            }
        }

        return this;
    }

    public Iterable<TModel> getVisibleItems() {
        //noinspection unchecked
        return Arrays.stream(itemRenderers)
                .map(ir -> ((IModelView<TModel>)ir).getModel())
                .collect(Collectors.toList());
    }

    @Override
    public boolean mouseWheelUp(IReadablePoint point, int scrollAmount)
    {
        if (scrollbar != null) {
            return scrollbar.mouseWheelUp(point, scrollAmount);
        } else
        {
            //Handle logic for a scroll pane without a scrollbar
            return false;
        }
    }

    @Override
    public boolean mouseWheelDown(IReadablePoint point, int scrollAmount)
    {
        if (scrollbar != null) {
            return scrollbar.mouseWheelDown(point, scrollAmount);
        } else
        {
            //Handle logic for a scroll pane without a scrollbar
            return false;
        }
    }

    @Override
    public void draw(PoseStack PoseStack)
    {
        if (itemRenderers.length == 0 || items.isEmpty()) {
            return;
        }

        final IReadableRectangle templateBounds = template.getBounds();

        final int itemHeight = templateBounds.getHeight() * items.size();
        final int viewportHeight = templateBounds.getHeight() * 5;

        double scrollbarProgress = 0;
        final int usableScrollingHeight = itemHeight - templateBounds.getHeight() * visibleItemCount;
        boolean resetModels = false;
        if (items.size() != lastItemsListCount) {
            lastItemsListCount = items.size();
            scrollbar.setEnabled(usableScrollingHeight > 0);
            final int maximumValue = Math.max(0, (items.size() - visibleItemCount) * template.getBounds().getHeight());
            scrollbar.setMaximumValue(maximumValue);
            scrollbar.setCurrentValue(0);
            resetModels = true;
        }

        final Rectangle viewport = new Rectangle(
                0, 0,
                templateBounds.getWidth(), viewportHeight);

        getGuiRenderer().startViewport(this, viewport);

        if (usableScrollingHeight > 0)
        {
            scrollbarProgress = scrollbarOffset / (double) usableScrollingHeight;
        }
        final double itemProgress = Math.max(0, scrollbarProgress * (items.size() - visibleItemCount));

        final int itemIndex = (int)Math.floor(itemProgress);
        final int itemOffset = scrollbarOffset % templateBounds.getHeight();
        for (int i = 0; i < itemRenderers.length; ++i)
        {
            final ControlBase itemRenderer = itemRenderers[i];
            if (resetModels || itemIndex != previousItemIndex)
            {
                TModel model = null;
                if (itemIndex + i < items.size())
                {
                    model = items.get(itemIndex + i);
                }

                //This is unchecked, but the generic constraints ensure this cast is possible.
                if (itemRenderer instanceof IModelView)
                {
                    //noinspection unchecked
                    ((IModelView<TModel>) itemRenderer).setModel(model);
                }

                //noinspection unchecked,ConstantConditions
                onItemMadeVisibleInternal((TChildComponentTemplate)itemRenderer, model);
            }

            itemRenderer.setLocation(0, templateBounds.getHeight() * i - itemOffset);
        }

        previousItemIndex = itemIndex;

        super.draw(PoseStack);

        getGuiRenderer().endViewport();
    }

    public class ScrollbarChangedEventListener implements ICurrentValueChangedEventListener<Integer>
    {
        @Override
        public void onCurrentValueChanged(ControlBase control, Integer previousValue, Integer newValue)
        {
            scrollbarOffset = newValue;
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // Item Visible
    /////////////////////////////////////////////////////////////////////////////

    private void onItemMadeVisibleInternal(TChildComponentTemplate itemRenderer, TModel model) {
        onItemMadeVisible(itemRenderer, model);

        fireItemMadeVisible(itemRenderer, model);
    }

    protected void onItemMadeVisible(TChildComponentTemplate itemRenderer, TModel model) {
    }

    private void fireItemMadeVisible(TChildComponentTemplate itemRenderer, TModel model)
    {
        for (final IItemMadeVisibleEventListener<TModel, TChildComponentTemplate> currentValueChangedEventListener : itemMadeVisibleEventListeners)
        {
            try {
                //noinspection unchecked
                currentValueChangedEventListener.onItemMadeVisible(this, itemRenderer, model);
            } catch (RuntimeException e) {
                GuiLogger.warning("Exception in an ICurrentValueChangedEventListener %s", e);
            }
        }
    }

    private final List<IItemMadeVisibleEventListener<TModel, TChildComponentTemplate>> itemMadeVisibleEventListeners = Lists.newArrayList();

    @SuppressWarnings("unused")
    public void addOnFireItemMadeVisibleEventListener(IItemMadeVisibleEventListener<TModel, TChildComponentTemplate> listener) {
        itemMadeVisibleEventListeners.add(listener);
    }
    @SuppressWarnings("unused")
    public void removeOnFireItemMadeVisibleEventListener(IItemMadeVisibleEventListener<TModel, TChildComponentTemplate> listener) {
        itemMadeVisibleEventListeners.remove(listener);
    }
}
